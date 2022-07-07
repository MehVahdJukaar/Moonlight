package net.mehvahdjukaar.moonlight.api.misc;

import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

//can be statically stored and persists across world loads
/**
 * A soft reference to an object in a Datapack registry
 */
public class ObjectReference<T> {

    private final ResourceLocation location;
    private final ResourceKey<Registry<T>> registryKey;

    public ObjectReference(ResourceLocation location, ResourceKey<Registry<T>> registry) {
        this.location = location;
        this.registryKey = registry;
    }

    public Holder<T> getHolder() {
        var r = Utils.hackyGetRegistryAccess();
        return r.registryOrThrow(registryKey).getHolderOrThrow(ResourceKey.create(registryKey, location));
    }

    public T get() {
        var r = Utils.hackyGetRegistryAccess();
        return r.registryOrThrow(registryKey).get(this.location);
    }

    public ResourceLocation getID() {
        return location;
    }
}
