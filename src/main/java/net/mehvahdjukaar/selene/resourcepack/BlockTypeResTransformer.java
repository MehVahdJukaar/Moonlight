package net.mehvahdjukaar.selene.resourcepack;

import net.mehvahdjukaar.selene.block_set.BlockType;
import net.mehvahdjukaar.selene.block_set.leaves.LeavesType;
import net.mehvahdjukaar.selene.block_set.wood.WoodType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.Nullable;

import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An object used to transform existing json resources to use new block types. Basically a fancy string replacement tool
 *
 * @param <T>
 */
public class BlockTypeResTransformer<T extends BlockType> {

    @FunctionalInterface
    public interface TextModification<T extends BlockType> extends TriFunction<String, ResourceLocation, T, String> {
        @Override
        String apply(String originalText, ResourceLocation blockId, T type);
    }

    private final ResourceManager manager;
    private final String modId;

    private final List<TextModification<T>> textModifiers = new ArrayList<>();
    private TextModification<T> idModifiers = (s, id, w) -> s;

    private BlockTypeResTransformer(String modId, ResourceManager manager) {
        this.manager = manager;
        this.modId = modId;
    }

    public static <T extends BlockType> BlockTypeResTransformer<T> create(String modId, ResourceManager manager) {
        return new BlockTypeResTransformer<>(modId, manager);
    }

    public static BlockTypeResTransformer<WoodType> wood(String modId, ResourceManager manager) {
        return new BlockTypeResTransformer<>(modId, manager);
    }

    public static BlockTypeResTransformer<LeavesType> leaves(String modId, ResourceManager manager) {
        return new BlockTypeResTransformer<>(modId, manager);
    }


    public BlockTypeResTransformer<T> setIDModifier(TextModification<T> modifier) {
        this.idModifiers = modifier;
        return this;
    }

    public BlockTypeResTransformer<T> IDReplaceType(String oldTypeName) {
        return setIDModifier((s, id, w) -> {
            return replaceType(s, id, w, oldTypeName);
        });
    }


    public BlockTypeResTransformer<T> IDReplaceBlock(String blockName) {
        return setIDModifier((s, id, w) -> s.replace(blockName, id.getPath()));
    }


    /**
     * Add generic modifier
     */
    public BlockTypeResTransformer<T> addModifier(TextModification<T> modifier) {
        this.textModifiers.add(modifier);
        return this;
    }

    /**
     * Add generic modifier
     */
    public BlockTypeResTransformer<T> addModifier(BiFunction<String, T, String> genericModifier) {
        return this.addModifier((s, id, w) -> genericModifier.apply(s, w));
    }

    public BlockTypeResTransformer<T> replaceBlockType(String oldTypeName) {
        this.addModifier((s, id, w) -> {
            return replaceFullBlockType(s, id, w, oldTypeName, modId);
        });
        return this;
    }


    /**
     * Replaces the provided block description with the equivalent of our own target block
     */
    public BlockTypeResTransformer<T> replaceSimpleBlock(Block block) {
        ResourceLocation res = block.getRegistryName();
        return replaceSimpleBlock(res.getNamespace(), res.getPath());
    }

    public BlockTypeResTransformer<T> replaceSimpleBlock(String blockNamespace, String blockName) {
        return replaceSimpleBlock(blockNamespace, blockName, ":block/", ":block/");
    }

    public BlockTypeResTransformer<T> replaceSimpleBlock(String blockNamespace, String blockName, String inBetween, String inBetween2) {
        return this.addModifier((s, id, w) -> s.replace(blockNamespace + inBetween + blockName,
                id.getNamespace() + inBetween2 + id.getPath()));
    }

    /**
     * Simple string replacement
     */
    public BlockTypeResTransformer<T> replaceString(String from, String to) {
        return this.addModifier((s, id, w) -> s.replace(from, to));
    }

    public BlockTypeResTransformer<T> replaceOakLeaves() {
        return this.replaceWithTextureFromChild("minecraft:block/oak_leaves", "leaves", s -> !s.contains("_snow"));
    }

    /**
     * Replaces the oak planks texture with the plank texture of the 'planks' child of this block type. Meant for wood types
     */
    public BlockTypeResTransformer<T> replaceOakPlanks() {
        return this.replaceWithTextureFromChild("minecraft:block/oak_planks", "planks");
    }

    /**
     * Replaces the oak log textures with the log texture of the 'log' child of this block type. Meant for wood types
     */
    public BlockTypeResTransformer<T> replaceOakBark() {
        return this.replaceWithTextureFromChild("minecraft:block/oak_log", "log", s -> !s.contains("_end") && !s.contains("_top"))
                .replaceWithTextureFromChild("minecraft:block/oak_log_top", "log", s -> s.contains("_top") || s.contains("_end"));
    }

    public BlockTypeResTransformer<T> replaceOakStripped() {
        return this.replaceWithTextureFromChild("minecraft:block/stripped_oak_log", "stripped_log", s -> !s.contains("_end") && !s.contains("_top"))
                .replaceWithTextureFromChild("minecraft:block/stripped_oak_log_top", "stripped_log", s -> s.contains("_top") || s.contains("_end"));
    }

