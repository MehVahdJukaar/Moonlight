package net.mehvahdjukaar.moonlight.api.client.model;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;

public interface IModelPartExtension {

    void moonlight$setDimensions(int texWidth, int texHeight);

    int moonlight$getTextWidth();

    int moonlight$getTextHeight();

    //Best-effort
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

    static int[] getTextureSize(EntityModel<?> model) {
        IModelPartExtension root = (IModelPartExtension) (Object) model.root();
        return new int[]{root.moonlight$getTextWidth(), root.moonlight$getTextHeight()};
    }
}
