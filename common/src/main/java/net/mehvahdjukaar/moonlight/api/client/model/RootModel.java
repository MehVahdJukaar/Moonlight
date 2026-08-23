package net.mehvahdjukaar.moonlight.api.client.model;

import net.mehvahdjukaar.moonlight.api.client.anim.KeyframeAnimationHandler;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.animation.KeyframeAnimations;
import net.minecraft.client.model.geom.ModelPart;

/**
 * Implement this interface on models that should use vanillas animation system but don't extend {@link HierarchicalModel}. <br>
 * Use {@link KeyframeAnimationHandler} to apply the {@link KeyframeAnimations} to the model.
 */
public interface RootModel {
    ModelPart root();
}
