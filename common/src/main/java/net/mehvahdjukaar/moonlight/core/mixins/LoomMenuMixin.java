package net.mehvahdjukaar.moonlight.core.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.mehvahdjukaar.moonlight.api.item.ILoomItem;
import net.minecraft.world.inventory.LoomMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LoomMenu.class)
public abstract class LoomMenuMixin {

    @ModifyExpressionValue(method = "quickMoveStack", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;getItem()Lnet/minecraft/world/item/Item;", ordinal = 0))
    private Item moonlight$shiftClickCustomBanners(Item original) {
        if (original instanceof ILoomItem) return Items.WHITE_BANNER;
        return original;
    }
}
