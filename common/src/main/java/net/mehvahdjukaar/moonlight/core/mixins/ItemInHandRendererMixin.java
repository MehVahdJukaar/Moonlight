package net.mehvahdjukaar.moonlight.core.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.moonlight.api.item.ClientAnimationExtension;
import net.mehvahdjukaar.moonlight.api.item.IFirstPersonAnimationProvider;
import net.mehvahdjukaar.moonlight.api.item.IFirstPersonSpecialItemRenderer;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {

    @Inject(method = "renderArmWithItem", at = @At(value = "INVOKE",
            ordinal = 1,
            target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;I)V")
    )
    public void moonlight$animateItem(AbstractClientPlayer player, float partialTicks, float pitch,
                                      InteractionHand hand, float swingProgress, ItemStack stack,
                                      float handHeight, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
                                      int combinedLight, CallbackInfo ci) {
        ClientAnimationExtension ext = ClientAnimationExtension.get(stack.getItem());
        IFirstPersonAnimationProvider provider = ext == null ? null : ext.firstPersonAnimation();
        if (provider != null) {
            boolean mainHanded = hand == InteractionHand.MAIN_HAND;
            HumanoidArm arm = mainHanded ? player.getMainArm() : player.getMainArm().getOpposite();
            provider.animateItemFirstPerson(player, stack, hand, arm, poseStack, partialTicks, pitch, swingProgress, handHeight);
        }
    }

    @Inject(method = "renderArmWithItem", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/player/AbstractClientPlayer;isUsingItem()Z",
            ordinal = 1,
            shift = At.Shift.BEFORE), cancellable = true)
    public void moonlight$renderSpecial(AbstractClientPlayer player, float partialTicks, float pitch, InteractionHand hand,
                                        float swingProgress, ItemStack stack, float equippedProgress,
                                        PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int combinedLight, CallbackInfo ci,
                                        @Local HumanoidArm arm) {
        ClientAnimationExtension ext = ClientAnimationExtension.get(stack.getItem());
        IFirstPersonSpecialItemRenderer provider = ext == null ? null : ext.firstPersonRenderer();
        if (provider != null) {
            if (provider.renderFirstPersonItem(player, stack, hand, arm, poseStack, partialTicks, pitch, swingProgress, equippedProgress,
                    submitNodeCollector, combinedLight, (ItemInHandRenderer) (Object) this)) {
                poseStack.popPose();
                ci.cancel();
            }
        }
    }

}
