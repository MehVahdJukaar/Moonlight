package net.mehvahdjukaar.moonlight.core.mixins;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.mehvahdjukaar.moonlight.api.events.IDropItemOnDeathEvent;
import net.mehvahdjukaar.moonlight.api.events.MoonlightEventsHelper;
import net.minecraft.world.entity.EntityEquipment;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.EnumMap;
import java.util.Map;

@Mixin(EntityEquipment.class)
public abstract class EntityEquipmentMixin {

    @Shadow
    @Final
    private EnumMap<EquipmentSlot, ItemStack> items;

    @Inject(method = "dropAll", at = @At("HEAD"))
    private void ml$fireDropEvent(LivingEntity dropper, CallbackInfo ci,
                                  @Share("toRestore") LocalRef<Map<EquipmentSlot, ItemStack>> toRestore) {
        if ((dropper instanceof Player player) && (player.isDeadOrDying() || player.dead)) {
            //needed since Inventory.dropAll only firest for main items only and not equipment
            for (var e : this.items.entrySet()) {
                ItemStack stack = e.getValue();
                if (stack.isEmpty()) continue;

                IDropItemOnDeathEvent event = IDropItemOnDeathEvent.create(stack, player, true);
                MoonlightEventsHelper.postEvent(event, IDropItemOnDeathEvent.class);
                if (event.isCanceled()) {
                    e.setValue(ItemStack.EMPTY);
                    if (toRestore.get() == null) toRestore.set(new EnumMap<>(EquipmentSlot.class));
                    toRestore.get().put(e.getKey(), event.getReturnItemStack());
                }
            }
        }
    }

    @Inject(method = "dropAll", at = @At("TAIL"))
    private void ml$restoreNotDropped(LivingEntity dropper, CallbackInfo ci,
                                      @Share("toRestore") LocalRef<Map<EquipmentSlot, ItemStack>> toRestore) {
        if (toRestore.get() != null){
            this.items.putAll(toRestore.get());
        }
    }
}
