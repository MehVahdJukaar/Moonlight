package net.mehvahdjukaar.moonlight.api.fluids;

import net.mehvahdjukaar.moonlight.api.misc.HolderReference;
import net.mehvahdjukaar.moonlight.core.Moonlight;

public class MLBuiltinSoftFluids {
//dont change these!
    public static final HolderReference<SoftFluid> EMPTY = create("empty");
    public static final HolderReference<SoftFluid> WATER = create("water");
    public static final HolderReference<SoftFluid> LAVA = create("lava");
    public static final HolderReference<SoftFluid> HONEY = create("honey");
    public static final HolderReference<SoftFluid> MILK = create("milk");
    public static final HolderReference<SoftFluid> MUSHROOM_STEW = create("mushroom_stew");
    public static final HolderReference<SoftFluid> BEETROOT_SOUP = create("beetroot_soup");
    public static final HolderReference<SoftFluid> RABBIT_STEW = create("rabbit_stew");
    public static final HolderReference<SoftFluid> SUS_STEW = create("suspicious_stew");
    public static final HolderReference<SoftFluid> POTION = create("potion");
    public static final HolderReference<SoftFluid> DRAGON_BREATH = create("dragon_breath");
    public static final HolderReference<SoftFluid> XP = create("experience");
    public static final HolderReference<SoftFluid> SLIME = create("slime");
    public static final HolderReference<SoftFluid> GHAST_TEAR = create("ghast_tear");
    public static final HolderReference<SoftFluid> MAGMA_CREAM = create("magma_cream");
    public static final HolderReference<SoftFluid> POWDERED_SNOW = create("powder_snow");


    private static HolderReference<SoftFluid> create(String name) {
        return HolderReference.of(Moonlight.res(name), SoftFluidRegistry.KEY);
    }
}
