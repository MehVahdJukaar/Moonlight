package net.mehvahdjukaar.selene.util;

import net.mehvahdjukaar.selene.resourcepack.AssetGenerators;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.stream.Collectors;

public class WoodSetType {

    public static WoodSetType OAK_WOOD_TYPE = new WoodSetType(new ResourceLocation("oak"), Blocks.OAK_PLANKS);

    public final ResourceLocation id;
    public final Material material;
    public final Block plankBlock;
    //if true then this woodtype probably shouldn't have other blocks assigned to it
    public final String shortenedNamespace;


    @Nullable
    public final Block logBlock; //used for log texture
    @Nullable
    public final Item signItem; //used for item textures

    protected WoodSetType(ResourceLocation id, Block baseBlock) {
        this.id = id;
        this.plankBlock = baseBlock;
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

        //checks if it has a sign
        ResourceLocation[] test2 = {
                new ResourceLocation(id.getNamespace(), id.getPath() + "_sign"),
                new ResourceLocation(id.getNamespace(), "sign_" + id.getPath())
        };
        Item temp2 = null;
        for (var r : test2) {
            if (ForgeRegistries.ITEMS.containsKey(r)) {
                temp2 = ForgeRegistries.ITEMS.getValue(r);
                break;
            }
        }
        this.signItem = temp2;
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


    private static String abbreviateString(String string) {
        if (string.length() <= 5) return string;
        String[] a = string.split("_");
        if (a.length > 2) {
            return "" + a[0].charAt(0) + a[1].charAt(0) + a[2].charAt(0) + (a.length > 3 ? a[3].charAt(0) : "");
        } else if (a.length > 1) {
            return "" + a[0].substring(0, Math.min(2, a[0].length())) + a[1].substring(0, Math.min(2, a[0].length()));
        } else return string.substring(0, 4);
    }
}
