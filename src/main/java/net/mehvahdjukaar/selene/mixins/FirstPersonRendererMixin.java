package net.mehvahdjukaar.selene.mixins;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.mehvahdjukaar.selene.api.IFirstPersonAnimationProvider;
import net.mehvahdjukaar.selene.map.CustomDecorationHolder;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.FirstPersonRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.world.storage.MapData;
import net.minecraft.world.storage.WorldSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FirstPersonRenderer.class)
public class FirstPersonRendererMixin {

    @Inject(method = "renderArmWithItem", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/FirstPersonRenderer;renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/renderer/model/ItemCameraTransforms$TransformType;ZLcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;I)V",
            ordinal = 1), cancellable = true)
    public void renderItem(AbstractClientPlayerEntity entity, float partialTicks, float pitch, Hand hand, float attackAnim, ItemStack stack, float handHeight, MatrixStack matrixStack, IRenderTypeBuffer buffer, int light, CallbackInfo ci) {
        Item item = stack.getItem();
        if (item instanceof IFirstPersonAnimationProvider) {
            ((IFirstPersonAnimationProvider) item).animateItemFirstPerson(entity, stack, hand, matrixStack, partialTicks, pitch, attackAnim, handHeight);
        }
    }

}
