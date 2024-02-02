package net.mehvahdjukaar.moonlight.core.mixins.forge;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.mehvahdjukaar.moonlight.api.events.IFireConsumeBlockEvent;
import net.mehvahdjukaar.moonlight.api.events.MoonlightEventsHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FireBlock.class)
public abstract class FireBlockMixin extends BaseFireBlock {

    protected FireBlockMixin(Properties settings, float damage) {
        super(settings, damage);
    }

    @Inject(method = "checkBurnOut",
            at = @At(value = "INVOKE",
                    target = "net/minecraft/world/level/Level.removeBlock (Lnet/minecraft/core/BlockPos;Z)Z",
                    shift = At.Shift.AFTER))
    private void afterRemoveBlock(Level level, BlockPos pos, int chance, RandomSource pRandom, int age,
                                  Direction face, CallbackInfo ci, @Local BlockState before) {
        var event = IFireConsumeBlockEvent.create(pos, level, before, chance, age, face);
        MoonlightEventsHelper.postEvent(event, IFireConsumeBlockEvent.class);
        BlockState newState = event.getFinalState();
        if (newState != null) level.setBlockAndUpdate(pos, newState);
    }
}