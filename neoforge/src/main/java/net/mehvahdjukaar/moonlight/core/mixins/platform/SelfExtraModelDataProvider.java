package net.mehvahdjukaar.moonlight.core.mixins.platform;

import net.mehvahdjukaar.moonlight.api.client.model.IExtraModelDataProvider;
import net.mehvahdjukaar.moonlight.api.client.model.platform.ExtraModelDataImpl;
import net.minecraft.network.Connection;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.neoforged.neoforge.model.data.ModelData;
import net.neoforged.neoforge.common.extensions.IBlockEntityExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(IExtraModelDataProvider.class)
public interface SelfExtraModelDataProvider extends IBlockEntityExtension, IExtraModelDataProvider {

    //overwrite since it already has a default
    /**
     * @author me
     * @reason it's my own class!
     */
    @Overwrite
    default void requestModelReload() {
        BlockEntity be = (BlockEntity) this;
        //marks model as dirty
        be.requestModelDataUpdate();
        var level = be.getLevel();
        if (level != null && level.isClientSide()) {
            //request re-render immediately
            level.sendBlockUpdated(be.getBlockPos(), be.getBlockState(), be.getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    default ModelData getModelData() {
        if (this.getExtraModelData() instanceof ExtraModelDataImpl(ModelData data1)) {
            return data1;
        }
        return ModelData.EMPTY;
    }

    @Override
    default void onDataPacket(Connection net, ValueInput valueInput) {
        BlockEntity be = (BlockEntity) this;
        var level = be.getLevel();
        if (level != null && level.isClientSide()) {
            var oldData = this.getExtraModelData();
            //this calls load
            IBlockEntityExtension.super.onDataPacket(net, valueInput);
            afterDataPacket(oldData);
        } else {
            IBlockEntityExtension.super.onDataPacket(net, valueInput);
        }
    }

}
