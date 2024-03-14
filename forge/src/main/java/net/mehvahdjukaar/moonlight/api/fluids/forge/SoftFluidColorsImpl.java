package net.mehvahdjukaar.moonlight.api.fluids.forge;

import net.mehvahdjukaar.moonlight.api.fluids.BuiltInSoftFluids;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.mehvahdjukaar.moonlight.api.util.PotionNBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import org.jetbrains.annotations.Nullable;

public class SoftFluidColorsImpl {

    //grabs world/ fluid stack dependent tint color if fluid has associated forge fluid. overrides normal tint color
    public static int getSpecialColor(SoftFluidStack stack, @Nullable BlockAndTintGetter world, @Nullable BlockPos pos) {
        int specialColor = 0;

        //yay hardcoding
        //at least this works for any fluid
        CompoundTag fluidTag = stack.getTag();
        if (fluidTag != null && fluidTag.contains("color")) {
            specialColor = fluidTag.getInt("color");
        }
        if (stack.is(BuiltInSoftFluids.POTION.get())) {
            specialColor = PotionNBTHelper.getColorFromNBT(fluidTag);
        } else {
            Fluid f = stack.getVanillaFluid();
            if (f != Fluids.EMPTY) {
                var prop = IClientFluidTypeExtensions.of(f);
                if (prop != IClientFluidTypeExtensions.DEFAULT) {
                    //world accessor
                    int w = -1;
                    //stack accessor
                    if (stack instanceof SoftFluidStackImpl ss) {
                        w = prop.getTintColor(ss.toForgeFluid());
                    }
                    if (w != -1) specialColor = w;
                    else if (world != null && pos != null) {
                        w = prop.getTintColor(f.defaultFluidState(), world, pos);
                        if (w != -1) specialColor = w;
                    }
                }
            }
        }
        return specialColor;
    }
}
