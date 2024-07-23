package net.mehvahdjukaar.moonlight.api.misc;

import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Supplier;

//can be statically stored and persists across world loads

/**
 * A soft reference to an object in a Data pack registry
 * Like registry object but can be invalidated and works for data pack registries
 */
public class DataObjectReference<T> implements Supplier<T> {

    private static final WeakHashSet<DataObjectReference<?>> REFERENCES = new WeakHashSet<>();

    private final ResourceKey<Registry<T>> registryKey;
    private final ResourceKey<T> key;

    @Nullable
    private Holder<T> cache;

    public DataObjectReference(String id, ResourceKey<Registry<T>> registry) {
        this(ResourceLocation.parse(id), registry);
    }

    public DataObjectReference(ResourceLocation location, ResourceKey<Registry<T>> registry) {
        this.registryKey = registry;
        this.key = ResourceKey.create(registryKey, location);
        REFERENCES.add(this);
    }

    public DataObjectReference(ResourceKey<T> key) {
        this.key = key;
        this.registryKey = ResourceKey.createRegistryKey(key.registry());
        REFERENCES.add(this);
    }

    public Holder<T> getHolder() {
        if (cache == null) {
            var r = Utils.hackyGetRegistryAccess();
            Registry<T> reg = r.registryOrThrow(registryKey);
            try {
                cache = reg.getHolderOrThrow(key);
            } catch (Exception e) {
                throw new RuntimeException("Failed to get object from registry: " + key +
                        ".\nCalled from " + Thread.currentThread() + ".\n"+
                        "Registry content was: " + reg.entrySet().stream().map(Map.Entry::getValue).toList(), e);
            }
        }
        return cache;
    }

    @NotNull
    public T get() {
        return getHolder().value();
    }

    public void clearCache() {
        cache = null;
    }

    public ResourceLocation getID() {
        return key.location();
    }

    @ApiStatus.Internal
    public static void onDataReload() {
        REFERENCES.forEach(DataObjectReference::clearCache);
    }
}
