package net.mehvahdjukaar.selene.block_set;

import net.mehvahdjukaar.selene.resourcepack.AssetGenerators;
import net.minecraft.resources.ResourceLocation;

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
        String namespace = this.getNamespace();
        if (namespace.equals("minecraft")) return baseName + "_" + this.getTypeName();
        return this.getNamespace() + "/" + baseName + "_" + this.getTypeName();
    }

    default String getNameForTranslation(String append) {
        //There's got to be a faster method call lol
        return AssetGenerators.LangBuilder.getReadableName(this.getTypeName() + "_" + append);
    }

    default boolean isVanilla() {
        return this.getNamespace().equals("minecraft");
    }

    abstract class SetFinder<T extends IBlockType>{

        public abstract Optional<T> get();
    }
}
