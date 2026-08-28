package net.mehvahdjukaar.moonlight.core.mixins;

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
    private boolean moonlight$isTwoHanded = false;

    @Inject(method = "poseRightArm", at = @At(value = "HEAD"), cancellable = true, require = 0)
    private void moonlight$poseRightArm(HumanoidRenderState state, CallbackInfo ci) {
        moonlight$poseArm(state, state.rightHandItemStack, true, ci);
    }

    @Inject(method = "poseLeftArm", at = @At(value = "HEAD"), cancellable = true, require = 0)
    private void moonlight$poseLeftArm(HumanoidRenderState state, CallbackInfo ci) {
        moonlight$poseArm(state, state.leftHandItemStack, false, ci);
    }

    @Unique
    private void moonlight$poseArm(HumanoidRenderState state, ItemStack stack, boolean right, CallbackInfo ci) {
        //cancel offhand animation if two-handed so two-handed animation always happens last
        if (this.moonlight$isTwoHanded) ci.cancel();
        IThirdPersonAnimationProvider provider = IThirdPersonAnimationProvider.get(stack.getItem());
        if (provider == null) return;
        HumanoidModel<?> model = (HumanoidModel<?>) (Object) this;
        boolean handled = right ? provider.poseRightArm(stack, model, state, state.mainArm)
                : provider.poseLeftArm(stack, model, state, state.mainArm);
        if (handled) {
            if (provider.isTwoHanded()) moonlight$isTwoHanded = true;
            ci.cancel();
        }
    }

    @Inject(method = "setupAnim*", at = @At(value = "RETURN"), require = 0)
    private void moonlight$resetTwoHanded(HumanoidRenderState state, CallbackInfo ci) {
        this.moonlight$isTwoHanded = false;
    }
}
