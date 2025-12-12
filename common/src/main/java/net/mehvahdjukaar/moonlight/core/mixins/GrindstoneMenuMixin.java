package net.mehvahdjukaar.moonlight.core.mixins;


import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(GrindstoneMenu.class)
public abstract class GrindstoneMenuMixin {

    @ModifyArg(method = {"quickMoveStack"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/Slot;onTake(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;)V"))
    private ItemStack ml$fixOnTake_dontCare(ItemStack original, @Local(ordinal = 0) ItemStack actualStack) {
       return actualStack;
    }

}
