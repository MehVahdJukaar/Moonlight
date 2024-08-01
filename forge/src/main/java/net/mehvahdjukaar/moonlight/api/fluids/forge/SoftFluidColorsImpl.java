package net.mehvahdjukaar.moonlight.api.fluids.forge;

import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import org.jetbrains.annotations.Nullable;

public class SoftFluidColorsImpl {

    //grabs world/ fluid stack dependent tint color if fluid has associated forge fluid. overrides normal tint color
    public static int getSpecialColor(SoftFluidStack stack, @Nullable BlockAndTintGetter world, @Nullable BlockPos pos) {
        int specialColor = 0;

        //yay hardcoding
        //at least this works for any fluid
        PotionContents potionContents = stack.get(DataComponents.POTION_CONTENTS);
        if (potionContents != null && potionContents.customColor().isPresent()) {
            specialColor = potionContents.customColor().get();
        } else {
            Holder<Fluid> f = stack.getVanillaFluid();
            if (f != Fluids.EMPTY) {
                var prop = IClientFluidTypeExtensions.of(f.value());
                if (prop != IClientFluidTypeExtensions.DEFAULT) {
                    //world accessor
                    int w = -1;
                    //stack accessor
                    if (stack instanceof SoftFluidStackImpl ss) {
                        w = prop.getTintColor(ss.toForgeFluid());
                    }
                    if (w != -1) specialColor = w;
                    else if (world != null && pos != null) {
                        w = prop.getTintColor(f.value().defaultFluidState(), world, pos);
                        if (w != -1) specialColor = w;
                    }
                }
            }
        }
        return specialColor;
    }
}
