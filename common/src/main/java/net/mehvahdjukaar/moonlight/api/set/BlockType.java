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

    public String getAppendableId() {
        return this.getNamespace() + "/" + this.getTypeName();
    }

    @Override
    public String toString() {
        return this.id.toString();
    }

    public abstract String getTranslationKey();

    /**
     * Use this to get the new id of a block variant
     *
     * @param baseName base variant name
     * @return something like mod_id/[baseName]_oak. ignores minecraft namespace
     */
    public String getVariantId(String baseName) {
        String namespace = this.isVanilla() ? "" : this.getNamespace() + "/";
        if (baseName.contains("%s")) return namespace + String.format(baseName, this.getTypeName());
        else return namespace + baseName + "_" + this.getTypeName();
    }

    public String getVariantId(String baseName, boolean prefix) {
        return getVariantId(prefix ? baseName + "_%s" : "%s_" + baseName);
    }

    public String getVariantId(String postfix, String prefix) {
        return getVariantId(prefix + "_%s_" + postfix);
    }

    public String getReadableName() {
        return LangBuilder.getReadableName(this.getTypeName());
    }

    public boolean isVanilla() {
        return this.getNamespace().equals("minecraft");
    }

    @FunctionalInterface
    public interface SetFinder<T extends BlockType> extends Supplier<Optional<T>> {
        Optional<T> get();
    }

    @Nullable
    protected <V> V findRelatedEntry(String appendedName, Registry<V> reg) {
        return findRelatedEntry(appendedName, "", reg);
    }

    @Nullable
    protected <V> V findRelatedEntry(String append, String postPend, Registry<V> reg) {
        if (this.id.getNamespace().equals("tfc")) {
            var o = reg.getOptional(
                    new ResourceLocation(id.getNamespace(), "wood/" + postPend + "/" + id.getPath()));
            if (o.isPresent()) return o.get();
        }


        String post = postPend.isEmpty() ? "" : "_" + postPend;
        ResourceLocation[] targets = {
                new ResourceLocation(id.getNamespace(), id.getPath() + "_" + append + post),
                new ResourceLocation(id.getNamespace(), append + "_" + id.getPath() + post),
                new ResourceLocation(id.getNamespace(), id.getPath() + "_planks_" + append + post),
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
        return v instanceof Item i ? i : null;
    }

    @Nullable
    public Block getBlockOfThis(String key) {
        var v = this.getChild(key);
        return v instanceof Block b ? b : null;
    }

    @Nullable
    public Object getChild(String key) {
        return this.children.get(key);
    }

    /**
     * Should be called after you register a block that is made out of this wood type
     */
    public void addChild(String genericName, @Nullable Object itemLike) {
        if (itemLike != null) {
            this.children.put(genericName, itemLike);
            var v = BlockSetInternal.getRegistry(this.getClass());
            if (v != null) {
                v.mapBlockToType(itemLike, this);
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
        return children.inverse().get(child);
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
                var blockChanged = changeType(bi.getBlock(), originalMat, destinationMat);
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
            return b.getSoundType(b.defaultBlockState());
        }
        return SoundType.STONE;
    }

}
