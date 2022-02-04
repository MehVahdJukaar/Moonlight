package net.mehvahdjukaar.selene.util;

import net.mehvahdjukaar.selene.Selene;
import net.mehvahdjukaar.selene.blocks.VerticalSlabBlock;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.RegistryObject;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class BlockSetHandler {

    /**
     * Do not access these to register your blocks since they are empty right before the last registration phase.
     * Use addWoodEntryRegistrationCallback instead
     */
    public static Map<ResourceLocation, WoodSetType> WOOD_TYPES = new HashMap<>();
    public static WoodSetType OAK_WOOD_TYPE = new WoodSetType(new ResourceLocation("oak"), Blocks.OAK_PLANKS);
    private static boolean hasFilledWoodTypes = false;

    /**
     * Gets corresponding wood type or oak if the provided one is not installed or missing
     *
     * @param name string resource location name of the type
     * @return wood type
     */
    public static WoodSetType getWoodTypeFromNBT(String name) {
        return WOOD_TYPES.getOrDefault(new ResourceLocation(name), OAK_WOOD_TYPE);
    }

    private static final Map<Class<? extends IForgeRegistryEntry<?>>, List<WoodRegistryCallback<?>>>
            CALLBACKS = new HashMap<>();

    public static void init() {
        CALLBACKS.clear();
    }

    @FunctionalInterface
    public interface WoodRegistryCallback<T extends IForgeRegistryEntry<T>> {
        void accept(RegistryEvent.Register<T> reg, Collection<WoodSetType> wood);
    }


    /**
     * Add a registry function meant to register a set of blocks that use a specific wood type
     * Other entries like items can access WOOD_TYPES directly since it will be filled
     *
     * @param registrationFunction registry function
     */
    public static <T extends IForgeRegistryEntry<T>> void addWoodRegistrationCallback(WoodRegistryCallback<T> registrationFunction, Class<T> regType) {

        if (!CALLBACKS.containsKey(regType)) {
            CALLBACKS.put(regType, new ArrayList<>());

            //if new registry type is added also register its event, so it gets called

            //Ugliest shit ever lol
            Consumer<RegistryEvent.Register<T>> consumer = e -> {
                if (!hasFilledWoodTypes) {
                    findAllAvailableWoodTypes();
                    hasFilledWoodTypes = true;
                }
                var ls = CALLBACKS.get(regType);
                for (var c : ls) {
                    ((WoodRegistryCallback<T>) c).accept(e, WOOD_TYPES.values());
                }
            };

            //so I'm sure it gets added to this mod bus
            Selene.MOD_BUS.addGenericListener(regType, EventPriority.LOWEST, consumer);
        }
        var list = CALLBACKS.get(regType);

        list.add(registrationFunction);
    }


    private static void findAllAvailableWoodTypes() {
        Map<ResourceLocation, WoodSetType> map = new LinkedHashMap<>();
        //base oak is always there
        map.put(OAK_WOOD_TYPE.id, OAK_WOOD_TYPE);
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
        }
        if (name != null) {
            BlockState state = baseBlock.defaultBlockState();
            //needs to use wood sound type
            if (state.getSoundType() == SoundType.WOOD) {
                Material mat = state.getMaterial();
                //and have correct material
                if (mat == Material.WOOD || mat == Material.NETHER_WOOD) {
                    ResourceLocation id = new ResourceLocation(baseRes.getNamespace(), name);
                    return Optional.of(new WoodSetType(id, baseBlock));
                }
            }
        }
        return Optional.empty();
    }

    public static class WoodSetType {
        public final ResourceLocation id;
        public final Material material;
        public final Block baseBlock;
        //if true then this woodtype probably shouldn't have other blocks assigned to it
        public final boolean hasFence;
        public final String shortenedNamespace;

        protected WoodSetType(ResourceLocation id, Block baseBlock) {
            this.id = id;
            this.baseBlock = baseBlock;
            this.material = baseBlock.defaultBlockState().getMaterial();
            String[] test = {id.toString() + "_fence", id.toString() + "_planks_fence", id.toString() + "_plank_fence"};
            this.hasFence = Arrays.stream(test).anyMatch(s -> ForgeRegistries.BLOCKS.containsKey(new ResourceLocation(s)));
            this.shortenedNamespace = id.getNamespace().equals("minecraft") ? "" : "_" + getAbbreviation(id.getNamespace());
        }

        @Override
        public String toString() {
            return this.id.toString();
        }

        public String getWoodName() {
            return id.getPath();
        }

        public String getNamespace() {
            return id.getNamespace();
        }

        public String getNameForTranslation(String append) {
            //There's got to be a faster method call lol
            return Arrays.stream((this.getWoodName() + "_" + append).split("_"))
                    .map(StringUtils::capitalize).collect(Collectors.joining(" "));
        }

        /**
         * @return relatively short id used to append to blocks registryNames
         */
        public String getAppendableId() {
            return this.getWoodName() + this.shortenedNamespace;
        }

        public boolean shouldHaveBlockSet() {
            return this.hasFence;
        }

        public boolean canBurn() {
            return this.material.isFlammable();
        }

        public MaterialColor getColor() {
            return this.material.getColor();
        }

        public boolean isVanilla() {
            return this.getNamespace().equals("minecraft");
        }

    }

    private static String getAbbreviation(String string) {
        if (string.length() <= 5) return string;
        String[] a = string.split("_");
        if (a.length > 2) {
            return "" + a[0].charAt(0) + a[1].charAt(0) + a[2].charAt(0) + (a.length > 3 ? a[3].charAt(0) : "");
        } else if (a.length > 1) {
            return "" + a[0].substring(0, Math.min(2, a[0].length())) + a[1].substring(0, Math.min(2, a[0].length()));
        } else return string.substring(0, 4);
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
