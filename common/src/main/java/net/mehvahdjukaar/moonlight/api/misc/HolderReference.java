package net.mehvahdjukaar.moonlight.api.misc;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

@Deprecated(forRemoval = true)
public class HolderReference<A> extends HolderRef<A>{
    protected HolderReference(ResourceKey<Registry<A>> registryKey, ResourceKey<A> key) {
        super(registryKey, key);
    }

    public static <A> HolderReference<A> of(String id, ResourceKey<Registry<A>> registry) {
        return of(ResourceLocation.tryParse(id), registry);
    }

    public static <A> HolderReference<A> of(ResourceLocation location, ResourceKey<Registry<A>> registry) {
        return new HolderReference<>(registry, ResourceKey.create(registry, location));
    }

    public static <A> HolderReference<A> of(ResourceKey<A> key) {
        return new HolderReference<>(ResourceKey.createRegistryKey(key.registry()), key);
    }

    public static <A> HolderRef.Opt<A> optional(ResourceLocation location, ResourceKey<Registry<A>> registry) {
        return new HolderRef.Opt<>(registry, ResourceKey.create(registry, location));
    }

    public static <A> HolderRef.Opt<A> optional(ResourceKey<A> key) {
        return new HolderRef.Opt<>(ResourceKey.createRegistryKey(key.registry()), key);
    }
}
