package net.mehvahdjukaar.moonlight.api.events;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public interface ILightningStruckBlockEvent extends SimpleEvent {

    @ExpectPlatform
    static ILightningStruckBlockEvent create(BlockState state, LevelAccessor level, BlockPos pos, LightningBolt entity) {
        throw new AssertionError();
    }

    BlockPos getPos();

    BlockState getState();

    LevelAccessor getLevel();

    LightningBolt getEntity();
}
