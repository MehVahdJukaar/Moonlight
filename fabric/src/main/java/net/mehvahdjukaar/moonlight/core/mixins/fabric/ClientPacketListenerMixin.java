package net.mehvahdjukaar.moonlight.core.mixins.fabric;

import net.mehvahdjukaar.moonlight.api.client.model.ExtraModelData;
import net.mehvahdjukaar.moonlight.api.client.model.IExtraModelDataProvider;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {

    @Unique
    private ExtraModelData captured = null;

    @Inject(method = "method_38542",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/entity/BlockEntity;load(Lnet/minecraft/nbt/CompoundTag;)V",
                    shift = At.Shift.BEFORE))
    public void beforeLoad(ClientboundBlockEntityDataPacket clientboundBlockEntityDataPacket, BlockEntity blockEntity, CallbackInfo ci) {
        if (blockEntity instanceof IExtraModelDataProvider data) {
            captured = data.getExtraModelData();
        } else captured = null;
    }

    @Inject(method = "method_38542",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/entity/BlockEntity;load(Lnet/minecraft/nbt/CompoundTag;)V",
                    shift = At.Shift.AFTER))
    public void afterLoad(ClientboundBlockEntityDataPacket clientboundBlockEntityDataPacket, BlockEntity blockEntity, CallbackInfo ci) {
        if (captured != null && blockEntity instanceof IExtraModelDataProvider data) {
            data.afterDataPacket(captured);
        }
    }
}
