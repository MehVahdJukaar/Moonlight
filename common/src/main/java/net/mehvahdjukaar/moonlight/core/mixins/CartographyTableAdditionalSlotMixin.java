package net.mehvahdjukaar.moonlight.core.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.mehvahdjukaar.moonlight.api.util.CartographyTableHelper;
import net.minecraft.world.inventory.CartographyTableMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net.minecraft.world.inventory.CartographyTableMenu$4")
public abstract class CartographyTableAdditionalSlotMixin {

    @ModifyReturnValue(method = "mayPlace(Lnet/minecraft/world/item/ItemStack;)Z", at = @At("RETURN"))
    private boolean moonlight$allowCustomAdditionalSlotItems(boolean original, ItemStack stack) {
        return original || CartographyTableHelper.isAllowed(CartographyTableMenu.ADDITIONAL_SLOT, stack);
    }
}
