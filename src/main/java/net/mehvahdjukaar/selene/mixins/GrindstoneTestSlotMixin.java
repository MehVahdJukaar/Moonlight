package net.mehvahdjukaar.selene.mixins;


import net.mehvahdjukaar.selene.common.ModCriteriaTriggers;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = {"net.minecraft.inventory.container.GrindstoneContainer$4"})
public abstract class GrindstoneTestSlotMixin {

    @Inject(method = "onTake(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;)Lnet/minecraft/item/ItemStack;", at = @At("RETURN"), cancellable = true)
    private void onTake(PlayerEntity player, ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
        if(player instanceof ServerPlayerEntity)
            ModCriteriaTriggers.GRIND.trigger((ServerPlayerEntity)player, stack.copy());
    }

}
