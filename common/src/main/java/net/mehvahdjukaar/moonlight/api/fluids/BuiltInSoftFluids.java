package net.mehvahdjukaar.moonlight.api.fluids;

import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.api.misc.DataObjectReference;

public class BuiltInSoftFluids {

    public static final DataObjectReference<SoftFluid> EMPTY = create("empty");
    public static final DataObjectReference<SoftFluid> WATER = create("water");
    public static final DataObjectReference<SoftFluid> LAVA = create("lava");
    public static final DataObjectReference<SoftFluid> HONEY = create("honey");
    public static final DataObjectReference<SoftFluid> MILK = create("milk");
    public static final DataObjectReference<SoftFluid> MUSHROOM_STEW = create("mushroom_stew");
    public static final DataObjectReference<SoftFluid> BEETROOT_SOUP = create("beetroot_stew");
    public static final DataObjectReference<SoftFluid> RABBIT_STEW = create("rabbit_stew");
    public static final DataObjectReference<SoftFluid> SUS_STEW = create("suspicious_stew");
    public static final DataObjectReference<SoftFluid> POTION = create("potion");
    public static final DataObjectReference<SoftFluid> DRAGON_BREATH = create("dragon_breath");
    public static final DataObjectReference<SoftFluid> XP = create("experience");
    public static final DataObjectReference<SoftFluid> SLIME = create("slime");
    public static final DataObjectReference<SoftFluid> GHAST_TEAR = create("ghast_tear");
    public static final DataObjectReference<SoftFluid> MAGMA_CREAM = create("magma_cream");
    public static final DataObjectReference<SoftFluid> POWDERED_SNOW = create("powder_snow");


    private static DataObjectReference<SoftFluid> create(String name) {
        return new DataObjectReference<>(Moonlight.res(name), SoftFluidRegistry.KEY);
    }
}
