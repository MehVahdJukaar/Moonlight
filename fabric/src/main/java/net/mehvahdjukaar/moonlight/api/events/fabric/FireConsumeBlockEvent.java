package net.mehvahdjukaar.moonlight.api.events.fabric;

import net.mehvahdjukaar.moonlight.api.events.IFireConsumeBlockEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class FireConsumeBlockEvent implements IFireConsumeBlockEvent {
    @Nullable
    private BlockState newState = null;
    private final BlockState state;
    private final BlockPos pos;
    private final LevelAccessor level;
    private final int chance;
    private final int age;
    private final Direction face;

    public FireConsumeBlockEvent(Level world, BlockPos pos, BlockState state, int chance, int age, Direction face) {
        this.level = world;
        this.pos = pos;
        this.state = state;
        this.age = age;
        this.chance = chance;
        this.face = face;
    }

    @Override
    public BlockPos getPos() {
        return pos;
    }

    @Override
    public BlockState getState() {
        return state;
    }

    @Override
    public LevelAccessor getWorld() {
        return level;
    }

    @Override
    public Direction getFace() {
        return face;
    }

    @Override
    public int getAge() {
        return age;
    }

    @Override
    public int getChance() {
        return chance;
    }

    @Override
    public void setFinalState(BlockState state) {
        this.newState = state;
    }

    @Override
    public @Nullable BlockState getFinalState() {
        return newState;
    }
}
