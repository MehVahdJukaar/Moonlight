package net.mehvahdjukaar.moonlight.core.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.mehvahdjukaar.moonlight.api.client.LoomItemRenderer;
import net.mehvahdjukaar.moonlight.api.item.ILoomItem;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.LoomScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.LoomMenu;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
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

    @ModifyExpressionValue(method = "extractBackground", at = @At(value = "FIELD", opcode = Opcodes.GETFIELD,
            target = "Lnet/minecraft/client/gui/screens/inventory/LoomScreen;resultBannerPatterns:Lnet/minecraft/world/level/block/entity/BannerPatternLayers;",
            ordinal = 0))
    private BannerPatternLayers moonlight$customLoomPreview(BannerPatternLayers patterns, GuiGraphicsExtractor graphics,
                                                            int mouseX, int mouseY, float partialTicks) {
        ItemStack banner = this.menu.getBannerSlot().getItem();
        if (!(banner.getItem() instanceof ILoomItem custom)) return patterns;
        Supplier<LoomItemRenderer> factory = custom.getLoomRenderer();
        if (factory == null) return patterns;
        boolean skipVanilla = factory.get().render(graphics, banner, this.menu.getResultSlot().getItem(),
                patterns, this.leftPos, this.topPos, partialTicks);
        return skipVanilla ? null : patterns;
    }

    @ModifyExpressionValue(method = "extractBackground", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/inventory/Slot;getItem()Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack moonlight$dodgeBannerItemCast(ItemStack original) {
        if (original.getItem() instanceof ILoomItem custom) {
            DyeColor color = custom.getLoomBaseColor(original);
            return BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace(color.getName() + "_banner")).getDefaultInstance();
        }
        return original;
    }
}
