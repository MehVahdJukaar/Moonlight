package net.mehvahdjukaar.moonlight.core.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.mehvahdjukaar.moonlight.api.client.LoomItemRenderer;
import net.mehvahdjukaar.moonlight.api.item.ILoomItem;
import net.mehvahdjukaar.moonlight.core.misc.LoomSlotIcons;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CyclingSlotBackground;
import net.minecraft.client.gui.screens.inventory.LoomScreen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.LoomMenu;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
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

    @WrapWithCondition(method = "extractBackground", at = @At(value = "INVOKE", ordinal = 0,
            target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V"))
    private boolean moonlight$skipVanillaBannerIcon(GuiGraphicsExtractor graphics, RenderPipeline pipeline,
                                                    Identifier sprite, int x, int y, int width, int height) {
        return !LoomSlotIcons.hasCustom();
    }

    @Inject(method = "extractBackground", at = @At("TAIL"))
    private void moonlight$cycleBannerSlotIcons(GuiGraphicsExtractor graphics, int mouseX, int mouseY,
                                                float partialTicks, CallbackInfo ci) {
        if (LoomSlotIcons.hasCustom()) {
            this.moonlight$bannerSlotIcons.extractRenderState(this.menu, graphics, partialTicks, this.leftPos, this.topPos);
        }
    }

    @ModifyExpressionValue(method = "extractBackground", at = @At(value = "FIELD", opcode = Opcodes.GETFIELD,
            target = "Lnet/minecraft/client/gui/screens/inventory/LoomScreen;resultBannerPatterns:Lnet/minecraft/world/level/block/entity/BannerPatternLayers;",
            ordinal = 0))
    private BannerPatternLayers moonlight$customLoomPreview(BannerPatternLayers patterns, GuiGraphicsExtractor graphics,
                                                            int mouseX, int mouseY, float partialTicks) {
        ItemStack banner = this.menu.getBannerSlot().getItem();
        LoomItemRenderer renderer = moonlight$rendererFor(banner);
        if (renderer == null) return patterns;
        boolean skipVanilla = renderer.render(graphics, banner, this.menu.getResultSlot().getItem(),
                patterns, this.leftPos, this.topPos, partialTicks);
        return skipVanilla ? null : patterns;
    }

    @WrapWithCondition(method = "extractBackground", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/inventory/LoomScreen;extractBannerOnButton(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IILnet/minecraft/client/renderer/texture/TextureAtlasSprite;)V"))
    private boolean moonlight$customPatternIcon(LoomScreen instance, GuiGraphicsExtractor graphics, int x, int y,
                                                TextureAtlasSprite sprite, @Local Holder<BannerPattern> pattern) {
        ItemStack banner = this.menu.getBannerSlot().getItem();
        LoomItemRenderer renderer = moonlight$rendererFor(banner);
        if (renderer == null) return true;
        return !renderer.renderPatternIcon(graphics, banner, pattern, x, y);
    }

    @Nullable
    private static LoomItemRenderer moonlight$rendererFor(ItemStack stack) {
        if (!(stack.getItem() instanceof ILoomItem custom)) return null;
        Supplier<LoomItemRenderer> factory = custom.getLoomRenderer();
        return factory == null ? null : factory.get();
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
