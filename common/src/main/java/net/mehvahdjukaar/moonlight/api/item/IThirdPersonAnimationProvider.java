package net.mehvahdjukaar.moonlight.api.item;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;

/**
 * Poses the holder arms before the item renders in third person. Attach with ClientAnimationExtension.attach.
 * Return ArmPose.SPYGLASS from getUseAnimation to skip the arm bob, or unbob with AnimationUtils.bobModelPart(arm, entity.tickCount, -1.0F).
 */
public interface IThirdPersonAnimationProvider {

    enum Result {
        /** Not handled, vanilla poses this arm. */
        PASS,
        HANDLED,
        /** This call posed both arms; the other arm is not posed at all, not even by vanilla. */
        HANDLED_BOTH_ARMS
    }

    /** Called once per arm holding the item. The main arm is in state.mainArm. */
    Result poseArm(ItemStack stack, HumanoidModel<?> model, HumanoidRenderState state, HumanoidArm arm);
}
