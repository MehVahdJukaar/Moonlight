package net.mehvahdjukaar.moonlight.core.mixins.fabric;

import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity;
import net.mehvahdjukaar.moonlight.api.client.model.ExtraModelData;
import net.mehvahdjukaar.moonlight.api.client.model.IExtraModelDataProvider;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Objects;

@Mixin(IExtraModelDataProvider.class)
public interface SelfIExtraModelDataProvider extends RenderAttachmentBlockEntity, IExtraModelDataProvider {

    @Override
    default Object getRenderAttachmentData() {
        return this.getExtraModelData();
    }

    @Override
    default void requestModelReload() {
        BlockEntity be = (BlockEntity) this;
        if (be.getLevel() instanceof ClientLevel clientLevel) {
            //request re-render immediately
            clientLevel.sendBlockUpdated(be.getBlockPos(), be.getBlockState(), be.getBlockState(), Block.UPDATE_CLIENTS);
            // var section = SectionPos.of(be.getBlockPos());
            // clientLevel.setSectionDirtyWithNeighbors(section.x(),section.y(),section.z());
        }
    }
}
