package net.mehvahdjukaar.moonlight.api.resources;

import net.mehvahdjukaar.moonlight.api.misc.TriFunction;
import net.mehvahdjukaar.moonlight.api.resources.textures.SpriteUtils;
import net.mehvahdjukaar.moonlight.api.set.BlockType;
import net.mehvahdjukaar.moonlight.api.set.leaves.LeavesType;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
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
        return setIDModifier((s, id, w) -> replaceTypeNoNamespace(s, w, id, oldTypeName));
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

    public BlockTypeResTransformer<T> replaceSimpleType(String oldTypeName) {
        return addModifier((s, id, w) -> replaceType(s, w, id, oldTypeName, modId));
    }

    public BlockTypeResTransformer<T> replaceGenericType(String oldTypeName, String entryClass) {
        this.addModifier((s, id, w) -> replaceFullGenericType(s, w, id, oldTypeName, modId, entryClass));
        return this;
    }

    public BlockTypeResTransformer<T> replaceBlockType(String oldTypeName) {
        this.addModifier((s, id, w) -> replaceFullGenericType(s, w, id, oldTypeName, modId, "block"));
        return this;
    }

    public BlockTypeResTransformer<T> replaceItemType(String oldTypeName) {
        this.addModifier((s, id, w) -> replaceFullGenericType(s, w, id, oldTypeName, modId, "item"));
        return this;
    }


    /**
     * Simple string replacement
     */
    public BlockTypeResTransformer<T> replaceString(String from, String to) {
        return this.addModifier((s, id, w) -> s.replace(from, to));
    }

    public BlockTypeResTransformer<T> replaceOakLeaves() {
        return this.replaceWithTextureFromChild("minecraft:block/oak_leaves", "leaves", s -> {
            return !s.contains("_snow") && !s.contains("snow_") && !s.contains("snowy_");
        });
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
        return this.replaceWithTextureFromChild("minecraft:block/oak_log", "log", SpriteUtils.LOOKS_LIKE_SIDE_LOG_TEXTURE)
                .replaceWithTextureFromChild("minecraft:block/oak_log_top", "log", SpriteUtils.LOOKS_LIKE_TOP_LOG_TEXTURE);
    }

    public BlockTypeResTransformer<T> replaceOakStripped() {
        return this.replaceWithTextureFromChild("minecraft:block/stripped_oak_log", "stripped_log", SpriteUtils.LOOKS_LIKE_SIDE_LOG_TEXTURE)
                .replaceWithTextureFromChild("minecraft:block/stripped_oak_log_top", "stripped_log", SpriteUtils.LOOKS_LIKE_TOP_LOG_TEXTURE);
    }

    public BlockTypeResTransformer<T> replaceWoodTextures(WoodType woodType) {
        String n = woodType.getTypeName();
        return this.replaceWithTextureFromChild("minecraft:block/" + n + "_planks", "planks")
                .replaceWithTextureFromChild("minecraft:block/stripped_" + n + "_log", "stripped_log", SpriteUtils.LOOKS_LIKE_SIDE_LOG_TEXTURE)
                .replaceWithTextureFromChild("minecraft:block/stripped_" + n + "_log_top", "stripped_log", SpriteUtils.LOOKS_LIKE_TOP_LOG_TEXTURE)
                .replaceWithTextureFromChild("minecraft:block/" + n + "_log", "log", SpriteUtils.LOOKS_LIKE_SIDE_LOG_TEXTURE)
                .replaceWithTextureFromChild("minecraft:block/" + n + "_log_top", "log", SpriteUtils.LOOKS_LIKE_TOP_LOG_TEXTURE);

    }

    public BlockTypeResTransformer<T> replaceLeavesTextures(LeavesType woodType) {
        String n = woodType.getTypeName();
        return this.replaceWithTextureFromChild("minecraft:block/" + n + "_leaves", "leaves", SpriteUtils.LOOKS_LIKE_LEAF_TEXTURE)
                .replaceWithTextureFromChild("minecraft:block/stripped_" + n + "_log", l -> wfl(l, "stripped_log"), SpriteUtils.LOOKS_LIKE_SIDE_LOG_TEXTURE)
                .replaceWithTextureFromChild("minecraft:block/stripped_" + n + "_log_top", l -> wfl(l, "stripped_log"), SpriteUtils.LOOKS_LIKE_TOP_LOG_TEXTURE)
                .replaceWithTextureFromChild("minecraft:block/" + n + "_log", l -> wfl(l, "log"), SpriteUtils.LOOKS_LIKE_SIDE_LOG_TEXTURE)
                .replaceWithTextureFromChild("minecraft:block/" + n + "_log_top", l -> wfl(l, "log"), SpriteUtils.LOOKS_LIKE_TOP_LOG_TEXTURE);

    }

    private @Nullable ItemLike wfl(T t, String s) {
        if (t instanceof LeavesType l && l.getWoodType() != null) {
            var c = l.getWoodType().getChild(s);
            return c instanceof ItemLike il ? il : null;
        }
        return null;
    }

    public BlockTypeResTransformer<T> replaceWithTextureFromChild(String target, String textureFromChild) {
        return replaceWithTextureFromChild(target, textureFromChild, s -> true);
    }

    public BlockTypeResTransformer<T> replaceWithTextureFromChild(String target, String textureFromChild,
                                                                  Predicate<String> texturePredicate) {
        return replaceWithTextureFromChild(target, w -> (ItemLike) w.getChild(textureFromChild), texturePredicate);
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
        ResourceLocation newLocation = blockId.withPath(id);

        return StaticResource.create(newText.getBytes(), newLocation);
    }

    public static String replaceTypeNoNamespace(String text, BlockType blockType, ResourceLocation blockId, String oldTypeName) {
        return replaceFullGenericType(text, blockType, blockId, oldTypeName, null, "");
    }

    public static String replaceType(String text, BlockType blockType, ResourceLocation blockId, String oldTypeName, String oldNamespace) {
        return replaceFullGenericType(text, blockType, blockId, oldTypeName, oldNamespace, "");
    }

    /**
     * Specifically targets the whole block/ item string and replaces it whole with a new one
     *
     * @param classType    Registry entry type. E.G: "item" or "block"
     * @param text         Text to apply this replacement to
     * @param blockType    wood or leaf type that this new block has
     * @param blockId      new block id to replace this entry with
     * @param oldTypeName  original block type. E.G. "oak"
     * @param oldNamespace original namespace of this entry. E.G. "quark"
     */

    //this is terrible
    public static String replaceFullGenericType(String text, BlockType blockType, ResourceLocation blockId, String oldTypeName, String oldNamespace, String classType) {

        String prefix = "";
        Pattern pattern = Pattern.compile("([^,]*(?=\\/))");
        Matcher matcher = pattern.matcher(blockId.getPath());
        if (matcher.find()) prefix = matcher.group(1); //c/merge/

        if (oldNamespace == null) {
            //simple mode
            Pattern p2;
            if (text.contains("block/")) { ///block(/b/cc_)oak
                //needed so stuff matches the same as replaceFullBlockType
                p2 = Pattern.compile("((?<=block)[\\w\\/]*?)" + oldTypeName);
            } else p2 = Pattern.compile("(\\/\\w*?)" + oldTypeName); ///a/b(/cc_)oak
            Matcher m2 = p2.matcher(text);//->sup:block
            String finalPrefix = prefix;
            return m2.replaceAll(m -> "/" + finalPrefix + m.group(1) + blockType.getTypeName());

        } else {
            Pattern p2;
            if (classType.isEmpty()) {
                p2 = Pattern.compile(oldNamespace + ":" + "([^,]*?)" + oldTypeName); //merge:block(/a/b/cc_)oak //.*
                if (!prefix.isEmpty()) prefix = prefix + "/";
            } else {
                prefix = "/" + prefix;
                p2 = Pattern.compile(oldNamespace + ":" + classType + "([^,]*?\\/[^,]*?)" + oldTypeName); //merge:block(/a/b/cc_)oak
            }
            Matcher m2 = p2.matcher(text);//->sup:block
            String finalPrefix = prefix;
            return m2.replaceAll(m -> blockId.getNamespace() + ":" + classType + finalPrefix + m.group(1) + blockType.getTypeName());
        }
    }
}