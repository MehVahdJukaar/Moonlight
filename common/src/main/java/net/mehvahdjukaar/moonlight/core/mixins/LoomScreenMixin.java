package net.mehvahdjukaar.moonlight.core.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.mehvahdjukaar.moonlight.api.client.LoomItemRenderer;
import net.mehvahdjukaar.moonlight.api.item.ILoomItem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.LoomScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.LoomMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Supplier;

@Mixin(LoomScreen.class)
public abstract class LoomScreenMixin extends AbstractContainerScreen<LoomMenu> {

    protected LoomScreenMixin(LoomMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @ModifyExpressionValue(method = "renderBg", at = @At(value = "FIELD", opcode = Opcodes.GETFIELD,
            target = "Lnet/minecraft/client/gui/screens/inventory/LoomScreen;resultBannerPatterns:Lnet/minecraft/world/level/block/entity/BannerPatternLayers;",
            ordinal = 0))
    private BannerPatternLayers moonlight$customLoomPreview(BannerPatternLayers patterns, GuiGraphics graphics,
                                                            float partialTicks, int mouseX, int mouseY) {
        ItemStack banner = this.menu.getBannerSlot().getItem();
        if (!(banner.getItem() instanceof ILoomItem custom)) return patterns;
        Supplier<LoomItemRenderer> factory = custom.getLoomRenderer();
        if (factory == null) return patterns;
        boolean skipVanilla = factory.get().render(graphics, banner, this.menu.getResultSlot().getItem(),
                patterns, this.leftPos, this.topPos, partialTicks);
        graphics.flush();
        return skipVanilla ? null : patterns;
    }

    @ModifyExpressionValue(method = "renderBg", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/inventory/Slot;getItem()Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack moonlight$dodgeBannerItemCast(ItemStack original) {
        if (original.getItem() instanceof ILoomItem custom) {
            return BannerBlock.byColor(custom.getLoomBaseColor(original)).asItem().getDefaultInstance();
        }
        return original;
    }
}
