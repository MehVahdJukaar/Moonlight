package net.mehvahdjukaar.moonlight.api.fluids;

import net.mehvahdjukaar.moonlight.api.misc.HolderRef;
import net.mehvahdjukaar.moonlight.core.Moonlight;

public class MLBuiltinSoftFluids {

    public static final HolderRef<SoftFluid> EMPTY = create("empty");
    public static final HolderRef<SoftFluid> WATER = create("water");
    public static final HolderRef<SoftFluid> LAVA = create("lava");
    public static final HolderRef<SoftFluid> HONEY = create("honey");
    public static final HolderRef<SoftFluid> MILK = create("milk");
    public static final HolderRef<SoftFluid> MUSHROOM_STEW = create("mushroom_stew");
    public static final HolderRef<SoftFluid> BEETROOT_SOUP = create("beetroot_soup");
    public static final HolderRef<SoftFluid> RABBIT_STEW = create("rabbit_stew");
    public static final HolderRef<SoftFluid> SUS_STEW = create("suspicious_stew");
    public static final HolderRef<SoftFluid> POTION = create("potion");
    public static final HolderRef<SoftFluid> DRAGON_BREATH = create("dragon_breath");
    public static final HolderRef<SoftFluid> XP = create("experience");
    public static final HolderRef<SoftFluid> SLIME = create("slime");
    public static final HolderRef<SoftFluid> GHAST_TEAR = create("ghast_tear");
    public static final HolderRef<SoftFluid> MAGMA_CREAM = create("magma_cream");
    public static final HolderRef<SoftFluid> POWDERED_SNOW = create("powder_snow");


    private static HolderRef<SoftFluid> create(String name) {
        return HolderRef.of(Moonlight.res(name), SoftFluidRegistry.KEY);
    }
}
