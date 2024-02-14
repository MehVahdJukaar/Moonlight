package net.mehvahdjukaar.moonlight.core.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.moonlight.api.item.IFirstPersonAnimationProvider;
import net.mehvahdjukaar.moonlight.api.item.IFirstPersonSpecialItemRenderer;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {

    @Inject(method = "renderArmWithItem", at = @At(value = "INVOKE",
            ordinal = 1,
            target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"),
            require = 0)
    public void moonlight$animateItem(AbstractClientPlayer player, float partialTicks, float pitch,
                                      InteractionHand hand, float swingProgress, ItemStack stack,
                                      float handHeight, PoseStack poseStack, MultiBufferSource buffer,
                                      int combinedLight, CallbackInfo ci) {
        IFirstPersonAnimationProvider provider = IFirstPersonAnimationProvider.get(stack.getItem());
        if (provider != null) {
            provider.animateItemFirstPerson(player, stack, hand, poseStack, partialTicks, pitch, swingProgress, handHeight);
        }
    }

    @Inject(method = "renderArmWithItem", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/player/AbstractClientPlayer;isUsingItem()Z",
            ordinal = 1,
            shift = At.Shift.BEFORE))
    public void moonlight$renderSpecial(AbstractClientPlayer player, float partialTicks, float pitch, InteractionHand hand,
                                        float swingProgress, ItemStack stack, float equippedProgress,
                                        PoseStack poseStack, MultiBufferSource buffer, int combinedLight, CallbackInfo ci) {
        IFirstPersonSpecialItemRenderer provider = IFirstPersonSpecialItemRenderer.get(stack.getItem());
        if (provider != null) {
            provider.renderFirstPersonItem(player, stack, hand, poseStack, partialTicks, pitch, swingProgress, equippedProgress,
                   buffer, combinedLight, (ItemInHandRenderer) (Object) this);
        }
    }

}
