package net.mehvahdjukaar.moonlight.core.mixins;

import net.mehvahdjukaar.moonlight.api.events.IDropItemOnDeathEvent;
import net.mehvahdjukaar.moonlight.api.events.MoonlightEventsHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;
import java.util.List;

@Mixin(Inventory.class)
public abstract class InventoryMixin {

    @Shadow @Final public Player player;

    @Unique
    private ItemStack moonlight$toRestore = null;


    @Inject(method = "dropAll", at = @At(value = "INVOKE", target = "Ljava/util/List;get(I)Ljava/lang/Object;",
            shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILHARD)
    public void fireDropEvent(CallbackInfo ci, Iterator var1, List<ItemStack> list, int i) {
        if(this.player.isDeadOrDying() || this.player.dead){
            ItemStack stack = list.get(i);
            IDropItemOnDeathEvent event = IDropItemOnDeathEvent.create(stack, player, true);
            MoonlightEventsHelper.postEvent( event, IDropItemOnDeathEvent.class);
            if(event.isCanceled()){
                list.set(i, ItemStack.EMPTY);
                moonlight$toRestore = event.getReturnItemStack();
            }
        }
    }

    @Inject(method = "dropAll", at = @At(value = "INVOKE", target = "Ljava/util/List;get(I)Ljava/lang/Object;",
            shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
    public void restoreNotDropped(CallbackInfo ci, Iterator var1, List<ItemStack> list, int i) {
        if(moonlight$toRestore != null){
            list.set(i, moonlight$toRestore);
            moonlight$toRestore = null;
        }
    }
}