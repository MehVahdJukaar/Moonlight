package net.mehvahdjukaar.moonlight.api.client.model;

import net.mehvahdjukaar.moonlight.core.mixins.accessor.AgeableListModelAccessor;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import org.jetbrains.annotations.Nullable;

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
        ModelPart part = getRootPart(model);
        if (part != null) {
            IModelPartExtension ext = (IModelPartExtension) (Object) part;
            return new int[]{ext.moonlight$getTextWidth(), ext.moonlight$getTextHeight()};
        }
        return new int[]{64, 64};
    }
}
