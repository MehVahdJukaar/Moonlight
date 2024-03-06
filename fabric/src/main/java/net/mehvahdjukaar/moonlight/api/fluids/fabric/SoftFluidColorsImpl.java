package net.mehvahdjukaar.moonlight.api.fluids.fabric;

import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.mehvahdjukaar.moonlight.api.fluids.BuiltInSoftFluids;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.mehvahdjukaar.moonlight.api.util.PotionNBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

public class SoftFluidColorsImpl {

    //grabs world/ fluid stack dependent tint color if fluid has associated forge fluid. overrides normal tint color
    private static int getSpecialColor(SoftFluidStack stack, @Nullable BlockAndTintGetter world, @Nullable BlockPos pos) {
        int specialColor = 0;
        //yay hardcoding
        //at least this works for any fluid
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("color")) {
            specialColor = tag.getInt("color");
        }
        if (stack.is(BuiltInSoftFluids.POTION.get())) {
            specialColor = PotionNBTHelper.getColorFromNBT(tag);
        } else {
            Fluid f = stack.getVanillaFluid();
            if (f != Fluids.EMPTY) {
                var opt = FluidRenderHandlerRegistry.INSTANCE.get(f);
                return opt.getFluidColor(world, pos, f.defaultFluidState());
            }
        }
        return specialColor;
    }
}
