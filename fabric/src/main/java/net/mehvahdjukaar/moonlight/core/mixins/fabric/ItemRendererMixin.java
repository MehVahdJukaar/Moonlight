package net.mehvahdjukaar.moonlight.core.mixins.fabric;

import net.mehvahdjukaar.moonlight.api.platform.fabric.ClientHelperImpl;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiGraphics.class)
public abstract class ItemRendererMixin {

    @Inject(
            method = "renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V",
            at = @At(value = "RETURN")
    )
    private void renderInGui(Font font, ItemStack stack, int xPosition, int yPosition, String string, CallbackInfo ci) {
        if (!stack.isEmpty()) {
            var decorator = ClientHelperImpl.ITEM_DECORATORS.get(stack.getItem());
            if (decorator != null) {
                decorator.render((GuiGraphics) (Object) this, font, stack, xPosition, yPosition);
            }
        }
    }
}