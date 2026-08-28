package net.mehvahdjukaar.moonlight.api.fluids;

import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.core.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class SoftFluidRegistry {

    public static final ResourceKey<Registry<SoftFluid>> KEY = ResourceKey.createRegistryKey(Moonlight.res("soft_fluid"));


    public static Holder<SoftFluid> getEmpty(HolderLookup.Provider pr) {
        return MLBuiltinSoftFluids.EMPTY.getHolder(pr);
    }

    public static Holder<SoftFluid> getEmpty(HolderGetter<SoftFluid> reg) {
        return MLBuiltinSoftFluids.EMPTY.lookup(reg);
    }

    public static Registry<SoftFluid> get(RegistryAccess registryAccess) {
        return registryAccess.lookupOrThrow(KEY);
    }

    public static HolderLookup.RegistryLookup<SoftFluid> get(HolderLookup.Provider provider) {
        return provider.lookupOrThrow(KEY);
    }

    public static Registry<SoftFluid> get(Level level) {
        return get(level.registryAccess());
    }

}

