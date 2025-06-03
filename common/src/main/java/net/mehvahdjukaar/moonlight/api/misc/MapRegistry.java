package net.mehvahdjukaar.moonlight.api.misc;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import net.minecraft.core.IdMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.VarInt;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

public class MapRegistry<T> implements IdMap<T>, Codec<T> {
    private final StreamCodec<FriendlyByteBuf, T> streamCodec;

    private final String name;

    private final BiMap<ResourceLocation, T> map = HashBiMap.create();
    private final Reference2IntMap<T> tToId;
    private final List<T> idToT;

    public MapRegistry(String name) {
        this.name = name;
        this.idToT = Lists.newArrayListWithExpectedSize(32);
        this.tToId = new Reference2IntOpenHashMap<>(32);
        this.tToId.defaultReturnValue(-1);
        this.streamCodec = new StreamC();

    }

    public static <B> CodecMapRegistry<B> ofCodec(String name) {
        return new CodecMapRegistry<>(name);
    }

    public static <B> CodecMapRegistry<B> ofCodec() {
        return new CodecMapRegistry<>("unnamed codec registry");
    }

    public <B extends T> T register(ResourceLocation name, B value) {
        if (map.containsKey(name)) {
            throw new IllegalStateException("Cannot register duplicate value " + name);
        }
        this.map.put(name, value);
        this.recomputeIdMappings();
        return value;
    }

    public <B extends T> T register(String name, B value) {
        this.register(ResourceLocation.parse(name), value);
        return value;
    }

    protected void recomputeIdMappings() {
        this.tToId.clear();
        var orderedKeys = this.map.keySet().stream().sorted().toList();
        int id = 0;
        for (var k : orderedKeys) {
            T value = this.map.get(k);
            if (value == null) continue; //skip nulls
            this.tToId.put(value, id);
            id++;
        }
    }

    @Nullable
    public T getValue(ResourceLocation name) {
        return this.map.get(name);
    }

    @Nullable
    public T getValue(String name) {
        return this.getValue(ResourceLocation.parse(name));
    }

    @Nullable
    public ResourceLocation getKey(T value) {
        return this.map.inverse().get(value);
    }

    public Set<ResourceLocation> keySet() {
        return this.map.keySet();
    }

    public Set<T> getValues() {
        return this.map.values();
    }

    public T getValueOrDefault(ResourceLocation parse, T defaultType) {
        return this.map.getOrDefault(parse, defaultType);
    }

    public Set<Map.Entry<ResourceLocation, T>> getEntries() {
        return this.map.entrySet();
    }

    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    public int size() {
        return this.map.size();
    }

    public boolean containsKey(ResourceLocation name) {
        return this.map.containsKey(name);
    }

    public <U> DataResult<Pair<T, U>> decode(DynamicOps<U> ops, U json) {
        return ResourceLocation.CODEC.decode(ops, json).flatMap(pair -> {
            ResourceLocation id = pair.getFirst();
            T value = this.getValue(id);
            return value == null ? DataResult.error(() -> "Could not find any entry with key '" + id + "' in registry [" + name + "] \n Known keys: " + this.keySet()) :
                    DataResult.success(Pair.of(value, pair.getSecond()));
        });
    }

    public <U> DataResult<U> encode(T object, DynamicOps<U> ops, U prefix) {
        ResourceLocation id = this.getKey(object);
        return id == null ? DataResult.error(() -> "Could not find element " + object + " in registry" + name) :
                ops.mergeToPrimitive(prefix, ops.createString(id.toString()));
    }

    public void clear() {
        this.map.clear();
    }

    public <E> Codec<E> dispatch(Function<? super E, ? extends T> type) {
        return Codec.super.dispatch(type, c -> (MapCodec<? extends E>) c);
    }

    public int getId(T value) {
        return this.tToId.getInt(value);
    }

    @Nullable
    public final T byId(int id) {
        return id >= 0 && id < this.idToT.size() ? this.idToT.get(id) : null;
    }

    public Iterator<T> iterator() {
        return Iterators.filter(this.idToT.iterator(), Objects::nonNull);
    }

    public boolean contains(int id) {
        return this.byId(id) != null;
    }

    public StreamCodec<FriendlyByteBuf, T> getStreamCodec() {
        return this.streamCodec;
    }


    private class StreamC implements StreamCodec<FriendlyByteBuf, T> {
        @Override
        public T decode(FriendlyByteBuf buffer) {
            int i = VarInt.read(buffer);
            var obj = byId(i);
            if (obj == null) {
                throw new IllegalStateException("Unknown id " + i + " for registry " + name);
            }
            return obj;
        }

        @Override
        public void encode(FriendlyByteBuf buffer, T value) {
            int i = getId(value);
            VarInt.write(buffer, i);
        }
    }
}
