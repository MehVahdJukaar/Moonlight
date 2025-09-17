package net.mehvahdjukaar.moonlight.api.fluids;

import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.core.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

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
        return registryAccess.registryOrThrow(KEY);
    }

    public static HolderLookup.RegistryLookup<SoftFluid> get(HolderLookup.Provider provider) {
        return provider.lookupOrThrow(KEY);
    }

    public static Registry<SoftFluid> get(Level level) {
        return get(level.registryAccess());
    }



    @Deprecated(forRemoval = true)
    public static Registry<SoftFluid> getRegistry(RegistryAccess registryAccess) {
        return registryAccess.registryOrThrow(KEY);
    }

    @Deprecated(forRemoval = true)
    public static Holder<SoftFluid> getEmpty() {
        return MLBuiltinSoftFluids.EMPTY.getHolderUnsafe();
    }

    @Deprecated(forRemoval = true)
    public static Holder<SoftFluid> hackyGetEmpty() {
        return MLBuiltinSoftFluids.EMPTY.getHolderUnsafe();
    }

    @Deprecated(forRemoval = true)
    public static SoftFluid empty() {
        return MLBuiltinSoftFluids.EMPTY.getUnsafe();
    }

    @Deprecated(forRemoval = true)
    public static Registry<SoftFluid> hackyGetRegistry() {
        return Utils.hackyGetRegistry(KEY);
    }

    @Deprecated(forRemoval = true)
    public static Collection<SoftFluid> getValues() {
        return hackyGetRegistry().stream().toList();
    }

    @Deprecated(forRemoval = true)
    public static Collection<Holder.Reference<SoftFluid>> getHolders() {
        return hackyGetRegistry().holders().toList();
    }

    @Deprecated(forRemoval = true)
    public static Set<Map.Entry<ResourceKey<SoftFluid>, SoftFluid>> getEntries() {
        return hackyGetRegistry().entrySet();
    }

    @Deprecated(forRemoval = true)
    public static Holder<SoftFluid> getHolder(ResourceLocation id) {
        var opt = getOptionalHolder(id);
        if (opt.isPresent()) return opt.get();
        return getEmpty();
    }

    @Deprecated(forRemoval = true)
    public static Optional<Holder.Reference<SoftFluid>> getOptionalHolder(ResourceLocation id) {
        return hackyGetRegistry().getHolder(ResourceKey.create(KEY, id));
    }

}

