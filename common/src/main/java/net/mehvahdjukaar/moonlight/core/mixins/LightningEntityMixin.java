package net.mehvahdjukaar.moonlight.core.mixins;

import net.mehvahdjukaar.moonlight.api.events.ILightningStruckBlockEvent;
import net.mehvahdjukaar.moonlight.api.events.MoonlightEventsHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightningBolt.class)
public abstract class LightningEntityMixin extends Entity {

    @Shadow
    protected abstract BlockPos getStrikePosition();

    protected LightningEntityMixin(EntityType<?> type, Level world) {
        super(type, world);
    }


    @Inject(method = "powerLightningRod", at = @At("HEAD"))
    private void powerLightningRod(CallbackInfo ci) {
        BlockPos blockPos = this.getStrikePosition();
        BlockState blockState = this.level().getBlockState(blockPos);
        var event = ILightningStruckBlockEvent.create(blockState, level(), blockPos, (LightningBolt) (Object) this);
        MoonlightEventsHelper.postEvent(event, ILightningStruckBlockEvent.class);
    }
}
