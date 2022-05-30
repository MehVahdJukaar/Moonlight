package net.mehvahdjukaar.selene.block_set;

import net.mehvahdjukaar.selene.client.asset_generators.LangBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

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
        return getVariantId(baseName, true);
    }

    public String getVariantId(String baseName, boolean prefix) {
        String namespace = this.getNamespace();
        if (namespace.equals("minecraft")) return baseName + "_" + this.getTypeName();

        return prefix ? namespace + "/" + baseName + "_" + this.getTypeName() :
                namespace + "/" + this.getTypeName() + "_" + baseName;
    }

    public String getVariantId(String postfix, String prefix) {
        String namespace = this.getNamespace();
        if (namespace.equals("minecraft")) return prefix + "_" + this.getTypeName() + "_" + postfix;
        return namespace + "/" + prefix + "_" + this.getTypeName() + "_" + postfix;
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
    protected <V extends IForgeRegistryEntry<V>> V findRelatedEntry(String appendedName, IForgeRegistry<V> reg) {
        return findRelatedEntry(appendedName, "", reg);
    }

    @Nullable
    protected <V extends IForgeRegistryEntry<V>> V findRelatedEntry(String append, String postpend, IForgeRegistry<V> reg) {
        String post = postpend.isEmpty() ? "" : "_" + postpend;
        var id = this.getId();
        ResourceLocation[] targets = {
                new ResourceLocation(id.getNamespace(), id.getPath() + "_" + append + post),
                new ResourceLocation(id.getNamespace(), append + "_" + id.getPath() + post),
                new ResourceLocation(id.getNamespace(), id.getPath() + "_planks_" + append + post),
        };
        V found = null;
        for (var r : targets) {
            if (reg.containsKey(r)) {
                found = reg.getValue(r);
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
    public void addChild(String genericName, ItemLike itemLike) {
        this.children.put(genericName, itemLike);
    }

    protected abstract void initializeChildren();

    protected void initAfterSetup() {
        this.needsInit = false;
        this.initializeChildren();
    }

    ;

    /**
     * Tries changing an item block tupe. returns the item itself it it fails
     *
     * @param current        target item
     * @param originalMat    material from which the target item is made of
     * @param destinationMat desired block type
     */
    public static Item changeItemBlockType(Item current, BlockType originalMat, BlockType destinationMat) {
        AtomicReference<Item> newIng = new AtomicReference<>(current);
        originalMat.getChildren().forEach((e) -> {
            if (current == e.getValue().asItem()) {
                Item replacement = destinationMat.getItemOfThis(e.getKey());
                if (replacement != null) newIng.set(replacement);
            }
        });
        return newIng.get();
    }

}