    public BlockTypeResTransformer<T> replaceWoodTextures(WoodType woodType) {
        String n = woodType.getTypeName();
        return this.replaceWithTextureFromChild("minecraft:block/" + n + "_planks", "planks")
                .replaceWithTextureFromChild("minecraft:block/stripped_" + n + "_log", "stripped_log", s -> !s.contains("_end") && !s.contains("_top"))
                .replaceWithTextureFromChild("minecraft:block/stripped_" + n + "_log_top", "stripped_log", s -> s.contains("_top") || s.contains("_end"))
                .replaceWithTextureFromChild("minecraft:block/" + n + "_log", "log", s -> !s.contains("_end") && !s.contains("_top"))
                .replaceWithTextureFromChild("minecraft:block/" + n + "_log_top", "log", s -> s.contains("_top") || s.contains("_end"));

    }

    public BlockTypeResTransformer<T> replaceLeavesTextures(LeavesType woodType) {
        String n = woodType.getTypeName();
        return this.replaceWithTextureFromChild("minecraft:block/" + n + "_leaves", "leaves", s -> !s.contains("_snow"))
                .replaceWithTextureFromChild("minecraft:block/stripped_" + n + "_log", l -> wfl(l, "stripped_log"), s -> !s.contains("_end") && !s.contains("_top"))
                .replaceWithTextureFromChild("minecraft:block/stripped_" + n + "_log_top", l -> wfl(l, "stripped_log"), s -> s.contains("_top") || s.contains("_end"))
                .replaceWithTextureFromChild("minecraft:block/" + n + "_log", l -> wfl(l, "log"), s -> !s.contains("_end") && !s.contains("_top"))
                .replaceWithTextureFromChild("minecraft:block/" + n + "_log_top", l -> wfl(l, "log"), s -> s.contains("_top") || s.contains("_end"));

    }

    private @Nullable ItemLike wfl(T t, String s) {
        if (t instanceof LeavesType l && l.woodType != null) return l.woodType.getChild(s);
        return null;
    }

    public BlockTypeResTransformer<T> replaceWithTextureFromChild(String target, String textureFromChild) {
        return replaceWithTextureFromChild(target, textureFromChild, s -> true);
    }

    public BlockTypeResTransformer<T> replaceWithTextureFromChild(String target, String textureFromChild,
                                                                  Predicate<String> texturePredicate) {
        return replaceWithTextureFromChild(target, w -> w.getChild(textureFromChild), texturePredicate);
    }

    public BlockTypeResTransformer<T> replaceWithTextureFromChild(String target, Function<T, ItemLike> childProvider,
                                                                  Predicate<String> texturePredicate) {

        return this.addModifier((s, id, w) -> {
            String r = s;
            try {
                ItemLike woodObject = childProvider.apply(w);
                ResourceLocation newTexture = null;
                if (woodObject instanceof Block b) {
                    newTexture = RPUtils.findFirstBlockTextureLocation(manager, b, texturePredicate);
                } else if (woodObject instanceof Item i) {
                    newTexture = RPUtils.findFirstItemTextureLocation(manager, i);
                }
                if (newTexture != null) {
                    //try mc namespace
                    r = s.replace("\"block/", "\"minecraft:block/");

                    r = r.replace("\"" + target + "\"", "\"" + newTexture + "\"");
                }
            } catch (FileNotFoundException ignored) {
            }
            return r;
        });
    }

    /**
     * @param resource resource template to transform
     * @param blockId  id of the block that this is for
     * @param type     block type of the target block
     * @return new resource
     */
    public StaticResource transform(StaticResource resource, ResourceLocation blockId, T type) {
        String newText = new String(resource.data, StandardCharsets.UTF_8);

        for (var m : textModifiers) {
            newText = m.apply(newText, blockId, type);
        }
        ResourceLocation oldPath = resource.location;

        String id = idModifiers.apply(oldPath.getPath(), blockId, type);
        ResourceLocation newLocation = new ResourceLocation(blockId.getNamespace(), id);

        return StaticResource.create(newText.getBytes(), newLocation);
    }

    public static String replaceType(String original, ResourceLocation blockId, BlockType blockType, String oldTypeName) {
        String prefix = "";
        Pattern pattern = Pattern.compile("(.*(?=\\/))");
        Matcher matcher = pattern.matcher(blockId.getPath());
        if (matcher.find()) prefix = "/" + matcher.group(1); //c/create/
        Pattern p2;
        if (original.contains("block/")) { ///block(/b/cc_)oak
            //needed so stuff matches the same as replaceFullBlockType
            p2 = Pattern.compile("((?<=block)[\\w\\/]*?)" + oldTypeName);
        } else p2 = Pattern.compile("(\\/\\w*?)" + oldTypeName); ///a/b(/cc_)oak
        Matcher m2 = p2.matcher(original);//->sup:block
        String finalPrefix = prefix;
        String newS = m2.replaceAll(m -> finalPrefix + m.group(1) + blockType.getTypeName());
        return newS;
    }

    //more sstrict version of the one above. specifically targets blocks
    public static String replaceFullBlockType(String original, ResourceLocation blockId, BlockType blockType, String oldTypeName, String oldId) {
        String prefix = "";
        Pattern pattern = Pattern.compile("(.*(?=\\/))");
        Matcher matcher = pattern.matcher(blockId.getPath());
        if (matcher.find()) prefix = "/" + matcher.group(1); //c/create/
        Pattern p2 = Pattern.compile(oldId + ":block(.*\\/.*)" + oldTypeName); //create:block(/a/b/cc_)oak
        Matcher m2 = p2.matcher(original);//->sup:block
        String finalPrefix = prefix;
        return m2.replaceAll(m -> blockId.getNamespace() + ":block" + finalPrefix + m.group(1) + blockType.getTypeName());
    }

}