package net.mehvahdjukaar.moonlight.core.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.mehvahdjukaar.moonlight.api.block.IOptionalEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockStateBaseMixin {

    @Shadow
    public abstract Block getBlock();

    @ModifyReturnValue(method = "hasBlockEntity", at = @At("RETURN"))
    private boolean vista$forceBlockEntity(boolean original) {
        if (original && this.getBlock() instanceof IOptionalEntityBlock opt) {
            return opt.shouldHaveBlockEntity((BlockBehaviour.BlockStateBase) (Object) this);
        }
        return original;
    }
}
