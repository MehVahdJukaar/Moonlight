package net.mehvahdjukaar.selene.util;

import net.mehvahdjukaar.selene.resourcepack.AssetGenerators;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class WoodSetType {

    public static WoodSetType OAK_WOOD_TYPE = new WoodSetType(new ResourceLocation("oak"), Blocks.OAK_PLANKS,Blocks.OAK_LOG);

    public final ResourceLocation id;
    public final Material material;
    public final Block plankBlock;
    public final String shortenedNamespace;

    @Nullable
    public final Block logBlock; //used for log texture
    //lazy cause wood types are loaded before items so we can only access blocks
    @Nullable
    public final Lazy<Item> signItem; //used for item textures

    protected WoodSetType(ResourceLocation id, Block baseBlock,Block logBlock) {
        this.id = id;
        this.plankBlock = baseBlock;
        this.logBlock = logBlock;
        this.material = baseBlock.defaultBlockState().getMaterial();
        this.shortenedNamespace = id.getNamespace().equals("minecraft") ? "" : "_" + abbreviateString(id.getNamespace());

        //checks if it has a sign
        this.signItem = Lazy.of(this::findSign);
    }

    @Nullable
    private Item findSign() {
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
        return temp2;
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

    @Nullable
    private static Block findLog(ResourceLocation id) {
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
        return temp;
    }

    //returns if this block is the base plank block
    public static Optional<WoodSetType> getWoodTypeFromBlock(Block baseBlock) {
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
                Block logBlock = findLog(id);
                if(logBlock!=null) {
                    return Optional.of(new WoodSetType(id, baseBlock, logBlock));
                }
            }
            //}
        }
        return Optional.empty();
    }
}
