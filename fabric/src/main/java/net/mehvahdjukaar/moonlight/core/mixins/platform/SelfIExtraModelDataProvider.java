package net.mehvahdjukaar.moonlight.core.mixins.platform;

import net.fabricmc.fabric.api.blockgetter.v2.RenderDataBlockEntity;
import net.mehvahdjukaar.moonlight.api.client.model.IExtraModelDataProvider;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(IExtraModelDataProvider.class)
public interface SelfIExtraModelDataProvider extends RenderDataBlockEntity, IExtraModelDataProvider {

    @Override
    default Object getRenderData() {
        return this.getExtraModelData();
    }

    @Override
    default void requestModelReload() {
        BlockEntity be = (BlockEntity) this;
        if (be.getLevel() instanceof ClientLevel clientLevel) {
            //request re-render immediately
            clientLevel.sendBlockUpdated(be.getBlockPos(), be.getBlockState(), be.getBlockState(), Block.UPDATE_CLIENTS);
        }
    }
}
