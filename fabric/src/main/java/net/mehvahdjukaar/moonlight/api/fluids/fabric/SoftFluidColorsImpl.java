package net.mehvahdjukaar.moonlight.api.fluids.fabric;

import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

public class SoftFluidColorsImpl {

    //grabs world/ fluid stack dependent tint color if fluid has associated forge fluid. overrides normal tint color
    public static int getSpecialColor(SoftFluidStack stack, @Nullable BlockAndTintGetter world, @Nullable BlockPos pos) {
        //yay hardcoding
        DyedItemColor dyeColor = stack.get(DataComponents.DYED_COLOR);
        if (dyeColor != null) {
            return dyeColor.rgb();
        }
        PotionContents potionContents = stack.get(DataComponents.POTION_CONTENTS);
        if (potionContents != null) {
            return potionContents.getColor();
        }
        //at least this works for any fluid
        int specialColor = 0;
        Fluid f = stack.getVanillaFluid().value();
        if (f != Fluids.EMPTY) {
            var opt = FluidRenderHandlerRegistry.INSTANCE.get(f);
            return opt.getFluidColor(world, pos, f.defaultFluidState());
        }
        return specialColor;
    }
}
