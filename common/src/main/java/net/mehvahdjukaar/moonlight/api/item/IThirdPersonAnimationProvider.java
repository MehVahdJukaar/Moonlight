package net.mehvahdjukaar.moonlight.api.item;

import net.mehvahdjukaar.candlelight.api.ClientOnly;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Poses the holder arms before the item renders in third person. Attach with ClientAnimationExtension.attach.
 * Return ArmPose.SPYGLASS from getUseAnimation to skip the arm bob, or unbob with AnimationUtils.bobModelPart(arm, entity.tickCount, -1.0F).
 */
public interface IThirdPersonAnimationProvider {

    /** Return true to skip the default animation. */
    @ClientOnly
    boolean poseRightArm(ItemStack stack, HumanoidModel<?> model, HumanoidRenderState state, HumanoidArm mainArm);

    /** Return true to skip the default animation. */
    @ClientOnly
    boolean poseLeftArm(ItemStack stack, HumanoidModel<?> model, HumanoidRenderState state, HumanoidArm mainArm);

    @ClientOnly
    default boolean isTwoHanded() {
        return false;
    }

    @Nullable
    static IThirdPersonAnimationProvider get(Item target) {
        if (target instanceof IThirdPersonAnimationProvider p) return p;
        ClientAnimationExtension ext = ClientAnimationExtension.get(target);
        return ext == null ? null : ext.thirdPersonAnimation();
    }

}
