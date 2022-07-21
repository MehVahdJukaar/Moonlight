package net.mehvahdjukaar.moonlight.api.events;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.moonlight.api.platform.event.SimpleEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public interface IFireConsumeBlockEvent extends SimpleEvent {

    @ExpectPlatform
    static IFireConsumeBlockEvent create(BlockPos pos, Level level, BlockState state, int chance, int age, Direction face ) {
        throw new AssertionError();
    }

    BlockPos getPos();

    BlockState getState();

    LevelAccessor getLevel();

    Direction getFace();

    int getAge();

    int getChance();

    void setFinalState(BlockState state);

    @Nullable
    BlockState getFinalState();
}
