package net.mehvahdjukaar.moonlight.core.mixins.fabric;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.moonlight.api.platform.fabric.ClientHelperImpl;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {

    @Shadow
    public abstract void render(ItemStack stack, ItemDisplayContext transform, boolean leftHand, PoseStack matrixStack, MultiBufferSource buffer, int light, int overlay, BakedModel model);

    @Shadow
    public abstract BakedModel getModel(ItemStack stack, @Nullable Level level, @Nullable LivingEntity entity, int i);

    @Inject(
            method = "renderGuiItemDecorations(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V",
            at = @At(value = "RETURN")
    )
    private void renderInGui(PoseStack poseStack, Font font, ItemStack stack, int xPosition, int yPosition, String string, CallbackInfo ci) {
        if (!stack.isEmpty()) {
            var decorator = ClientHelperImpl.ITEM_DECORATORS.get(stack.getItem());
            if (decorator != null) {
                decorator.render(font, stack, xPosition, yPosition, poseStack);
            }
        }
    }
}