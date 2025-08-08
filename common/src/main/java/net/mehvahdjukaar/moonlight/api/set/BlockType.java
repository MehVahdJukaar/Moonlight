package net.mehvahdjukaar.moonlight.api.set;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.mehvahdjukaar.moonlight.api.resources.assets.LangBuilder;
import net.mehvahdjukaar.moonlight.api.util.INamedSupplier;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.set.BlockSetInternal;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
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

import java.util.HashMap;
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
        return this.getNamespace() + "/" + prefixed + this.getTypeName() + "_" + suffix;
    }

    /**
     * @return shortenedID/namespace/TYPENAME_suffix
     */
    public String createPathWith(String shortenedId, String suffix) {
        return createFullIdWith("", "", shortenedId, "", suffix);
    }

    /**
     * @return shortenedID/namespace/prefix_TYPENAME_suffix
     */
    public String createPathWith(String shortenedId, String prefix, String suffix) {
        return createFullIdWith("", "", shortenedId, prefix, suffix);
    }

    /**
     * @return namespace:folder/shortenedID/namespace/prefix_TYPENAME_suffix
     * OPTIONAL: modId, folder, prefix can be empty
     */
    public String createFullIdWith(String modIdOrEmpty, String folderOrEmpty, String shortenedIdOrEmpty, String prefixOrEmpty, String suffix) {
        String modIded = (modIdOrEmpty.isEmpty()) ? "" : modIdOrEmpty + ":";
        String foldered = (folderOrEmpty.isEmpty()) ? "" : folderOrEmpty + "/";
        String namespaced = (modIdOrEmpty.equals(this.getNamespace())) ? "" : this.getNamespace() + "/";
        String shortenedId = (shortenedIdOrEmpty.isEmpty()) ? "" : shortenedIdOrEmpty + "/";

        String prefixed = "";
        if (prefixOrEmpty.contains("/")) prefixed = prefixOrEmpty;
        else if (!prefixOrEmpty.isEmpty()) prefixed = prefixOrEmpty + "_";

        String suffixed = "";
        if (suffix.matches("\\.(png|json)")) suffixed = suffix;
        else if (!suffix.isEmpty()) suffixed = "_" + suffix;

        return modIded + foldered + shortenedId + namespaced + prefixed + this.getTypeName() + suffixed;
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
     *
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
     *
     * @return prefix_baseName_TYPENAME OR namespace/prefix_baseName_TYPENAME
     */
    public String getVariantId(String baseName, boolean prefix) {
        return getVariantId(prefix ? baseName + "_%s" : "%s_" + baseName);
    }

    /**
     * NOTE: minecraft will be ignored as namespace
     *
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
        return Utils.findFirstInRegistry(reg, targets);
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


    //TODO: move out of here
    @FunctionalInterface
    public interface SetFinder<T extends BlockType> extends Supplier<Optional<T>> {
        Optional<T> get();
    }

    public abstract static class SetFinderBuilder<T extends BlockType> implements SetFinder<T> {

        protected final ResourceLocation id;
        protected final Map<String, Supplier<ItemLike>> childNames = new HashMap<>();
        private final BlockTypeRegistry<T> reg; //TODO:remove this and place this class in registry class

        public SetFinderBuilder(ResourceLocation id, BlockTypeRegistry<T> reg) {
            this.id = id;
            this.reg = reg;
        }

        public SetFinderBuilder<T> child(String childType, Supplier<ItemLike> child) {
            this.childNames.put(childType, child);
            return this;
        }

        public SetFinderBuilder<T> childItem(String childType, ResourceLocation childName) {
            return this.child(childType, () -> BuiltInRegistries.ITEM.getOptional(childName).orElseThrow());
        }

        /**
         * @param prefix include the underscore, "_" if the blockId has one
         * @param suffix include the underscore, "_" if the blockId has one
         */
        public SetFinderBuilder<T> childItemAffix(String childType, String prefix, String suffix) {
            return this.childItem(childType, prefix + id.getPath() + suffix);
        }

        /**
         * @param suffix include the underscore, "_" if the blockId has one
         */
        public SetFinderBuilder<T> childItemSuffix(String childType, String suffix) {
            return this.childItem(childType, id.getPath() + suffix);
        }

        public SetFinderBuilder<T> childItem(String childType, String childName) {
            return this.childItem(childType,
                    Utils.idWithOptionalNamespace(childName, id.getNamespace()));
        }

        public SetFinderBuilder<T> childBlock(String childType, ResourceLocation childName) {
            return this.child(childType, () -> BuiltInRegistries.BLOCK.getOptional(childName).orElseThrow());
        }

        /**
         * @param prefix include the underscore, "_" if the blockId has one
         * @param suffix include the underscore, "_" if the blockId has one
         */
        public SetFinderBuilder<T> childBlockAffix(String childType, String prefix, String suffix) {
            return this.childBlock(childType, prefix + id.getPath() + suffix);
        }

        /**
         * @param suffix include the underscore, "_" if the blockId has one
         */
        public SetFinderBuilder<T> childBlockSuffix(String childType, String suffix) {
            return this.childBlock(childType, id.getPath() + suffix);
        }

        public SetFinderBuilder<T> childBlock(String childType, String childName) {
            return this.childBlock(childType,
                    Utils.idWithOptionalNamespace(childName, id.getNamespace()));
        }

        // returns a supplier of the found block
        public INamedSupplier<T> build() {
            return reg.makeFutureHolder(id);
        }
    }
}
