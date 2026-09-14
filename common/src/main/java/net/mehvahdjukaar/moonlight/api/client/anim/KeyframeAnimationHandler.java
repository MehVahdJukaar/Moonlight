package net.mehvahdjukaar.moonlight.api.client.anim;

import net.mehvahdjukaar.moonlight.api.client.model.RootModel;
import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.AnimationState;
import org.joml.Vector3f;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This combines animation methods from {@link KeyframeAnimations} and {@link HierarchicalModel} but they are modified to use
 * our {@link RootModel} interface. <br>
 * Use these methods if you want to apply {@link KeyframeAnimations} to a model that doesn't extend {@link HierarchicalModel}.
 */
public class KeyframeAnimationHandler {
    private static final Vector3f ANIMATION_VECTOR_CACHE = new Vector3f();

    public static <M extends EntityModel<?> & RootModel> void animateWalk(M model, AnimationDefinition pAnimationDefinition, float pLimbSwing, float pLimbSwingAmount, float pMaxAnimationSpeed, float pAnimationScaleFactor) {
        long i = (long)(pLimbSwing * 50.0F * pMaxAnimationSpeed);
        float f = Math.min(pLimbSwingAmount * pAnimationScaleFactor, 1.0F);
        animate(model, pAnimationDefinition, i, f, ANIMATION_VECTOR_CACHE);
    }

    public static <M extends EntityModel<?> & RootModel> void animate(M model, AnimationState pAnimationState, AnimationDefinition pAnimationDefinition, float pAgeInTicks) {
        animate(model, pAnimationState, pAnimationDefinition, pAgeInTicks, 1.0F);
    }

    public static <M extends EntityModel<?> & RootModel> void animate(M model, AnimationState pAnimationState, AnimationDefinition pAnimationDefinition, float pAgeInTicks, float pSpeed) {
        pAnimationState.updateTime(pAgeInTicks, pSpeed);
        pAnimationState.ifStarted((p_233392_) -> animate(model, pAnimationDefinition, p_233392_.getAccumulatedTime(), 1.0F, ANIMATION_VECTOR_CACHE));
    }

    public static <M extends EntityModel<?> & RootModel> void animate(M model, AnimationDefinition pAnimationDefinition, long pAccumulatedTime, float pScale, Vector3f pAnimationVecCache) {
        float time = getElapsedSeconds(pAnimationDefinition, pAccumulatedTime);

        for(Map.Entry<String, List<AnimationChannel>> entry : pAnimationDefinition.boneAnimations().entrySet()) {
            Optional<ModelPart> optional = getAnyDescendantWithName(model, entry.getKey());
            List<AnimationChannel> list = entry.getValue();
            optional.ifPresent((modelPart) -> list.forEach((animationChannel) -> {
                Keyframe[] keyframes = animationChannel.keyframes();
                int frameInd = Math.max(0, Mth.binarySearch(0, keyframes.length, (ix) -> time <= keyframes[ix].timestamp()) - 1);
                int nextFrameInd = Math.min(keyframes.length - 1, frameInd + 1);
                Keyframe frame = keyframes[frameInd];
                Keyframe nextFrame = keyframes[nextFrameInd];
                float timeDelta = time - frame.timestamp();
                float delta;
                if (nextFrameInd != frameInd) {
                    delta = Mth.clamp(timeDelta / (nextFrame.timestamp() - frame.timestamp()), 0.0F, 1.0F);
                } else {
                    delta = 0.0F;
                }

                nextFrame.interpolation().apply(pAnimationVecCache, delta, keyframes, frameInd, nextFrameInd, pScale);
                animationChannel.target().apply(modelPart, pAnimationVecCache);
            }));
        }

    }

    private static <M extends EntityModel<?> & RootModel> Optional<ModelPart> getAnyDescendantWithName(M model, String pName) {
        return pName.equals("root") ? Optional.of(model.root()) : model.root().getAllParts().filter((part)
                -> part.hasChild(pName)).findFirst().map((part) -> part.getChild(pName));
    }

    private static float getElapsedSeconds(AnimationDefinition pAnimationDefinition, long pAccumulatedTime) {
        float f = (float)pAccumulatedTime / 1000.0F;
        return pAnimationDefinition.looping() ? f % pAnimationDefinition.lengthInSeconds() : f;
    }

    public static Vector3f posVec(float x, float y, float z) {
        return new Vector3f(x, -y, z);
    }

    public static Vector3f degreeVec(float xDegrees, float yDegrees, float zDegrees) {
        return new Vector3f(xDegrees * ((float)Math.PI / 180F), yDegrees * ((float)Math.PI / 180F), zDegrees * ((float)Math.PI / 180F));
    }

    public static Vector3f scaleVec(double xScale, double yScale, double zScale) {
        return new Vector3f((float)(xScale - (double)1.0F), (float)(yScale - (double)1.0F), (float)(zScale - (double)1.0F));
    }
}
