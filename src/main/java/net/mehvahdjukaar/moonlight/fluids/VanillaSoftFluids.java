package net.mehvahdjukaar.moonlight.fluids;

import net.mehvahdjukaar.moonlight.Moonlight;
import net.mehvahdjukaar.moonlight.misc.ObjectReference;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.RegistryObject;

public class VanillaSoftFluids {

    public static final ObjectReference<SoftFluid> EMPTY = create("empty");
    public static final ObjectReference<SoftFluid> WATER = create("water");
    public static final ObjectReference<SoftFluid> LAVA = create("lava");
    public static final ObjectReference<SoftFluid> HONEY = create("honey");
    public static final ObjectReference<SoftFluid> MILK = create("milk");
    public static final ObjectReference<SoftFluid> MUSHROOM_STEW = create("mushroom_stew");
    public static final ObjectReference<SoftFluid> BEETROOT_SOUP = create("beetroot_stew");
    public static final ObjectReference<SoftFluid> RABBIT_STEW = create("rabbit_stew");
    public static final ObjectReference<SoftFluid> SUS_STEW = create("suspicious_stew");
    public static final ObjectReference<SoftFluid> POTION = create("potion");
    public static final ObjectReference<SoftFluid> DRAGON_BREATH = create("dragon_breath");
    public static final ObjectReference<SoftFluid> XP = create("experience");
    public static final ObjectReference<SoftFluid> SLIME = create("slime");
    public static final ObjectReference<SoftFluid> GHAST_TEAR = create("ghast_tear");
    public static final ObjectReference<SoftFluid> MAGMA_CREAM = create("magma_cream");
    public static final ObjectReference<SoftFluid> POWDERED_SNOW = create("powder_snow");

    private static RegistryObject<SoftFluid> get1(String name) {
        return RegistryObject.create(new ResourceLocation(name), SoftFluidRegistry.SOFT_FLUIDS.get());
    }
    private static ObjectReference<SoftFluid> create(String name) {
        return new ObjectReference<>(Moonlight.res(name), SoftFluidRegistry.REGISTRY_KEY);
    }
}
