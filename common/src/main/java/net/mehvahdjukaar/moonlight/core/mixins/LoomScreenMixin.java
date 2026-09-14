package net.mehvahdjukaar.moonlight.core.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.WrapWithCondition;
import net.mehvahdjukaar.moonlight.api.client.LoomItemRenderer;
import net.mehvahdjukaar.moonlight.api.item.ILoomItem;
import net.mehvahdjukaar.moonlight.core.misc.LoomSlotIcons;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CyclingSlotBackground;
import net.minecraft.client.gui.screens.inventory.LoomScreen;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.LoomMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(LoomScreen.class)
public abstract class LoomScreenMixin extends AbstractContainerScreen<LoomMenu> {

    @Unique
    private final CyclingSlotBackground moonlight$bannerSlotIcons = new CyclingSlotBackground(0);

    protected LoomScreenMixin(LoomMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (LoomSlotIcons.hasCustom()) this.moonlight$bannerSlotIcons.tick(LoomSlotIcons.get());
    }

    @WrapWithCondition(method = "renderBg", at = @At(value = "INVOKE", ordinal = 0,
            target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lnet/minecraft/resources/ResourceLocation;IIII)V"))
    private boolean moonlight$skipVanillaBannerIcon(GuiGraphics graphics, ResourceLocation sprite,
                                                    int x, int y, int width, int height) {
        return !LoomSlotIcons.hasCustom();
    }

    @Inject(method = "renderBg", at = @At("TAIL"))
    private void moonlight$cycleBannerSlotIcons(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY,
                                                CallbackInfo ci) {
        if (LoomSlotIcons.hasCustom()) {
            this.moonlight$bannerSlotIcons.render(this.menu, graphics, partialTicks, this.leftPos, this.topPos);
        }
    }

    @ModifyExpressionValue(method = "renderBg", at = @At(value = "FIELD", opcode = Opcodes.GETFIELD,
            target = "Lnet/minecraft/client/gui/screens/inventory/LoomScreen;resultBannerPatterns:Lnet/minecraft/world/level/block/entity/BannerPatternLayers;",
            ordinal = 0))
    private BannerPatternLayers moonlight$customLoomPreview(BannerPatternLayers patterns, GuiGraphics graphics,
                                                            float partialTicks, int mouseX, int mouseY) {
        ItemStack banner = this.menu.getBannerSlot().getItem();
        LoomItemRenderer renderer = moonlight$rendererFor(banner);
        if (renderer == null) return patterns;
        boolean skipVanilla = renderer.render(graphics, banner, this.menu.getResultSlot().getItem(),
                patterns, this.leftPos, this.topPos, partialTicks);
        graphics.flush();
        return skipVanilla ? null : patterns;
    }

    @Inject(method = "renderPattern", at = @At("HEAD"), cancellable = true)
    private void moonlight$customPatternIcon(GuiGraphics graphics, Holder<BannerPattern> pattern, int x, int y,
                                             CallbackInfo ci) {
        LoomItemRenderer renderer = moonlight$rendererFor(this.menu.getBannerSlot().getItem());
        if (renderer == null) return;
        if (renderer.renderPatternIcon(graphics, this.menu.getBannerSlot().getItem(), pattern, x, y)) {
            graphics.flush();
            ci.cancel();
        }
    }

    @Nullable
    private static LoomItemRenderer moonlight$rendererFor(ItemStack stack) {
        if (!(stack.getItem() instanceof ILoomItem custom)) return null;
        Supplier<LoomItemRenderer> factory = custom.getLoomRenderer();
        return factory == null ? null : factory.get();
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
