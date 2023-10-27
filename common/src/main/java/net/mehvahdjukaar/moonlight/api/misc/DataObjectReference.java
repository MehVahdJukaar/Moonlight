package net.mehvahdjukaar.moonlight.api.misc;

import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//can be statically stored and persists across world loads

/**
 * A soft reference to an object in a Data pack registry
 */
public class DataObjectReference<T> {

    private final ResourceLocation location;
    private final ResourceKey<Registry<T>> registryKey;

    public DataObjectReference(ResourceLocation location, ResourceKey<Registry<T>> registry) {
        this.location = location;
        this.registryKey = registry;
    }

    public Holder<T> getHolder() {
        var r = Utils.hackyGetRegistryAccess();
        return r.registryOrThrow(registryKey).getHolderOrThrow(ResourceKey.create(registryKey, location));
    }

    @NotNull
    public T get() {
        var r = Utils.hackyGetRegistryAccess();
        var reg = r.registryOrThrow(registryKey);
        var v = reg.get(this.location);
        if (v == null) {
            throw new RuntimeException("Data object at location " + location + " could not be found. How? " +
                    "Registry content:" + reg.keySet());
        }
        return v;
    }

    @Nullable
    public T getUnchecked() {
        var r = Utils.hackyGetRegistryAccess();
        return r.registryOrThrow(registryKey).get(location);
    }

    public ResourceLocation getID() {
        return location;
    }
}
