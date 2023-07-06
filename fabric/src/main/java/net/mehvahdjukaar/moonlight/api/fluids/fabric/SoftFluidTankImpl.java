package net.mehvahdjukaar.moonlight.api.fluids.fabric;

import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.mehvahdjukaar.moonlight.api.fluids.BuiltInSoftFluids;
import net.mehvahdjukaar.moonlight.api.util.PotionNBTHelper;
import net.mehvahdjukaar.moonlight.core.client.SoftFluidParticleColors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import org.jetbrains.annotations.Nullable;

/**
 * instance this fluid tank in your tile entity
 */
@SuppressWarnings("unused")
public class SoftFluidTankImpl extends SoftFluidTank {

    public static SoftFluidTank create(int capacity) {
        return new SoftFluidTankImpl(capacity);
    }

    protected SoftFluidTankImpl(int capacity) {
        super(capacity);
    }

    /**
     * @return tint color to be applied on the fluid texture
     */
    //client only
    public int getTintColor(@Nullable BlockAndTintGetter world, @Nullable BlockPos pos) {
        SoftFluid.TintMethod method = this.fluid.getTintMethod();
        if (method == SoftFluid.TintMethod.NO_TINT) return -1;
        if (this.needsColorRefresh) {
            this.refreshSpecialColor(world, pos);
            this.needsColorRefresh = false;
        }
        if (this.specialColor != 0) return this.specialColor;
        return this.fluid.getTintColor();
    }

    /**
     * @return tint color to be applied on the fluid texture
     */
    public int getFlowingTint(@Nullable BlockAndTintGetter world, @Nullable BlockPos pos) {
        SoftFluid.TintMethod method = this.fluid.getTintMethod();
        if (method == SoftFluid.TintMethod.FLOWING) return this.getParticleColor(world, pos);
        else return this.getTintColor(world, pos);
    }

    /**
     * @return tint color to be used on particle. Differs from getTintColor since it returns an mixWith color extrapolated from their fluid textures
     */
    public int getParticleColor(@Nullable BlockAndTintGetter world, @Nullable BlockPos pos) {
        if (this.isEmpty()) return -1;
        int tintColor = this.getTintColor(world, pos);
        //if tint color is white gets averaged color
        if (tintColor == -1) return SoftFluidParticleColors.getParticleColor(this.fluid);
        return tintColor;
    }

    //grabs world/ fluid stack dependent tint color if fluid has associated forge fluid. overrides normal tint color
    private void refreshSpecialColor(@Nullable BlockAndTintGetter world, @Nullable BlockPos pos) {

        if (fluid == BuiltInSoftFluids.POTION.get()) {
            this.specialColor = PotionNBTHelper.getColorFromNBT(this.nbt);
        } else {
            Fluid f = this.fluid.getForgeFluid();
            if (f != Fluids.EMPTY) {
            }
        }
    }

}
