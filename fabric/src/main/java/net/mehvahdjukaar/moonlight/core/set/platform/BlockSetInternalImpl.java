package net.mehvahdjukaar.moonlight.core.set.platform;

import net.mehvahdjukaar.moonlight.api.misc.Registrator;
import net.mehvahdjukaar.moonlight.api.set.BlockSetAPI;
import net.mehvahdjukaar.moonlight.api.set.BlockType;
import net.mehvahdjukaar.moonlight.core.set.BlockSetInternal;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;

import java.util.*;
import java.util.function.Consumer;

public class BlockSetInternalImpl {

    private static boolean hasFilledBlockSets = false;

    public static boolean hasFilledBlockSets() {
        return hasFilledBlockSets;
    }

    private static final Map<Registry<?>, Map<Class<? extends BlockType>, LateBTRegQueue<?, ?>>> QUEUES_OLD = new HashMap<>();
    private static final Map<Registry<?>, LateRegQueue<?>> QUEUES = new HashMap<>();

    public static <T> void addDynamicRegistration(String modId, Consumer<Registrator<T>> registrationFunction, Registry<T> registry) {
        LateRegQueue<T> r = (LateRegQueue<T>) QUEUES.computeIfAbsent(registry, b -> new LateRegQueue<>(registry));
        r.add(registrationFunction);
    }

    @SuppressWarnings("unchecked")
    @Deprecated
    public static <T extends BlockType, E> void addDynamicRegistration(
            BlockSetAPI.BlockTypeRegistryCallback<E, T> registrationFunction, Class<T> blockType,
            Registry<E> registry) {
        LateBTRegQueue<T, E> r = (LateBTRegQueue<T, E>) QUEUES_OLD.computeIfAbsent(registry, b -> new LinkedHashMap<>())
                .computeIfAbsent(blockType, b -> new LateBTRegQueue<>(blockType, registry));
        r.add(registrationFunction);
    }

    public static void initializeBlockSets() {
        BlockSetInternal.initializeBlockSets();
        //init items immediately as this happens after all registries have fired
        hasFilledBlockSets = true;
    }

    public static void registerDynamicOrdered(List<? extends ResourceKey<? extends Registry<?>>> regs) {
        for (var id : regs) {
            Registry<?> registry = BuiltInRegistries.REGISTRY.getValue((ResourceKey) id);
            var q = QUEUES_OLD.get(registry);
            if (q != null) {
                for (var e : q.entrySet()) {
                    e.getValue().registerEntries();
                }
                QUEUES_OLD.remove(registry);
            }

            var q2 = QUEUES.get(registry);
            if (q2 != null) {
                q2.registerEntries();
                QUEUES.remove(registry);
            }
        }

        for (var qq : QUEUES_OLD.values()) {
            for (var e : qq.entrySet()) {
                e.getValue().registerEntries();
            }
        }
        QUEUES_OLD.clear();

        for (var qq2 : QUEUES.values()) {
            qq2.registerEntries();
        }
        QUEUES.clear();
    }

    @Deprecated
    private static class LateBTRegQueue<T extends BlockType, E> {
        final Class<T> blockType;
        final Queue<BlockSetAPI.BlockTypeRegistryCallback<E, T>> queue = new ArrayDeque<>();
        final Registry<E> registry;

        public LateBTRegQueue(Class<T> blockType, Registry<E> registry) {
            this.blockType = blockType;
            this.registry = registry;
        }

        public void add(BlockSetAPI.BlockTypeRegistryCallback<E, T> callback) {
            queue.add(callback);
        }

        public void registerEntries() {
            queue.forEach(a -> a.accept((n, i) ->
                    Registry.register(registry, n, i), BlockSetAPI.getBlockSet(blockType).getValues()));
        }
    }

    private static class LateRegQueue<E> {
        final Queue<Consumer<Registrator<E>>> queue = new ArrayDeque<>();
        final Registry<E> registry;

        public LateRegQueue(Registry<E> registry) {
            this.registry = registry;
        }

        public void add(Consumer<Registrator<E>> callback) {
            queue.add(callback);
        }

        public void registerEntries() {
            queue.forEach(a -> a.accept((n, i) -> Registry.register(registry, n, i)));
        }
    }

}
