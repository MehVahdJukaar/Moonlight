package net.mehvahdjukaar.moonlight.api.events.forge;

import net.mehvahdjukaar.moonlight.api.events.ILightningStruckBlockEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.level.BlockEvent;

public class LightningStruckBlockEvent extends BlockEvent implements ILightningStruckBlockEvent {

    private final LightningBolt entity;

    public LightningStruckBlockEvent(BlockState state, LevelAccessor level, BlockPos pos, LightningBolt entity) {
        super(level, pos, state);
        this.entity = entity;
    }

    @Override
    public LightningBolt getEntity() {
        return entity;
    }
}
