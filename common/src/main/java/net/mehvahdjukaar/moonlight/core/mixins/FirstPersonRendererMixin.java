package net.mehvahdjukaar.moonlight.core.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.moonlight.api.item.IFirstPersonAnimationProvider;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemInHandRenderer.class)
public abstract class FirstPersonRendererMixin {

    @WrapOperation(method = "renderArmWithItem", at = @At(value = "INVOKE",
            ordinal = 1,
            target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"),
            require = 0)
    public void renderItem(ItemInHandRenderer instance, LivingEntity entity, ItemStack stack, ItemDisplayContext displayContext,
                           boolean leftHand, PoseStack poseStack, MultiBufferSource buffer, int seed, Operation<Void> operation,
                           @Local InteractionHand hand, @Local(ordinal = 0) float partialTicks,  @Local(ordinal = 1) float pitch,
                           @Local(ordinal = 2) float attackAnim, @Local(ordinal = 3) float handHeight,
                           @Local(ordinal = 0) int combinedLight) {
        IFirstPersonAnimationProvider provider = IFirstPersonAnimationProvider.get(stack.getItem());
        if (provider != null) {
            provider.animateItemFirstPerson(entity, stack, hand, poseStack, partialTicks, pitch, attackAnim, handHeight);
            if (provider.renderFirstPersonItem(entity, stack, hand, poseStack, partialTicks, buffer, combinedLight)) {
                return;
            }
        }
        operation.call(instance, entity, stack, displayContext, leftHand, poseStack, buffer, seed);
    }

}
