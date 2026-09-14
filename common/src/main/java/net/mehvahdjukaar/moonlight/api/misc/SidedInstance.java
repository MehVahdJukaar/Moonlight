package net.mehvahdjukaar.moonlight.api.misc;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ChatType;
import org.jetbrains.annotations.ApiStatus;

import java.util.concurrent.ExecutionException;
import java.util.function.Function;

// so hear me out, datapack registry entries are one per logical side
// this means we cant serialize them properly if we just keep 1 instance as we might want to serialize them both ways
// so we need to keep one instance per logical side
// how to do that tho? we need a way we can then retrieve with a RegistryAccess or Level
// Weak HashMap using HolderLookup.Provider as key? nope those can be subclasses and are very often, leading to more undeded instances
// so we use a dummy object from one of the registries datapack registires...
// IMPORTANT: weak keys are NOT enough to make this reload safe on their own: almost every instance we store keeps the
// provider it was built from, and that provider owns the registry that owns our dummy key, so the value
// resurrects its own key and the entry can never be collected. That's why clearAll() exists and is called
// on server stop and client disconnect.
public class SidedInstance<T> {

    private static final WeakHashSet<SidedInstance<?>> ALL = new WeakHashSet<>();

    //hack so we can have essentially an identity map
    private final Cache<ChatType, T> instances = CacheBuilder.newBuilder()
            .weakKeys()
            .build();

    private final Function<HolderLookup.Provider, T> factory;

    private SidedInstance(Function<HolderLookup.Provider, T> factory) {
        this.factory = factory;
    }

    public static <T> SidedInstance<T> of(Function<HolderLookup.Provider, T> factory) {
        SidedInstance<T> instance = new SidedInstance<>(factory);
        ALL.add(instance);
        return instance;
    }

    @ApiStatus.Internal
    public static void clearAll() {
        for (var i : ALL) i.instances.invalidateAll();
    }

    @ApiStatus.Internal
    public static void clearAll(HolderLookup.Provider ra) {
        ChatType key;
        try {
            key = getDummyKey(ra);
        } catch (Exception e) {
            return;
        }
        for (var i : ALL) i.instances.invalidate(key);
    }

    public T get(HolderLookup.Provider ra) {
        try {
            return instances.get(getDummyKey(ra),
                    () -> this.factory.apply(ra));
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public void invalidate(HolderLookup.Provider ra) {
        instances.invalidate(getDummyKey(ra));
    }

    public void set(HolderLookup.Provider ra, T instance) {
        instances.put(getDummyKey(ra), instance);
    }

    private static ChatType getDummyKey(HolderLookup.Provider ra) {
        try {
            return ra.lookupOrThrow(Registries.CHAT_TYPE)
                    .getOrThrow(ChatType.CHAT).value();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to find CHAT_TYPE registry! This is a VANILLA datapack registry! How is this possible??");
        }
    }
}
