package net.mehvahdjukaar.moonlight.api.set;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.mehvahdjukaar.moonlight.api.resources.assets.LangBuilder;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.set.BlockSetInternal;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

public abstract class BlockType {

    //stuff made out of this type
    private final BiMap<String, Object> children = HashBiMap.create();
    public final ResourceLocation id;

    protected BlockType(ResourceLocation resourceLocation) {
        this.id = resourceLocation;
    }

    public ResourceLocation getId() {
        return id;
    }

    public String getTypeName() {
        return id.getPath();
    }

    public String getNamespace() {
        return id.getNamespace();
    }

    /**
     * @return namespace/TYPENAME
     */
    public String getAppendableId() {
        return this.getNamespace() + "/" + this.getTypeName();
    }

    /**
     * @return namespace/TYPENAME_suffix
     */
    public String getAppendableIdWith(String suffix) {
        return getAppendableIdWith("", suffix);
    }

    /**
     * @return namespace/prefix_TYPENAME_suffix
     */
    public String getAppendableIdWith(String prefix, String suffix) {
        String prefixed = (prefix.isEmpty()) ? "" : prefix + "_";
        return  this.getNamespace() +"/"+ prefixed + this.getTypeName() +"_"+ suffix;
    }

    /**
     * @return shortenedID/namespace/TYPENAME_suffix
     */
    public String createPathWith(String shortenedId, String suffix) {
        return createPathWith(shortenedId, "", suffix);
    }

    /**
     * @return shortenedID/namespace/prefix_TYPENAME_suffix
     */
    public String createPathWith(String shortenedId, String prefix, String suffix) {
        String prefixed = (prefix.isEmpty()) ? "" : prefix + "_";
        return shortenedId +"/"+ this.getNamespace() +"/"+ prefixed + this.getTypeName() +"_"+ suffix;
    }

    /**
     * @return namesapce:folder/shortenedID/namespace/prefix_TYPENAME_suffix
     */
    public String createFullIdWith(String modId, String folder, String shortenedId, String prefix, String suffix) {
        String prefixed = (prefix.isEmpty()) ? "" : prefix + "_";
        String foldered = (folder.isEmpty()) ? "" : folder + "/";

        return modId+":"+ foldered + shortenedId +"/"+ this.getNamespace() +"/"+ prefixed + this.getTypeName() +"_"+ suffix;
    }

    @Override
    public String toString() {
        return this.id.toString();
    }

    public abstract String getTranslationKey();

    /**
     * Use this to get the new id of a block variant
     * NOTE: minecraft will be ignored as namespace
     * TYPENAME == getTypeName()
     * @param baseName base variant name
     * @return baseName_TYPENAME OR namespace/baseName_TYPENAME
     */
    public String getVariantId(String baseName) {
        String namespace = this.isVanilla() ? "" : this.getNamespace() + "/";
        if (baseName.contains("%s")) return namespace + String.format(baseName, this.getTypeName());
        else return namespace + baseName + "_" + this.getTypeName();
    }

    /**
     * NOTE: minecraft will be ignored as namespace
     * @return prefix_baseName_TYPENAME OR namespace/prefix_baseName_TYPENAME
     */
    public String getVariantId(String baseName, boolean prefix) {
        return getVariantId(prefix ? baseName + "_%s" : "%s_" + baseName);
    }

    /**
     * NOTE: minecraft will be ignored as namespace
     * @return prefix_TYPENAME_postfix OR namespace/prefix_TYPENAME_postfix
     */
    public String getVariantId(String postfix, String prefix) {
        return getVariantId(prefix + "_%s_" + postfix);
    }

    public String getReadableName() {
        return LangBuilder.getReadableName(this.getTypeName());
    }

    public boolean isVanilla() {
        return this.getNamespace().equals("minecraft");
    }

    public <T extends BlockType> BlockTypeRegistry<T> getRegistry() {
        return (BlockTypeRegistry<T>) BlockSetInternal.getRegistry(this.getClass());
    }

    @FunctionalInterface
    public interface SetFinder<T extends BlockType> extends Supplier<Optional<T>> {
        Optional<T> get();
    }

    @Nullable
    protected <V> V findRelatedEntry(String after, Registry<V> reg) {
        return findRelatedEntry(after, "", reg);
    }

    @Nullable
    protected <V> V findRelatedEntry(String before, String after, Registry<V> reg) {
        if (!after.isEmpty()) after = "_" + after;
        ResourceLocation[] targets = {
                id.withPath(id.getPath() + "_" + before + after),
                id.withPath(before + "_" + id.getPath() + after),
        };
        V found = null;
        for (var r : targets) {
            if (reg.containsKey(r)) {
                found = reg.get(r);
                break;
            }
        }
        return found;
    }

