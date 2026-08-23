package net.mehvahdjukaar.moonlight.api.client.model;

import net.mehvahdjukaar.moonlight.core.mixins.accessor.AgeableListModelAccessor;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import org.jetbrains.annotations.Nullable;

/**
 * Duck interface implemented on every {@link ModelPart} (via mixin). It remembers the
 * texture (atlas) dimensions the part was baked with, so overlay render layers can map a
 * fixed-size overlay texture onto the model with pixel-perfect, texture-size-aware UVs
 * regardless of the model's own texture resolution.
 */
public interface IModelPartExtension {

    void moonlight$setDimensions(int texWidth, int texHeight);

    int moonlight$getTextWidth();

    int moonlight$getTextHeight();

    /**
     * Best-effort grab of a root {@link ModelPart} of an entity model, handling the
     * {@link HierarchicalModel}, the {@link net.minecraft.client.model.AgeableListModel} and the {@link RootModel} layouts.
     * Returns {@code null} for exotic models that expose neither.
     */
    @Nullable
    static ModelPart getRootPart(EntityModel<?> model) {
        if (model instanceof AgeableListModelAccessor al) {
            for (ModelPart v : al.moonlight$invokeBodyParts()) {
                return v;
            }
        } else if (model instanceof HierarchicalModel<?> m) {
            return m.root();
        } else if (model instanceof RootModel m) {
            return m.root();
        }
        return null;
    }

    /**
     * The baked texture size of an entity model as {@code [width, height]}, read from a root part.
     * Falls back to {@code 64x64} when the model exposes no usable root part.
     */
    static int[] getTextureSize(EntityModel<?> model) {
        ModelPart part = getRootPart(model);
        if (part != null) {
            IModelPartExtension ext = (IModelPartExtension) (Object) part;
            return new int[]{ext.moonlight$getTextWidth(), ext.moonlight$getTextHeight()};
        }
        return new int[]{64, 64};
    }
}
