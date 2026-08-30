package net.mehvahdjukaar.moonlight.core.mixins;

import net.mehvahdjukaar.moonlight.api.item.ClientAnimationExtension;
import net.mehvahdjukaar.moonlight.api.item.IThirdPersonAnimationProvider;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
public abstract class ThirdPersonRendererMixin {

    @Unique
    private boolean moonlight$posedBothArms = false;

    @Inject(method = "poseRightArm", at = @At(value = "HEAD"), cancellable = true, require = 0)
    private void moonlight$poseRightArm(HumanoidRenderState state, CallbackInfo ci) {
        moonlight$poseArm(state, state.rightHandItemStack, HumanoidArm.RIGHT, ci);
    }

    @Inject(method = "poseLeftArm", at = @At(value = "HEAD"), cancellable = true, require = 0)
    private void moonlight$poseLeftArm(HumanoidRenderState state, CallbackInfo ci) {
        moonlight$poseArm(state, state.leftHandItemStack, HumanoidArm.LEFT, ci);
    }

    @Unique
    private void moonlight$poseArm(HumanoidRenderState state, ItemStack stack, HumanoidArm arm, CallbackInfo ci) {
        //a BOTH_ARMS result on the first arm already posed this one too
        if (this.moonlight$posedBothArms) {
            ci.cancel();
            return;
        }
        ClientAnimationExtension ext = ClientAnimationExtension.get(stack.getItem());
        IThirdPersonAnimationProvider provider = ext == null ? null : ext.thirdPersonAnimation();
        if (provider == null) return;
        var result = provider.poseArm(stack, (HumanoidModel<?>) (Object) this, state, arm);
        if (result == IThirdPersonAnimationProvider.Result.PASS) return;
        if (result == IThirdPersonAnimationProvider.Result.HANDLED_BOTH_ARMS) this.moonlight$posedBothArms = true;
        ci.cancel();
    }

    @Inject(method = "setupAnim*", at = @At(value = "RETURN"), require = 0)
    private void moonlight$resetPosedBothArms(HumanoidRenderState state, CallbackInfo ci) {
        this.moonlight$posedBothArms = false;
    }
}
