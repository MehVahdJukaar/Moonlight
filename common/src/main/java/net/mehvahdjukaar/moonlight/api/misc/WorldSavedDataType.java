package net.mehvahdjukaar.moonlight.api.misc;


import com.mojang.serialization.Codec;
import net.mehvahdjukaar.moonlight.api.MoonlightRegistry;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

//Basically a helper that manages syncing on world join and stores codecs cleanly
//per world, automatically synced when stream codec is not null. not per game. saved onto overworld level
public final class WorldSavedDataType<D extends WorldSavedData> {

    public static final Codec<WorldSavedDataType<? extends WorldSavedData>> CODEC =
            MoonlightRegistry.WORLD_SAVED_DATA_TYPE_REGISTRY.byNameCodec();
    public static final StreamCodec<RegistryFriendlyByteBuf, WorldSavedDataType<? extends WorldSavedData>> STREAM_CODEC =
            ByteBufCodecs.registry(MoonlightRegistry.WORLD_SAVED_DATA_TYPE_REGISTRY.key());


    private final Codec<D> codec;
    @Nullable
    private final StreamCodec<RegistryFriendlyByteBuf, D> streamCodec;
    private final SavedData.Factory<D> factory;
    private final String name;


    private D clientInstance = null;

    public WorldSavedDataType(ResourceLocation id, Function<ServerLevel, D> constructor, Codec<D> codec, @Nullable StreamCodec<RegistryFriendlyByteBuf, D> streamCodec) {
        this.codec = codec;
        this.streamCodec = streamCodec;
        this.name = id.toDebugFileName();

        this.factory = new SavedData.Factory<>(() -> constructor.apply(
                PlatHelper.getCurrentServer().overworld()),
                this::load, null);
    }

    public D getData(Level level) {
        if (level.isClientSide && !this.isSyncable()) {
            throw new IllegalStateException("Tried to access unsyncable world saved data on client side!");
        }

        if (level instanceof ServerLevel server) {
            return server.getServer().overworld().getDataStorage()
                    .computeIfAbsent(factory, name);
        } else {
            return clientInstance;
        }
    }

    public void setData(Level level, D data) {
        if (level instanceof ServerLevel server) {
            server.getServer().overworld().getDataStorage().set(name, data);
        } else {
            this.clientInstance = data;
        }
        data.onReassigned(level);
    }

    private D load(CompoundTag tag, HolderLookup.Provider provider) {
        var t = tag.get(this.getName());
        var ops = provider.createSerializationContext(NbtOps.INSTANCE);
        var dataResult = codec.decode(ops, t);
        return dataResult.getOrThrow().getFirst();
    }

    public Codec<D> getCodec() {
        return codec;
    }

    @Nullable
    public StreamCodec<RegistryFriendlyByteBuf, D> getStreamCodec() {
        return streamCodec;
    }

    public boolean isSyncable() {
        return streamCodec != null;
    }

    public String getName() {
        return name;
    }
}