    /**
     * @return set of objects made out of this block type marked by their generic name
     */
    public Set<Map.Entry<String, Object>> getChildren() {
        return this.children.entrySet();
    }

    /**
     * Gets an item made out of this type
     */
    @Nullable
    public Item getItemOfThis(String key) {
        var v = this.getChild(key);
        return v instanceof ItemLike i ? i.asItem() : null;
    }

    @Nullable
    public Block getBlockOfThis(String key) {
        var v = this.getChild(key);
        if (v instanceof BlockItem bi) return bi.getBlock();
        return v instanceof Block b ? b : null;
    }

    @Nullable
    public Object getChild(String key) {
        return this.children.get(key);
    }

    public boolean hasChild(String key) {
        return this.children.containsKey(key);
    }

    public boolean hasChildren(String... keys) {
        for (String key : keys) {
            if (!this.hasChild(key)) return false;
        }
        return true;
    }

    /**
     * Should be called after you register a block made out of this wood type
     */
    public void addChild(String genericName, @Nullable Object obj) {
        if (obj == Items.AIR || obj == Blocks.AIR) {
            //add better check than this...
            throw new IllegalStateException("Tried to add air block/item to Block Type. Key " + genericName + ". This is a Moonlight bug, please report me");
        }
        if (obj != null) {
            try {
                this.children.put(genericName, obj);
                var registry = BlockSetInternal.getRegistry(this.getClass());
                if (registry != null) {
                    // Don't you dare to access block item map now.
                    // Will cause all sorts of issues. We'll worry about items later
                    registry.mapObjectToType(obj, this);
                }
            } catch (Exception e) {
                Moonlight.LOGGER.error("Failed to add block type child: value already present. Key {}, Object {}, BlockType {}", genericName, obj, this);
            }
        }
    }

    /**
     * Runs right after all blocks registration has run but before any dynamic block registration is run.
     * Use to add existing vanilla or modded blocks
     */
    protected abstract void initializeChildrenBlocks();

    /**
     * Runs right after all items registration has run but before any dynamic block registration is run.
     * Use to add existing vanilla or modded blocks
     */
    protected abstract void initializeChildrenItems();

    /**
     * base block that this type originates from. Has to be an ItemLike
     */
    public abstract ItemLike mainChild();

    /**
     * Returns the given child string key. Null if this type does not have such child
     */
    @Nullable
    public String getChildKey(Object child) {
        BiMap<Object, String> inverse = children.inverse();
        var firs = inverse.get(child);
        if (firs != null) return firs;
        if (child instanceof BlockItem bi) {
            return inverse.get(bi.getBlock());
        }
        return null;
    }

    /**
     * Tries changing an item block type. Returns null if it fails
     *
     * @param current        target item
     * @param originalMat    material from which the target item is made of
     * @param destinationMat desired block type
     */
    @Nullable
    public static Object changeType(Object current, BlockType originalMat, BlockType destinationMat) {
        if (destinationMat == originalMat) return current;
        String key = originalMat.getChildKey(current);
        if (key != null) {
            return destinationMat.getChild(key);
        }
        return null;
    }

    //for items
    @Nullable
    public static Item changeItemType(Item current, BlockType originalMat, BlockType destinationMat) {
        Object changed = changeType(current, originalMat, destinationMat);
        //if item swap fails, try to swap blocks instead
        if (changed == null) {
            if (current instanceof BlockItem bi) {
                Object blockChanged = changeType(bi.getBlock(), originalMat, destinationMat);
                if (blockChanged instanceof Block il) {
                    Item i = il.asItem();
                    if (i != Items.AIR) changed = i;
                }
            }
        }
        if (changed instanceof ItemLike il) {
            if (il.asItem() == current) {
                Moonlight.LOGGER.error("Somehow changed an item type into itself. How? Target mat {}, destination map {}, item {}",
                        destinationMat, originalMat, il);
            }
            return il.asItem();
        }
        return null;
    }

    //for blocks
    @Nullable
    public static Block changeBlockType(@NotNull Block current, BlockType originalMat, BlockType destinationMat) {
        Object changed = changeType(current, originalMat, destinationMat);
        //if block swap fails, try to swap items instead
        if (changed == null) {
            if (current.asItem() != Items.AIR) {
                var itemChanged = changeType(current.asItem(), originalMat, destinationMat);
                if (itemChanged instanceof BlockItem bi) {
                    Item i = bi.asItem();
                    if (i != Items.AIR) changed = i;
                }
            }
        }
        if (changed instanceof Block b) return b;
        return null;
    }

    public SoundType getSound() {
        if (this.mainChild() instanceof Block b) {
            return b.defaultBlockState().getSoundType();
        }
        return SoundType.STONE;
    }
}
