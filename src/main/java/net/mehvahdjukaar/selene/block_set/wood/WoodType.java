package net.mehvahdjukaar.selene.block_set.wood;

import net.mehvahdjukaar.selene.Selene;
import net.mehvahdjukaar.selene.block_set.IBlockType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Supplier;

public class WoodType implements IBlockType {

    public static WoodType OAK_WOOD_TYPE = new WoodType(new ResourceLocation("oak"), Blocks.OAK_PLANKS, Blocks.OAK_LOG);

    public final ResourceLocation id;
    public final Material material;
    public final Block planks;
    public final Block log;

    //additional optional blocks
    @Nullable
    public final Block slab;
    @Nullable
    public final Block stairs;
    @Nullable
    public final Block fence;
    @Nullable
    public final Block fenceGate;
    @Nullable
    public final Block button;
    @Nullable
    public final Block pressurePlate;
    @Nullable
    public final Block door;
    @Nullable
    public final Block trapdoor;
    @Nullable
    public final Block strippedLog;
    @Nullable
    public final Block strippedWood;
    @Nullable
    public final Block wood;
    @Nullable
    public final Block leaves;
    //lazy cause wood types are loaded before items, so we can only access blocks
    public final Lazy<Item> signItem; //used for item textures
    public final Lazy<Item> boatItem;

    //remove
    public final String shortenedNamespace;

    protected WoodType(ResourceLocation id, Block baseBlock, Block logBlock) {
        this.id = id;
        this.planks = baseBlock;
        this.log = logBlock;
        this.material = baseBlock.defaultBlockState().getMaterial();
        this.shortenedNamespace = id.getNamespace().equals("minecraft") ? "" : "_" + abbreviateString(id.getNamespace());

        this.leaves = this.findRelatedEntry("leaves", ForgeRegistries.BLOCKS);
        this.strippedLog = this.findRelatedEntry("stripped", "log", ForgeRegistries.BLOCKS);
        this.strippedWood = this.findRelatedEntry("stripped", "wood", ForgeRegistries.BLOCKS);
        this.wood = this.findRelatedEntry("wood", ForgeRegistries.BLOCKS);
        this.slab = this.findRelatedEntry("slab", ForgeRegistries.BLOCKS);
        this.stairs = this.findRelatedEntry("stairs", ForgeRegistries.BLOCKS);
        this.fence = this.findRelatedEntry("fence", ForgeRegistries.BLOCKS);
        this.fenceGate = this.findRelatedEntry("fence_gate", ForgeRegistries.BLOCKS);
        this.door = this.findRelatedEntry("door", ForgeRegistries.BLOCKS);
        this.trapdoor = this.findRelatedEntry("trapdoor", ForgeRegistries.BLOCKS);
        this.button = this.findRelatedEntry("button", ForgeRegistries.BLOCKS);
        this.pressurePlate = this.findRelatedEntry("pressure_plate", ForgeRegistries.BLOCKS);

        //checks if it has a sign
        this.signItem = Lazy.of(() -> this.findRelatedEntry("sign", ForgeRegistries.ITEMS));
        this.boatItem = Lazy.of(() -> this.findRelatedEntry("boat", ForgeRegistries.ITEMS));
    }

    @Override
    public String toString() {
        return this.id.toString();
    }

    @Deprecated
    public String getWoodName() {
        return this.getTypeName();
    }

    public String getTypeName() {
        return id.getPath();
    }

    public String getNamespace() {
        return id.getNamespace();
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    /**
     * @return relatively short id used to append to blocks registryNames
     */
    @Deprecated
    public String getAppendableId() {
        return this.getTypeName() + this.shortenedNamespace;
    }

    /**
     * Use this to get the texture path of a wood type
     *
     * @return something like minecraft/oak
     */
    public String getTexturePath() {
        String namespace = this.getNamespace();
        if (namespace.equals("minecraft")) return this.getTypeName();
        return this.getNamespace() + "/" + this.getTypeName();
    }

    public boolean canBurn() {
        return this.material.isFlammable();
    }

    public MaterialColor getColor() {
        return this.material.getColor();
    }

    @Deprecated
    private static String abbreviateString(String string) {
        if (string.length() <= 5) return string;
        String[] a = string.split("_");
        if (a.length > 2) {
            return "" + a[0].charAt(0) + a[1].charAt(0) + a[2].charAt(0) + (a.length > 3 ? a[3].charAt(0) : "");
        } else if (a.length > 1) {
            return "" + a[0].substring(0, Math.min(2, a[0].length())) + a[1].substring(0, Math.min(2, a[0].length()));
        } else return string.substring(0, 4);
    }

    @Override
    public String getTranslationKey() {
        return "wood_type." + this.getTypeName().replace("/",".");
    }

    public static class Finder extends SetFinder<WoodType> {

        private final Supplier<Block> planksFinder;
        private final Supplier<Block> logFinder;
        private final ResourceLocation id;

        public Finder(ResourceLocation id, Supplier<Block> planks, Supplier<Block> log) {
            this.id = id;
            this.planksFinder = planks;
            this.logFinder = log;
        }

        public static Finder simple(String modId, String woodTypeName, String planksName, String logName) {
            return simple(new ResourceLocation(modId, woodTypeName), new ResourceLocation(modId, planksName), new ResourceLocation(modId, logName));
        }

        public static Finder simple(ResourceLocation woodTypeName, ResourceLocation planksName, ResourceLocation logName) {
            return new Finder(woodTypeName,
                    () -> ForgeRegistries.BLOCKS.getValue(planksName),
                    () -> ForgeRegistries.BLOCKS.getValue(logName));
        }

        public Optional<WoodType> get() {
            if (ModList.get().isLoaded(id.getNamespace())) {
                try {
                    Block plank = planksFinder.get();
                    Block log = logFinder.get();
                    var d = ForgeRegistries.BLOCKS.getValue(ForgeRegistries.BLOCKS.getDefaultKey());
                    if (plank != d && log != d && plank != null && log != null) {
                        return Optional.of(new WoodType(id, plank, log));
                    }
                } catch (Exception ignored) {
                }
                Selene.LOGGER.warn("Failed to find custom wood type {}", id);
            }
            return Optional.empty();
        }
    }


}
