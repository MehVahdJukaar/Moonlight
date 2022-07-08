package net.mehvahdjukaar.moonlight.api.events.fabric;

import net.mehvahdjukaar.moonlight.api.events.IFireConsumeBlockEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class IFireConsumeBlockEventImpl {

   public static IFireConsumeBlockEvent create(BlockPos pos, Level level, BlockState state, int chance, int age, Direction face){
        return new FireConsumeBlockEvent(level, pos, state, chance, age, face);
    }
}
