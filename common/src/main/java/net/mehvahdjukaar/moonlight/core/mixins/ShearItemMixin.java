package net.mehvahdjukaar.moonlight.core.mixins;

import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShearsItem.class)
public class ShearItemMixin {

    @Inject(method = "mineBlock", at = @At("HEAD"))
    public void mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miningEntity, CallbackInfoReturnable<Boolean> cir) {
        if (state.is(Moonlight.SHEATABLE_TAG)) cir.setReturnValue(true);
    }

    @Inject(method = "isCorrectToolForDrops", at = @At("HEAD"))
    public void isCorrectToolForDrops(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (state.is(Moonlight.SHEATABLE_TAG)) cir.setReturnValue(true);
    }

    @Inject(method = "getDestroySpeed", at = @At("HEAD"))
    public void getDestroySpeed(ItemStack stack, BlockState state, CallbackInfoReturnable<Float> cir) {
        if (state.is(Moonlight.SHEATABLE_TAG)) cir.setReturnValue(2f);
    }
}
