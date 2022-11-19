package net.mehvahdjukaar.moonlight.api.events.fabric;

import net.mehvahdjukaar.moonlight.api.events.ILightningStruckBlockEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public record LightningStruckBlockEvent(BlockState state, LevelAccessor level, BlockPos pos,
                                        LightningBolt entity) implements ILightningStruckBlockEvent {

    @Override
    public BlockPos getPos() {
        return pos;
    }

    @Override
    public BlockState getState() {
        return state;
    }

    @Override
    public LightningBolt getEntity() {
        return entity;
    }

    @Override
    public LevelAccessor getLevel() {
        return level;
    }
}
