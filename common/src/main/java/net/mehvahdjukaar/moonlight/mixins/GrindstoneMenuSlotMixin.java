package net.mehvahdjukaar.moonlight.mixins;


import net.mehvahdjukaar.moonlight.misc.ModCriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = {"net.minecraft.world.inventory.GrindstoneMenu$4"})
public abstract class GrindstoneMenuSlotMixin {


    @Inject(method = {"onTake"}, at = @At("RETURN"))
    private void onTake(Player player, ItemStack stack, CallbackInfo ci) {
        if(player instanceof ServerPlayer)
            ModCriteriaTriggers.GRIND.trigger((ServerPlayer)player, stack.copy());
    }

}
