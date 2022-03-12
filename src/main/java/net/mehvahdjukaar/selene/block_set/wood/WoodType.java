package net.mehvahdjukaar.selene.block_set.wood;

import net.mehvahdjukaar.selene.Selene;
import net.mehvahdjukaar.selene.block_set.IBlockType;
import net.mehvahdjukaar.selene.resourcepack.AssetGenerators;
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
    public final Block plankBlock;
    public final Block logBlock;
    //lazy cause wood types are loaded before items, so we can only access blocks
    @Nullable
    public final Lazy<Item> signItem; //used for item textures
    @Nullable
    public final Lazy<Item> boatItem;

    //remove
    public final String shortenedNamespace;

    protected WoodType(ResourceLocation id, Block baseBlock, Block logBlock) {
        this.id = id;
        this.plankBlock = baseBlock;
        this.logBlock = logBlock;
        this.material = baseBlock.defaultBlockState().getMaterial();
        this.shortenedNamespace = id.getNamespace().equals("minecraft") ? "" : "_" + abbreviateString(id.getNamespace());

        //checks if it has a sign
        this.signItem = Lazy.of(()->this.findRelatedItem("sign"));
        this.boatItem = Lazy.of(()->this.findRelatedItem("boat"));
    }

    @Nullable
    private Item findRelatedItem(String appendedName) {
        ResourceLocation[] targets = {
                new ResourceLocation(id.getNamespace(), id.getPath() + "_" + appendedName),
                new ResourceLocation(id.getNamespace(), appendedName + "_" + id.getPath())
        };
        Item found = null;
        for (var r : targets) {
            if (ForgeRegistries.ITEMS.containsKey(r)) {
                found = ForgeRegistries.ITEMS.getValue(r);
                break;
            }
        }
        return found;
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
        return AssetGenerators.LangBuilder.getReadableName(this.getWoodName() + "_" + append);
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
     * @return something like minecraft/oak
     */
    public String getTexturePath() {
        String namespace = this.getNamespace();
        if (namespace.equals("minecraft")) return this.getWoodName();
        return this.getNamespace() + "/" + this.getWoodName();
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


    private static String abbreviateString(String string) {
        if (string.length() <= 5) return string;
        String[] a = string.split("_");
        if (a.length > 2) {
            return "" + a[0].charAt(0) + a[1].charAt(0) + a[2].charAt(0) + (a.length > 3 ? a[3].charAt(0) : "");
        } else if (a.length > 1) {
            return "" + a[0].substring(0, Math.min(2, a[0].length())) + a[1].substring(0, Math.min(2, a[0].length()));
        } else return string.substring(0, 4);
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
            return new Finder(new ResourceLocation(modId, woodTypeName),
                    () -> ForgeRegistries.BLOCKS.getValue(new ResourceLocation(modId, planksName)),
                    () -> ForgeRegistries.BLOCKS.getValue(new ResourceLocation(modId, logName)));
        }

        public Optional<WoodType> get() {
            if (ModList.get().isLoaded(id.getNamespace())) {
                try {
                    Block plank = planksFinder.get();
                    Block log = logFinder.get();
                    return Optional.of(new WoodType(id, plank, log));
                } catch (Exception e) {
                    Selene.LOGGER.warn("Failed to find custom wood type {}", id);
                }
            }
            return Optional.empty();
        }

    }
}
