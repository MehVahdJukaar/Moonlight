package net.mehvahdjukaar.moonlight.api.set;

import net.mehvahdjukaar.moonlight.api.client.language.LangBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public abstract class BlockType {

    //stuff made out of this type
    private final Map<String, ItemLike> children = new HashMap<>();
    boolean needsInit = true;
    public final ResourceLocation id;

    public BlockType(ResourceLocation resourceLocation) {
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

    public static abstract class SetFinder<T extends BlockType> {
        public abstract Optional<T> get();
    }

    @Nullable
    protected <V> V findRelatedEntry(String appendedName, Registry<V> reg) {
        return findRelatedEntry(appendedName, "", reg);
    }

    @Nullable
    protected <V> V findRelatedEntry(String append, String postPend, Registry<V> reg) {
        String post = postPend.isEmpty() ? "" : "_" + postPend;
        var id = this.getId();
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
    public Set<Map.Entry<String, ItemLike>> getChildren() {
        if (needsInit) this.initAfterSetup();
        return this.children.entrySet();
    }

    /**
     * Gets an item made out of this type
     */
    @Nullable
    public Item getItemOfThis(String key) {
        var v = this.getChild(key);
        return v == null ? null : v.asItem();
    }

    @Nullable
    public Block getBlockOfThis(String key) {
        var v = this.getChild(key);
        return v instanceof Block b ? b : null;
    }

    @Nullable
    public ItemLike getChild(String key) {
        if (needsInit) this.initAfterSetup();
        return this.children.get(key);
    }

    /**
     * Should be called after you register a block that is made out of this wood type
     */
    public void addChild(String genericName, @Nullable ItemLike itemLike) {
        if (itemLike != null) {
            this.children.put(genericName, itemLike);
        }
    }

    protected abstract void initializeVanillaChildren();

    protected void initAfterSetup() {
        this.needsInit = false;
        this.initializeVanillaChildren();
    }

    /**
     * base block that this type originates from
     */
    public abstract ItemLike mainChild();

    /**
     * Tries changing an item block type. returns null if it fails
     *
     * @param current        target item
     * @param originalMat    material from which the target item is made of
     * @param destinationMat desired block type
     */
    @Nullable
    public static ItemLike changeItemBlockType(ItemLike current, BlockType originalMat, BlockType destinationMat) {
        if (destinationMat == originalMat) return current;
        for (var c : originalMat.getChildren()) {
            var child = c.getValue();
            ItemLike replacement = null;
            if (current instanceof Block && child instanceof Block) {
                if (current == child) {
                    replacement = destinationMat.getChild(c.getKey());
                }
            } else if (current.asItem() == c.getValue().asItem()) {
                replacement = destinationMat.getChild(c.getKey());
            }
            if (replacement != null) {
                return replacement;
            }
        }
        return null;
    }

}
