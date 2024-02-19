package net.mehvahdjukaar.moonlight.api.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public interface IRecolorable {

    boolean tryRecolor(Level level, BlockPos pos, BlockState state,@Nullable DyeColor color);

    boolean isDefaultColor(Level level, BlockPos pos, BlockState state);
}
