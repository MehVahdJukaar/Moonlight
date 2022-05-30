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

import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * An object used to transform existing json resources to use new block types. Basically a fancy string replacement tool
 *
 * @param <T>
 */
public class BlockTypeResourceTransform<T extends BlockType> {

    @FunctionalInterface
    public interface WoodTextModifier<T extends BlockType> extends TriFunction<String, ResourceLocation, T, String> {
        @Override
        String apply(String originalText, ResourceLocation blockId, T type);
    }

    private final ResourceManager manager;
    private final String modId;

    private final List<WoodTextModifier<T>> textModifiers = new ArrayList<>();
    private WoodTextModifier<T> idModifiers = (s, id, w) -> s;

    private BlockTypeResourceTransform(String modId, ResourceManager manager) {
        this.manager = manager;
        this.modId = modId;
    }

    public static <T extends BlockType> BlockTypeResourceTransform<T> create(String modId, ResourceManager manager) {
        return new BlockTypeResourceTransform<>(modId, manager);
    }

    public static BlockTypeResourceTransform<WoodType> wood(String modId, ResourceManager manager) {
        return new BlockTypeResourceTransform<>(modId, manager);
    }

    public static BlockTypeResourceTransform<LeavesType> leaves(String modId, ResourceManager manager) {
        return new BlockTypeResourceTransform<>(modId, manager);
    }


    public BlockTypeResourceTransform<T> setIdModifier(WoodTextModifier<T> modifier) {
        this.idModifiers = modifier;
        return this;
    }

    public BlockTypeResourceTransform<T> idReplaceType(String originalWoodName) {
        return setIdModifier((s, id, w) -> s.replace(originalWoodName, removePostfix(id, w)));
    }

    private String removePostfix(ResourceLocation id, T w) {
        String jj = id.getPath().replace(w.getTypeName(), "#");
        return jj.substring(0, jj.lastIndexOf("#")) + w.getTypeName();
    }

    public BlockTypeResourceTransform<T> idReplaceBlock(String blockName) {
        return setIdModifier((s, id, w) -> s.replace(blockName, id.getPath()));
    }

    public BlockTypeResourceTransform<T> replaceBlockType(String typeName) {
        this.addModifier((s, id, w) -> s.replace(modId + ":block/" + typeName,
                id.getNamespace() + ":block/" + removePostfix(id, w)));
        return this;
    }

    public BlockTypeResourceTransform<T> addModifier(WoodTextModifier<T> modifier) {
        this.textModifiers.add(modifier);
        return this;
    }

    public BlockTypeResourceTransform<T> addModifier(BiFunction<String, T, String> genericModifier) {
        return this.addModifier((s, id, w) -> genericModifier.apply(s, w));
    }

    public BlockTypeResourceTransform<T> replaceSimpleBlock(Block block) {
        ResourceLocation res = block.getRegistryName();
        return replaceSimpleBlock(res.getNamespace(), res.getPath());
    }

    public BlockTypeResourceTransform<T> replaceSimpleBlock(String blockNamespace, String blockName) {
        return replaceSimpleBlock(blockNamespace, blockName, ":block/", ":block/");
    }

    public BlockTypeResourceTransform<T> replaceSimpleBlock(String blockNamespace, String blockName, String inBetween, String inBetween2) {
        return this.addModifier((s, id, w) -> s.replace(blockNamespace + inBetween + blockName,
                id.getNamespace() + inBetween2 + id.getPath()));
    }

    public BlockTypeResourceTransform<T> replaceString(String from, String to) {
        return this.addModifier((s, id, w) -> s.replace(from, to));
    }

    public BlockTypeResourceTransform<T> replaceOakPlanks() {
        return this.replaceWithTextureFromChild("minecraft:block/oak_planks", "planks");
    }

    public BlockTypeResourceTransform<T> replaceOakBark() {
        return this.replaceWithTextureFromChild("minecraft:block/oak_log", "log", s -> !s.contains("top"));
    }

    public BlockTypeResourceTransform<T> replaceWithTextureFromChild(String target, String textureFromChild) {
        return replaceWithTextureFromChild(target, textureFromChild, s -> true);
    }

    public BlockTypeResourceTransform<T> replaceWithTextureFromChild(String target, String textureFromChild,
                                                                     Predicate<String> texturePredicate) {
        return replaceWithTextureFromChild(target, w -> w.getChild(textureFromChild), texturePredicate);
    }

    public BlockTypeResourceTransform<T> replaceWithTextureFromChild(String target, Function<T, ItemLike> childProvider,
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
                    r = s.replace(target, newTexture.toString());
                }
            } catch (FileNotFoundException ignored) {
            }
            return r;
        });
    }

    public StaticResource transform(StaticResource resource, ResourceLocation blockId, T type) {
        String newText = new String(resource.data, StandardCharsets.UTF_8);

        for (var m : textModifiers) {
            newText = m.apply(newText, blockId, type);
        }

        ResourceLocation oldPath = resource.location;
        //old code. TODO: redo
            /*
            StringBuilder builder = new StringBuilder();
            String[] partial = oldPath.getPath().split("/");
            for (int i = 0; i < partial.length; i++) {
                if (i != 0) builder.append("/");
                if (i == partial.length - 1) {
                    builder.append(idModifiers.apply(partial[i], blockId, type));
                } else builder.append(partial[i]);
            }*/
        String id = idModifiers.apply(oldPath.getPath(), blockId, type);
        ResourceLocation newLocation = new ResourceLocation(blockId.getNamespace(), id);

        return StaticResource.create(newText.getBytes(), newLocation);
    }
}