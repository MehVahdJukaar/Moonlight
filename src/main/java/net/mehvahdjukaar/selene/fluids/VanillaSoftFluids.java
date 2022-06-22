package net.mehvahdjukaar.selene.fluids;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.RegistryObject;

public class VanillaSoftFluids {

    public static final RegistryObject<SoftFluid> EMPTY = get("empty");
    public static final RegistryObject<SoftFluid> WATER = get("water");
    public static final RegistryObject<SoftFluid> LAVA = get("lava");
    public static final RegistryObject<SoftFluid> HONEY = get("honey");
    public static final RegistryObject<SoftFluid> MILK = get("milk");
    public static final RegistryObject<SoftFluid> MUSHROOM_STEW = get("mushroom_stew");
    public static final RegistryObject<SoftFluid> BEETROOT_SOUP = get("beetroot_stew");
    public static final RegistryObject<SoftFluid> RABBIT_STEW = get("rabbit_stew");
    public static final RegistryObject<SoftFluid> SUS_STEW = get("suspicious_stew");
    public static final RegistryObject<SoftFluid> POTION = get("potion");
    public static final RegistryObject<SoftFluid> DRAGON_BREATH = get("dragon_breath");
    public static final RegistryObject<SoftFluid> XP = get("experience");
    public static final RegistryObject<SoftFluid> SLIME = get("slime");
    public static final RegistryObject<SoftFluid> GHAST_TEAR = get("ghast_tear");
    public static final RegistryObject<SoftFluid> MAGMA_CREAM = get("magma_cream");
    public static final RegistryObject<SoftFluid> POWDERED_SNOW = get("powder_snow");

    private static RegistryObject<SoftFluid> get(String name) {
        return RegistryObject.create(new ResourceLocation("water"), SoftFluidRegistry.SOFT_FLUIDS.get());
    }
}
