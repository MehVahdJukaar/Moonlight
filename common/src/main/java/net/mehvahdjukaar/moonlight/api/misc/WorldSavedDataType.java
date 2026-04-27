package net.mehvahdjukaar.moonlight.api.misc;


import com.mojang.serialization.Codec;
import net.mehvahdjukaar.moonlight.api.MoonlightRegistry;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.core.set.BlocksColorInternal;
import net.minecraft.client.Camera;
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
import java.util.function.Supplier;

//Basically a helper that manages syncing on world join and stores codecs cleanly
//per world, automatically synced when stream codec is not null. not per game. saved onto overworld level
public final class WorldSavedDataType<D extends WorldSavedData> {
    public static final StreamCodec<RegistryFriendlyByteBuf, WorldSavedDataType<? extends WorldSavedData>> STREAM_CODEC =
            ByteBufCodecs.registry(MoonlightRegistry.WORLD_SAVED_DATA_TYPE_REGISTRY.key());
    public static final Codec<WorldSavedDataType<? extends WorldSavedData>> CODEC =
            MoonlightRegistry.WORLD_SAVED_DATA_TYPE_REGISTRY.byNameCodec();

    //TODO: 1.22 make map codec here instead

    private final Supplier<Codec<D>> codec;
    @Nullable
    private final Supplier<StreamCodec<? super RegistryFriendlyByteBuf, D>> streamCodec;
    private final SavedData.Factory<D> factory;
    private final String name;
    private final Scope scope;


    private D clientInstance = null;

    public enum Scope {
        SINGLE_OVERWORLD,
        PER_LEVEL;

        private ServerLevel getTargetLevel(ServerLevel sl) {
            ServerLevel targetLevel;
            if (this == Scope.PER_LEVEL) {
                targetLevel = sl;
            } else {
                targetLevel = sl.getServer().overworld();
            }
            return targetLevel;
        }
    }

    public WorldSavedDataType(ResourceLocation id, Function<ServerLevel, D> overworldToDataConstructor,
                              Supplier<Codec<D>> codec,
                              @Nullable Supplier<StreamCodec<? super RegistryFriendlyByteBuf, D>> streamCodec) {
        this(id, overworldToDataConstructor, codec, streamCodec, Scope.SINGLE_OVERWORLD);
    }

    public WorldSavedDataType(ResourceLocation id, Function<ServerLevel, D> overworldToDataConstructor,
                              Supplier<Codec<D>> codec,
                              @Nullable Supplier<StreamCodec<? super RegistryFriendlyByteBuf, D>> streamCodec, Scope scope) {
        this.codec = codec;
        this.streamCodec = streamCodec;
        this.name = id.toDebugFileName();
        this.scope = scope;

        this.factory = new SavedData.Factory<>(() -> overworldToDataConstructor.apply(
                PlatHelper.getCurrentServer().overworld()),
                this::load, null);
    }

    //only null when called on client too early
    @Nullable
    public D getData(Level level) {
        if (level.isClientSide && !this.isSyncable()) {
            throw new IllegalStateException("Tried to access unsyncable world saved data on client side!");
        }

        if (level instanceof ServerLevel server) {
            ServerLevel targetLevel = scope.getTargetLevel(server);
            return targetLevel.getDataStorage().computeIfAbsent(factory, name);
        } else {
            return clientInstance;
        }
    }


    public void setData(Level level, D data) {
        if (level instanceof ServerLevel server) {
            ServerLevel targetLevel = scope.getTargetLevel(server);
            targetLevel.getDataStorage().set(name, data);
        } else {
            this.clientInstance = data;
        }
        data.onReassigned(level);
    }

    private D load(CompoundTag tag, HolderLookup.Provider provider) {
        var t = tag.get(this.getName());
        var ops = provider.createSerializationContext(NbtOps.INSTANCE);
        var dataResult = codec.get().decode(ops, t);
        return dataResult.getOrThrow().getFirst();
    }

    //TODO: make map codec
    public Codec<D> getCodec() {
        return codec.get();
    }

    @Nullable
    public StreamCodec<? super RegistryFriendlyByteBuf, D> getStreamCodec() {
        return streamCodec == null ? null : streamCodec.get();
    }

    public boolean isSyncable() {
        return streamCodec != null;
    }

    public String getName() {
        return name;
    }
}
