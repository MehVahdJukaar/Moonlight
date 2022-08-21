package net.mehvahdjukaar.moonlight.core.mixins.fabric;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.moonlight.api.platform.fabric.ClientPlatformHelperImpl;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
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
    public float blitOffset;

    @Shadow
    public abstract void render(ItemStack stack, ItemTransforms.TransformType transform, boolean leftHand, PoseStack matrixStack, MultiBufferSource buffer, int light, int overlay, BakedModel model);

    @Shadow
    public abstract BakedModel getModel(ItemStack p_174265_, @Nullable Level p_174266_, @Nullable LivingEntity p_174267_, int p_174268_);

    @Inject(
            method = "renderGuiItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V",
            at = @At(value = "RETURN")
    )
    private void renderInGui(Font fr, ItemStack stack, int xPosition, int yPosition, String text, CallbackInfo ci) {
        if (!stack.isEmpty()) {
            var decorator = ClientPlatformHelperImpl.ITEM_DECORATORS.get(stack.getItem());
            if (decorator != null) {
                decorator.render(fr, stack, xPosition, yPosition, this.blitOffset);
            }
        }
    }
}