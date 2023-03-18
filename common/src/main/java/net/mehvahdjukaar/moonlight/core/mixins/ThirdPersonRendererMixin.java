package net.mehvahdjukaar.moonlight.core.mixins;

import net.mehvahdjukaar.moonlight.api.item.IThirdPersonAnimationProvider;
import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
public abstract class ThirdPersonRendererMixin<T extends LivingEntity> extends AgeableListModel<T> {

    @Unique
    private boolean isTwoHanded = false;

    @Inject(method = "poseRightArm", at = @At(value = "HEAD"), cancellable = true, require = 0)
    public void poseRightArm(T entity, CallbackInfo ci) {
        //cancel offhand animation if two-handed so two-handed animation always happens last
        if (this.isTwoHanded) ci.cancel();
        HumanoidArm handSide = entity.getMainArm();
        ItemStack stack = entity.getItemInHand(handSide == HumanoidArm.RIGHT ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
        Item item = stack.getItem();
        if (item instanceof IThirdPersonAnimationProvider thirdPersonAnimationProvider) {
            if (thirdPersonAnimationProvider.poseRightArm(stack, (HumanoidModel<T>) (Object) this, entity, handSide)) {
                if (thirdPersonAnimationProvider.isTwoHanded()) isTwoHanded = true;
                ci.cancel();
            }
        }
    }

    @Inject(method = "poseLeftArm", at = @At(value = "HEAD"), cancellable = true, require = 0)
    public void poseLeftArm(T entity, CallbackInfo ci) {
        //cancel offhand animation if two-handed so two-handed animation always happens last
        if (this.isTwoHanded) ci.cancel();
        HumanoidArm handSide = entity.getMainArm();
        ItemStack stack = entity.getItemInHand(handSide == HumanoidArm.RIGHT ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
        Item item = stack.getItem();
        if (item instanceof IThirdPersonAnimationProvider thirdPersonAnimationProvider) {
            if (thirdPersonAnimationProvider.poseLeftArm(stack, (HumanoidModel<T>) (Object) this, entity, handSide)) {
                if (thirdPersonAnimationProvider.isTwoHanded()) isTwoHanded = true;
                ci.cancel();
            }
        }
    }

    @Inject(method = "setupAnim*", at = @At(value = "RETURN"), require = 0)
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        this.isTwoHanded = false;
    }
}
