package net.mehvahdjukaar.moonlight.core.mixins.forge;

import dev.architectury.patchedmixin.staticmixin.spongepowered.asm.mixin.Overwrite;
import net.mehvahdjukaar.moonlight.api.client.model.IExtraModelDataProvider;
import net.mehvahdjukaar.moonlight.api.client.model.forge.ExtraModelDataImpl;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.common.extensions.IForgeBlockEntity;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Objects;

@Mixin(IExtraModelDataProvider.class)
public interface SelfExtraModelDataProvider extends IForgeBlockEntity, IExtraModelDataProvider {

    //overwrite since it already has a default
    @Overwrite
    default void requestModelReload() {
        BlockEntity be = (BlockEntity) this;
        //marks model as dirty
        be.requestModelDataUpdate();
        var level = be.getLevel();
        if (level != null && level.isClientSide) {
            //request re-render immediately
            level.sendBlockUpdated(be.getBlockPos(), be.getBlockState(), be.getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    default ModelData getModelData() {
        if (this.getExtraModelData() instanceof ExtraModelDataImpl data) {
            return data.data();
        }
        return ModelData.EMPTY;
    }

    @Override
    default void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        BlockEntity be = (BlockEntity) this;
        var level = be.getLevel();
        if (level != null && level.isClientSide) {
            var oldData = this.getExtraModelData();
            CompoundTag tag = pkt.getTag();
            //this calls load
            if (tag != null) {
                be.load(tag);
                afterDataPacket(oldData);
            }
        }
    }

}
