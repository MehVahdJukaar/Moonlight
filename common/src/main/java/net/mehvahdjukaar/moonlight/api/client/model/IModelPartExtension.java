package net.mehvahdjukaar.moonlight.api.client.model;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;

/**
 * Duck interface implemented on every ModelPart (via mixin). It remembers the
 * texture (atlas) dimensions the part was baked with, so overlay render layers can map a
 * fixed-size overlay texture onto the model with pixel-perfect, texture-size-aware UVs
 * regardless of the model's own texture resolution.
 */
public interface IModelPartExtension {

    void moonlight$setDimensions(int texWidth, int texHeight);

    int moonlight$getTextWidth();

    int moonlight$getTextHeight();

    static int[] getTextureSize(EntityModel<?> model) {
        IModelPartExtension root = (IModelPartExtension) (Object) model.root();
        return new int[]{root.moonlight$getTextWidth(), root.moonlight$getTextHeight()};
    }
}
