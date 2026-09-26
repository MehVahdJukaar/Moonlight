package net.mehvahdjukaar.moonlight.core.mixins;

import net.mehvahdjukaar.moonlight.api.util.CartographyTableHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CartographyTableScreen;
import net.minecraft.client.gui.screens.inventory.CyclingSlotBackground;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.CartographyTableMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CartographyTableScreen.class)
public abstract class CartographyTableScreenMixin extends AbstractContainerScreen<CartographyTableMenu> {

    @Unique
    private final CyclingSlotBackground moonlight$mapSlotIcons = new CyclingSlotBackground(CartographyTableMenu.MAP_SLOT);
    @Unique
    private final CyclingSlotBackground moonlight$additionalSlotIcons = new CyclingSlotBackground(CartographyTableMenu.ADDITIONAL_SLOT);

    protected CartographyTableScreenMixin(CartographyTableMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (CartographyTableHelper.hasCustomIcons()) {
            this.moonlight$mapSlotIcons.tick(CartographyTableHelper.getSlotIcons(CartographyTableMenu.MAP_SLOT));
            this.moonlight$additionalSlotIcons.tick(CartographyTableHelper.getSlotIcons(CartographyTableMenu.ADDITIONAL_SLOT));
        }
    }

    @Inject(method = "renderBg", at = @At("TAIL"))
    private void moonlight$cycleSlotIcons(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY,
                                          CallbackInfo ci) {
        if (CartographyTableHelper.hasCustomIcons()) {
            this.moonlight$mapSlotIcons.render(this.menu, graphics, partialTicks, this.leftPos, this.topPos);
            this.moonlight$additionalSlotIcons.render(this.menu, graphics, partialTicks, this.leftPos, this.topPos);
        }
    }
}
