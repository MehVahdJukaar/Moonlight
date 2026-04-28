package net.mehvahdjukaar.moonlight.core.mixins;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LevelChunk.class)
public abstract class LevelChunkMixin {

    //TODO: remove from IW
    //fixing a vanilla bug that causes log spam when a block that can provide a tile doesn't actually provide it (they can do this now)
    @WrapWithCondition(method = "promotePendingBlockEntity",
            require = 0,
            at = @At(value = "INVOKE",
                    target = "org/slf4j/Logger.warn(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V",
                    ordinal = 1))
    private boolean moonlight$stopDumbWarning(Logger instance, String s, Object b, Object p) {
        if (b instanceof BlockState state && p instanceof BlockPos pos) {
            if (state.getBlock() instanceof EntityBlock block) {
                return block.newBlockEntity(pos, state) != null;
            }
        }
        return true;
    }


}