package net.mehvahdjukaar.moonlight.api.events.forge;

import net.mehvahdjukaar.moonlight.api.events.IFireConsumeBlockEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.level.BlockEvent;
import org.jetbrains.annotations.Nullable;

public class FireConsumeBlockEvent extends BlockEvent implements IFireConsumeBlockEvent {

    private BlockState newState = null;

    private final int chance;
    private final int age;
    private final Direction face;

    public FireConsumeBlockEvent(Level world, BlockPos pos, BlockState state, int chance, int age, Direction face) {
        super(world, pos, state);
        this.age = age;
        this.chance = chance;
        this.face = face;
    }

    @Override
    public int getChance() {
        return chance;
    }

    @Override
    public int getAge() {
        return age;
    }

    @Override
    public Direction getFace() {
        return face;
    }

    @Override
    public void setFinalState(BlockState state) {
        this.newState = state;
    }
    @Nullable
    @Override
    public BlockState getFinalState() {
        return newState;
    }
}
