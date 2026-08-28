package net.mehvahdjukaar.moonlight.core.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.mehvahdjukaar.moonlight.api.events.IDropItemOnDeathEvent;
import net.mehvahdjukaar.moonlight.api.events.MoonlightEventsHelper;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Inventory.class)
public abstract class InventoryMixin {

    @Unique
    private ItemStack moonlight$toRestore = null;

    @WrapOperation(method = "dropAll", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;"))
    private ItemEntity ml$fireDropEvent(Player player, ItemStack stack, boolean dropAround, boolean traceItem,
                                        Operation<ItemEntity> original) {
        if (player.isDeadOrDying() || player.dead) {
            IDropItemOnDeathEvent event = IDropItemOnDeathEvent.create(stack, player, true);
            MoonlightEventsHelper.postEvent(event, IDropItemOnDeathEvent.class);
            if (event.isCanceled()) {
                moonlight$toRestore = event.getReturnItemStack();
                return null;
            }
        }
        return original.call(player, stack, dropAround, traceItem);
    }

    //the slot is cleared right after the drop, so put back whatever the event wanted to keep
    @WrapOperation(method = "dropAll", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/core/NonNullList;set(ILjava/lang/Object;)Ljava/lang/Object;"))
    private Object ml$restoreNotDropped(NonNullList<ItemStack> items, int index, Object emptyStack,
                                        Operation<Object> original) {
        if (moonlight$toRestore != null) {
            Object old = original.call(items, index, moonlight$toRestore);
            moonlight$toRestore = null;
            return old;
        }
        return original.call(items, index, emptyStack);
    }
}
