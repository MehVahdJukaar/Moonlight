package net.mehvahdjukaar.moonlight.api.misc;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

//can be statically stored and persists across world loads

/**
 * A soft reference to an object in a Data pack registry
 * Like registry object but can be invalidated and works for data pack registries
 */
@Deprecated()
public class DataObjectReference<T> extends DynamicHolder<T> {

    public DataObjectReference(String id, ResourceKey<Registry<T>> registry) {
        this(new ResourceLocation(id), registry);
    }

    public DataObjectReference(ResourceLocation location, ResourceKey<Registry<T>> registry) {
        super(registry, ResourceKey.create(registry, location));
    }

    public DataObjectReference(ResourceKey<T> key) {
        super(ResourceKey.createRegistryKey(key.registry()), key);
    }

    public Holder<T> getHolder() {
        return getInstance();
    }

    @Deprecated(forRemoval = true)
    @Nullable
    public T getUnchecked() {
        return get();
    }
}
