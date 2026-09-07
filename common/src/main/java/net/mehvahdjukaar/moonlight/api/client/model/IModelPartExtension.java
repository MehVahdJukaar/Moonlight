package net.mehvahdjukaar.moonlight.api.client.model;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;

public interface IModelPartExtension {

    void moonlight$setDimensions(int texWidth, int texHeight);

    int moonlight$getTextWidth();

    int moonlight$getTextHeight();

    static ModelPart getRootPart(EntityModel<?> model) {
        return model.root();
    }

    static int[] getTextureSize(EntityModel<?> model) {
        IModelPartExtension root = (IModelPartExtension) (Object) model.root();
        return new int[]{root.moonlight$getTextWidth(), root.moonlight$getTextHeight()};
    }
}
