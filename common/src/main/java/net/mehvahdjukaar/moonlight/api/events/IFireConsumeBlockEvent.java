package net.mehvahdjukaar.moonlight.api.events;

import net.mehvahdjukaar.candlelight.api.PlatformImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public interface IFireConsumeBlockEvent extends SimpleEvent {

    @PlatformImpl
    static IFireConsumeBlockEvent create(BlockPos pos, Level level, BlockState state, int chance, int age, Direction face, boolean wasReplacedByFire) {
        throw new AssertionError();
    }

    BlockPos getPos();

    BlockState getState();

    LevelAccessor getLevel();

    Direction getFace();

    int getAge();

    int getChance();

    /**
     * If a fire block will replace this block
     */
    boolean wasReplacedByFire();

    void setFinalState(BlockState state);

    @Nullable
    BlockState getFinalState();
}
