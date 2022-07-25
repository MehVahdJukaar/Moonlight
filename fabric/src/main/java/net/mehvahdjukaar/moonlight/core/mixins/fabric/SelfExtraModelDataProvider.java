package net.mehvahdjukaar.moonlight.core.mixins.fabric;

import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity;
import net.mehvahdjukaar.moonlight.api.client.model.IExtraModelDataProvider;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(IExtraModelDataProvider.class)
public interface SelfExtraModelDataProvider extends RenderAttachmentBlockEntity, IExtraModelDataProvider {

    @Override
    default Object getRenderAttachmentData() {
        return this.getExtraModelData();
    }

    @Override
    default void requestModelReload() {
        BlockEntity tile = ((BlockEntity)this);
        if (tile.getLevel() instanceof ClientLevel clientLevel) {
            var section = SectionPos.of(tile.getBlockPos());
            clientLevel.setSectionDirtyWithNeighbors(section.x(),section.y(),section.z());
        }
    }
}
