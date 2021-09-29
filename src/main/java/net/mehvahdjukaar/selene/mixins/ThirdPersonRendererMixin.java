package net.mehvahdjukaar.selene.mixins;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.mehvahdjukaar.selene.api.IThirdPersonAnimationProvider;
import net.mehvahdjukaar.selene.util.TwoHandedAnimation;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.FirstPersonRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.entity.model.AgeableModel;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BipedModel.class)
public abstract class ThirdPersonRendererMixin<T extends LivingEntity> extends AgeableModel<T> {

    public TwoHandedAnimation animationType = new TwoHandedAnimation();

    @Inject(method = "poseRightArm", at = @At(value = "HEAD"), cancellable = true)
    public void poseRightArm(T entity, CallbackInfo ci) {
        //cancel off hand animation if two handed so two handed animation always happens last
        if (this.animationType.isTwoHanded()) ci.cancel();
        HandSide handSide = entity.getMainArm();
        ItemStack stack = entity.getItemInHand(handSide == HandSide.RIGHT ? Hand.MAIN_HAND : Hand.OFF_HAND);
        Item item = stack.getItem();
        if (item instanceof IThirdPersonAnimationProvider) {
            if (((IThirdPersonAnimationProvider) item).poseRightArmGeneric(stack, this, entity, handSide, this.animationType)) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "poseLeftArm", at = @At(value = "HEAD"), cancellable = true)
    public void poseLeftArm(T entity, CallbackInfo ci) {
        //cancel off hand animation if two handed so two handed animation always happens last
        if (this.animationType.isTwoHanded()) ci.cancel();
        HandSide handSide = entity.getMainArm();
        ItemStack stack = entity.getItemInHand(handSide == HandSide.RIGHT ? Hand.OFF_HAND : Hand.MAIN_HAND);
        Item item = stack.getItem();
        if (item instanceof IThirdPersonAnimationProvider) {
            if (((IThirdPersonAnimationProvider) item).poseLeftArmGeneric(stack, this, entity, handSide, this.animationType)) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "setupAnim", at = @At(value = "RETURN"), cancellable = true)
    public void setupAnim(T p_225597_1_, float p_225597_2_, float p_225597_3_, float p_225597_4_, float p_225597_5_, float p_225597_6_, CallbackInfo ci) {
        this.animationType.setTwoHanded(false);
    }

}
