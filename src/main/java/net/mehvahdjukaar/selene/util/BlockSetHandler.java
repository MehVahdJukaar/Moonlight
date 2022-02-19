package net.mehvahdjukaar.selene.util;

import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.selene.blocks.VerticalSlabBlock;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.*;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class BlockSetHandler {

    /**
     * Do not access these to register your blocks since they are empty right before the last registration phase.
     * Use addWoodEntryRegistrationCallback instead
     */
    public static Map<ResourceLocation, WoodSetType> WOOD_TYPES = new LinkedHashMap<>();

    private static boolean hasFilledWoodTypes = false;

    /**
     * Use this function to register a modded wood type manually.
     * This is handy for wood types that are somewhat unique and which can't be detected by the detection system
     * Use with care
     *
     * @param woodSetType modded wood type
     */
    public static void registerCustomWoodType(WoodSetType woodSetType) {
        WOOD_TYPES.put(woodSetType.id, woodSetType);
    }

    /**
     * Gets corresponding wood type or oak if the provided one is not installed or missing
     *
     * @param name string resource location name of the type
     * @return wood type
     */
    public static WoodSetType getWoodTypeFromNBT(String name) {
        return WOOD_TYPES.getOrDefault(new ResourceLocation(name), WoodSetType.OAK_WOOD_TYPE);
    }

    @FunctionalInterface
    public interface WoodRegistryCallback<T extends IForgeRegistryEntry<T>> {
        void accept(RegistryEvent.Register<T> reg, Collection<WoodSetType> wood);
    }

    //shitties code ever lol
    protected static void registerModLateBlockAndItems(RegistryEvent.Register<Item> event) {
        //when the first registration function is called we find all wood types
        if (!hasFilledWoodTypes) {
            findAllAvailableWoodTypes();
            hasFilledWoodTypes = true;
        }
        //get the queue corresponding to this certain mod
        String modId = ModLoadingContext.get().getActiveContainer().getModId();
        var registrationQueues =
                LATE_REGISTRATION_QUEUE.get(modId);
        if (registrationQueues != null) {
            //register blocks
            var blockQueue = registrationQueues.getFirst();
            blockQueue.forEach(Runnable::run);
            //registers items
            var itemQueue = registrationQueues.getSecond();
            itemQueue.forEach(q -> q.accept(event));
        }
        //clears stuff that's been execured. not really needed but just to be safe its here
        LATE_REGISTRATION_QUEUE.remove(registrationQueues);
    }

    //maps containing mod ids and block and items runnables. Block one is ready to run, items needs the bus supplied to it
    //they will be run each mod at a time block first then items
    private static final Map<String, Pair<
            List<Runnable>, //block registration function
            List<Consumer<RegistryEvent.Register<Item>>> //item registration function
            >>
            LATE_REGISTRATION_QUEUE = new HashMap<>();

    /**
     * Add a registry function meant to register a set of blocks that use a specific wood type
     * Other entries like items can access WOOD_TYPES directly since it will be filled
     * Will be called (hopefully) after all other block registrations have been fired so the wood set type is complete
     * IMPORTANT: your mod needs to be set to run AFTER this mod. You can sed it in your mods.toml dependency
     * If you dont this will still work but registration will throw some warnings
     *
     * @param registrationFunction registry function
     */
    //this is horrible. worst shit ever
    public static <T extends IForgeRegistryEntry<T>> void addWoodRegistrationCallback(
            WoodRegistryCallback<T> registrationFunction, Class<T> regType) {

        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        Consumer<RegistryEvent.Register<T>> eventConsumer;

        if (regType == Block.class || regType == Item.class) {

            //get the queue corresponding to this certain mod
            String modId = ModLoadingContext.get().getActiveContainer().getModId();

            var registrationQueues =
                    LATE_REGISTRATION_QUEUE.computeIfAbsent(modId, s -> {
                        //if absent we register its registration callback
                        bus.addGenericListener(Item.class, EventPriority.HIGHEST, BlockSetHandler::registerModLateBlockAndItems);
                        return Pair.of(new ArrayList<>(), new ArrayList<>());
                    });


            if (regType == Block.class) {
                //if block makes a function that just adds the bus and runnable to the queue whenever reg block is fired
                eventConsumer = e -> {
                    //actual runnable which will registers the blocks
                    Runnable lateRegistration = () -> {

                        IForgeRegistry<?> registry = e.getRegistry();
                        if (registry instanceof ForgeRegistry fr) {
                            boolean frozen = fr.isLocked();
                            fr.unfreeze();
                            registrationFunction.accept(e, WOOD_TYPES.values());
                            if (frozen) fr.freeze();
                        }
                    };
                    //when this reg block event fires we only add a runnable to the queue
                    registrationQueues.getFirst().add(lateRegistration);
                };
                //registering block event to the bus

                bus.addGenericListener(regType, EventPriority.HIGHEST, eventConsumer);
            } else {
                //items just get added to the queue. they will already be called with the correct event

                Consumer<RegistryEvent.Register<Item>> itemEvent = e ->
                        registrationFunction.accept((RegistryEvent.Register<T>) e, WOOD_TYPES.values());
                registrationQueues.getSecond().add(itemEvent);
            }
        } else {
            //non block /item event. just wraps it by giving it the wood types

            eventConsumer = e -> registrationFunction.accept(e, WOOD_TYPES.values());
            bus.addGenericListener(regType, eventConsumer);
        }
    }


    private static void findAllAvailableWoodTypes() {
        Map<ResourceLocation, WoodSetType> map = new LinkedHashMap<>();
        //base oak is always there
        map.put(WoodSetType.OAK_WOOD_TYPE.id, WoodSetType.OAK_WOOD_TYPE);
        for (var b : ForgeRegistries.BLOCKS) {
            getWoodSetType(b).ifPresent(t -> {
                if (!map.containsKey(t.id)) map.put(t.id, t);
            });
        }
        WOOD_TYPES = map;
    }

    private static Optional<WoodSetType> getWoodSetType(Block baseBlock) {
        ResourceLocation baseRes = baseBlock.getRegistryName();
        String name = null;
        String path = baseRes.getPath();
        //needs to contain planks in its name
        if (path.endsWith("_planks")) {
            name = path.substring(0, path.length() - "_planks".length());
        } else if (path.startsWith("planks_")) {
            name = path.substring("planks_".length());
        } else if (path.endsWith("_plank")) {
            name = path.substring(0, path.length() - "_plank".length());
        } else if (path.startsWith("plank_")) {
            name = path.substring("plank_".length());
        }
        if (name != null) {
            BlockState state = baseBlock.defaultBlockState();
            //needs to use wood sound type
            //if (state.getSoundType() == SoundType.WOOD) { //wood from tcon has diff sounds
            Material mat = state.getMaterial();
            //and have correct material
            if (mat == Material.WOOD || mat == Material.NETHER_WOOD) {
                ResourceLocation id = new ResourceLocation(baseRes.getNamespace(), name);
                return Optional.of(new WoodSetType(id, baseBlock));
            }
            //}
        }
        return Optional.empty();
    }

    public enum VariantType {
        BLOCK(Block::new),
        SLAB(SlabBlock::new),
        VERTICAL_SLAB(VerticalSlabBlock::new),
        WALL(WallBlock::new),
        STAIRS(StairBlock::new);
        private final BiFunction<Supplier<BlockState>, BlockBehaviour.Properties, Block> constructor;

        VariantType(BiFunction<Supplier<BlockState>, BlockBehaviour.Properties, Block> constructor) {
            this.constructor = constructor;
        }

        VariantType(Function<BlockBehaviour.Properties, Block> constructor) {
            this.constructor = (b, p) -> constructor.apply(p);
        }

        private Block create(Block parent) {
            return this.constructor.apply(parent::defaultBlockState, BlockBehaviour.Properties.copy(parent));
        }
    }

    /**
     * Utility to register a full block set
     *
     * @param blockRegistry block registry
     * @param itemRegistry  item registry
     * @param baseName      base block name
     * @param parentBlock   base block
     * @return registry object map
     */
    public static EnumMap<VariantType, RegistryObject<Block>> registerFullBlockSet(
            DeferredRegister<Block> blockRegistry, DeferredRegister<Item> itemRegistry,
            String baseName, Block parentBlock, boolean isHidden) {

        EnumMap<VariantType, RegistryObject<Block>> map = new EnumMap<>(VariantType.class);
        for (VariantType type : VariantType.values()) {
            String name = baseName;
            if (!type.equals(VariantType.BLOCK)) name += "_" + type.name().toLowerCase(Locale.ROOT);
            RegistryObject<Block> block = blockRegistry.register(name, () -> type.create(parentBlock));
            CreativeModeTab tab = switch (type) {
                case VERTICAL_SLAB -> !isHidden && ModList.get().isLoaded("quark") ? CreativeModeTab.TAB_BUILDING_BLOCKS : null;
                case WALL -> !isHidden ? CreativeModeTab.TAB_DECORATIONS : null;
                default -> !isHidden ? CreativeModeTab.TAB_BUILDING_BLOCKS : null;
            };
            itemRegistry.register(block.getId().getPath(),
                    () -> new BlockItem(block.get(), (new Item.Properties()).tab(tab)));
            map.put(type, block);
        }
        return map;
    }


}
