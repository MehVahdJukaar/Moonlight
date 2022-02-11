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
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.RegistryObject;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

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

    public static void onModSetup() {
        if (ForgeRegistries.BLOCKS.getValues().size() > totalBlockRegistered) {
            Selene.LOGGER.error("Conditional registration didn't run last. This might have cause it to miss some wood types.");
        }
    }



    @FunctionalInterface
    public interface WoodRegistryCallback<T extends IForgeRegistryEntry<T>> {
        void accept(RegistryEvent.Register<T> reg, Collection<WoodSetType> wood);
    }

    private static int totalBlockRegistered = 0;

    //Fires after the block registration one. We use it to first gather all wood types, then register out blocks here
    //hopefully it should not cause problems
    public static void detectWoodTypes(RegistryEvent.Register<Item> event) {

    }

    private static final List<WoodRegistryCallback<Block>> LATE_BLOCK_REGISTRATIONS = new ArrayList<>();

    /**
     * Add a registry function meant to register a set of blocks that use a specific wood type
     * Other entries like items can access WOOD_TYPES directly since it will be filled
     * Will be called (hopefully) after all other block registrations have been fired so the wood set type is complete
     * IMPORTANT: your mod needs to be set to run AFTER this mod. You can sed it in your mods.toml dependency
     * If you dont this will still work but registration will throw some warnings
     *
     * @param registrationFunction registry function
     */
    public static <T extends IForgeRegistryEntry<T>> void addWoodRegistrationCallback(WoodRegistryCallback<T> registrationFunction, Class<T> regType) {


        //event consumer
        Consumer<RegistryEvent.Register<T>> consumer = e -> {
            if (!hasFilledWoodTypes) {
                findAllAvailableWoodTypes();
                hasFilledWoodTypes = true;
            }
            registrationFunction.accept(e, WOOD_TYPES.values());
            //sanity check to verity that this indeed run last
            if (regType == Block.class) {
                totalBlockRegistered = ForgeRegistries.BLOCKS.getValues().size();
            }
        };

        //so I'm sure it gets added to this mod bus
        //bus consumer

        Selene.enqueueLateBusWork(b -> b.addGenericListener(regType, EventPriority.LOWEST, consumer));

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
            if (state.getSoundType() == SoundType.WOOD || baseRes.equals("tconstruct")) { //wood from tcon has diff sounds
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
        public final String shortenedNamespace;
        @Nullable
        public final Block logBlock;

        protected WoodSetType(ResourceLocation id, Block baseBlock) {
            this.id = id;
            this.baseBlock = baseBlock;
            this.material = baseBlock.defaultBlockState().getMaterial();
            this.shortenedNamespace = id.getNamespace().equals("minecraft") ? "" : "_" + abbreviateString(id.getNamespace());
            //check if it has its log
            ResourceLocation[] test = {
                    new ResourceLocation(id.getNamespace(), id.getPath() + "_log"),
                    new ResourceLocation(id.getNamespace(), "log_" + id.getPath()),
                    new ResourceLocation(id.getPath() + "_log"),
                    new ResourceLocation("log_" + id.getPath()),
                    new ResourceLocation(id.getNamespace(), id.getPath() + "_stem"),
                    new ResourceLocation(id.getNamespace(), "stem_" + id.getPath()),
                    new ResourceLocation(id.getPath() + "_stem"),
                    new ResourceLocation("stem_" + id.getPath())
            };
            Block temp = null;
            for (var r : test) {
                if (ForgeRegistries.BLOCKS.containsKey(r)) {
                    temp = ForgeRegistries.BLOCKS.getValue(r);
                    break;
                }
            }
            this.logBlock = temp;
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
        @Deprecated
        public String getAppendableId() {
            return this.getWoodName() + this.shortenedNamespace;
        }

        /**
         * Use this to get the new id of a block variant
         *
         * @param baseName base variant name
         * @return something like mod_id/[baseName]_oak. ignores minecraft namespace
         */
        public String getVariantId(String baseName) {
            String namespace = this.getNamespace();
            if (namespace.equals("minecraft")) return baseName + "_" + this.getWoodName();
            return this.getNamespace() + "/" + baseName + "_" + this.getWoodName();
        }

        /**
         * Use this to get the texture path of a wood type
         *
         * @param baseName base variant name
         * @return something like minecraft/oak
         */
        public String getTexturePath() {
            String namespace = this.getNamespace();
            if (namespace.equals("minecraft")) return this.getWoodName();
            return this.getNamespace() + "/" + this.getWoodName();
        }

        /**
         * @return True if this wood type should probably have wood items registered to
         * Simply checks if a log type with the same name exists. Should cover most cases
         */
        public boolean shouldHaveBlockSet() {
            return this.logBlock != null;
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

    private static String abbreviateString(String string) {
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
