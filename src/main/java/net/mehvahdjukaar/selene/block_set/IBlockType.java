package net.mehvahdjukaar.selene.block_set;

import net.mehvahdjukaar.selene.resourcepack.asset_generators.LangBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public interface IBlockType {

    ResourceLocation getId();

    String toString();

    String getTypeName();

    String getNamespace();

    /**
     * Use this to get the new id of a block variant
     *
     * @param baseName base variant name
     * @return something like mod_id/[baseName]_oak. ignores minecraft namespace
     */
    default String getVariantId(String baseName) {
        return getVariantId(baseName, true);
    }

    default String getVariantId(String baseName, boolean prefix) {
        String namespace = this.getNamespace();
        if (namespace.equals("minecraft")) return baseName + "_" + this.getTypeName();

        return prefix ? namespace + "/" + baseName + "_" + this.getTypeName() :
                namespace + "/" + this.getTypeName() + "_" + baseName;
    }

    default String getNameForTranslation(String append) {
        //There's got to be a faster method call lol
        return LangBuilder.getReadableName(this.getTypeName() + "_" + append);
    }

    default boolean isVanilla() {
        return this.getNamespace().equals("minecraft");
    }

    abstract class SetFinder<T extends IBlockType> {
        public abstract Optional<T> get();
    }

    @Nullable
    default <V extends IForgeRegistryEntry<V>> V findRelatedEntry(String appendedName, IForgeRegistry<V> reg) {
        return findRelatedEntry(appendedName, "", reg);
    }

    @Nullable
    default <V extends IForgeRegistryEntry<V>> V findRelatedEntry(String append, String postpend, IForgeRegistry<V> reg) {
        String post = postpend.isEmpty() ? "" : "_" + postpend;
        var id = this.getId();
        ResourceLocation[] targets = {
                new ResourceLocation(id.getNamespace(), id.getPath() + "_" + append + post),
                new ResourceLocation(id.getNamespace(), append + "_" + id.getPath() + post)
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

}
