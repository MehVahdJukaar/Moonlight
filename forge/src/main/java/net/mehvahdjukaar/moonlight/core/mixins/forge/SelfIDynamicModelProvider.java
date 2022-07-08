package net.mehvahdjukaar.moonlight.core.mixins.forge;

import net.mehvahdjukaar.moonlight.api.client.model.IExtraModelDataProvider;
import net.mehvahdjukaar.moonlight.api.client.model.forge.ExtraModelDataImpl;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.ModelDataManager;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.common.extensions.IForgeBlockEntity;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Objects;

@Mixin(IExtraModelDataProvider.class)
public interface SelfIDynamicModelProvider extends IForgeBlockEntity, IExtraModelDataProvider {

    @Override
    default IModelData getModelData() {
        if (this.getExtraModelData() instanceof ExtraModelDataImpl data) {
            return data.getData();
        }
        return EmptyModelData.INSTANCE;
    }

    @Override
    default void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        //TODO:check

        var oldData = this.getExtraModelData();
        CompoundTag tag = pkt.getTag();
        //this calls load
        handleUpdateTag(tag);
        if (!Objects.equals(oldData, this.getExtraModelData())) {
            BlockEntity be = (BlockEntity)this;
            //not needed cause model data doesn't create new obj. updating old one instead
            ModelDataManager.requestModelDataRefresh(be);
            //this.data.setData(MIMIC, this.getHeldBlock());
            be.getLevel().sendBlockUpdated(be.getBlockPos(), be.getBlockState(), be.getBlockState(), Block.UPDATE_CLIENTS);
        }
    }
}
