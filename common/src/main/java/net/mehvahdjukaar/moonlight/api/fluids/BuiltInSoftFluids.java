package net.mehvahdjukaar.moonlight.api.fluids;

import net.mehvahdjukaar.moonlight.api.misc.DynamicHolder;
import net.mehvahdjukaar.moonlight.core.Moonlight;

public class BuiltInSoftFluids {

    public static final DynamicHolder<SoftFluid> EMPTY = create("empty");
    public static final DynamicHolder<SoftFluid> WATER = create("water");
    public static final DynamicHolder<SoftFluid> LAVA = create("lava");
    public static final DynamicHolder<SoftFluid> HONEY = create("honey");
    public static final DynamicHolder<SoftFluid> MILK = create("milk");
    public static final DynamicHolder<SoftFluid> MUSHROOM_STEW = create("mushroom_stew");
    public static final DynamicHolder<SoftFluid> BEETROOT_SOUP = create("beetroot_soup");
    public static final DynamicHolder<SoftFluid> RABBIT_STEW = create("rabbit_stew");
    public static final DynamicHolder<SoftFluid> SUS_STEW = create("suspicious_stew");
    public static final DynamicHolder<SoftFluid> POTION = create("potion");
    public static final DynamicHolder<SoftFluid> DRAGON_BREATH = create("dragon_breath");
    public static final DynamicHolder<SoftFluid> XP = create("experience");
    public static final DynamicHolder<SoftFluid> SLIME = create("slime");
    public static final DynamicHolder<SoftFluid> GHAST_TEAR = create("ghast_tear");
    public static final DynamicHolder<SoftFluid> MAGMA_CREAM = create("magma_cream");
    public static final DynamicHolder<SoftFluid> POWDERED_SNOW = create("powder_snow");


    private static DynamicHolder<SoftFluid> create(String name) {
        return DynamicHolder.of(Moonlight.res(name), SoftFluidRegistry.KEY);
    }
}
