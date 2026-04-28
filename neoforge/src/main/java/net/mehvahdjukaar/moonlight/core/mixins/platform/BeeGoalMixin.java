package net.mehvahdjukaar.moonlight.core.mixins.platform;

import com.llamalad7.mixinextras.sugar.Local;
import net.mehvahdjukaar.moonlight.api.block.IBeeGrowable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

//fixes, hopefully, double plant growth. should work with other mods too
@Mixin(targets = {"net.minecraft.world.entity.animal.Bee$BeeGrowCropGoal"})
public abstract class BeeGoalMixin {


    @Shadow
    @Final
    Bee this$0;

    @Inject(method = "tick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;levelEvent(ILnet/minecraft/core/BlockPos;I)V",
                    shift = At.Shift.BY, by = -2), cancellable = true)
    public void moonlight$tickBeeGrowable(CallbackInfo ci, @Local BlockPos blockPos, @Local(ordinal = 1) BlockState blockState2) {
        if (blockState2.getBlock() instanceof IBeeGrowable beeGrowable) {
            beeGrowable.getPollinated(this.this$0.level(), blockPos, blockState2);
            this.this$0.level().levelEvent(2005, blockPos, 0);
            this.this$0.incrementNumCropsGrownSincePollination();
            ci.cancel();
        } //TODO: check
    }
}
