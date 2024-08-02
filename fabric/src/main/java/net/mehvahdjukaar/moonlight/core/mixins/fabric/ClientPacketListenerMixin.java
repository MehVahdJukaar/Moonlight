package net.mehvahdjukaar.moonlight.core.mixins.fabric;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.mehvahdjukaar.moonlight.api.client.model.ExtraModelData;
import net.mehvahdjukaar.moonlight.api.client.model.IExtraModelDataProvider;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {

    @Inject(method = "method_38542",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/entity/BlockEntity;loadWithComponents(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/core/HolderLookup$Provider;)V",
                    shift = At.Shift.BEFORE))
    public void moonlight$storeOldModelData(ClientboundBlockEntityDataPacket clientboundBlockEntityDataPacket, BlockEntity blockEntity,
                                            CallbackInfo ci, @Share("oldData") LocalRef<ExtraModelData> oldData) {
        if (blockEntity instanceof IExtraModelDataProvider data) {
            oldData.set(data.getExtraModelData());
        } else oldData.set( null);
    }

    @Inject(method = "method_38542",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/entity/BlockEntity;loadWithComponents(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/core/HolderLookup$Provider;)V",
                    shift = At.Shift.AFTER))
    public void moonlight$callModelDataCallback(ClientboundBlockEntityDataPacket clientboundBlockEntityDataPacket, BlockEntity blockEntity,
                          CallbackInfo ci, @Share("oldData") LocalRef<ExtraModelData> oldData) {
        var d = oldData.get();
        if (d != null && blockEntity instanceof IExtraModelDataProvider data) {
            data.afterDataPacket(d);
        }
    }
}
