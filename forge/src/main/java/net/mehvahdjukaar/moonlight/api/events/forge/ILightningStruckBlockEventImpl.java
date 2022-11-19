package net.mehvahdjukaar.moonlight.api.events.forge;

import net.mehvahdjukaar.moonlight.api.events.IFireConsumeBlockEvent;
import net.mehvahdjukaar.moonlight.api.events.ILightningStruckBlockEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public class ILightningStruckBlockEventImpl {
    public static ILightningStruckBlockEvent create(BlockState state, LevelAccessor level, BlockPos pos, LightningBolt entity) {
        return new LightningStruckBlockEvent(state, level, pos, entity);
    }

}
