package net.mehvahdjukaar.moonlight.api.map.type;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.mehvahdjukaar.moonlight.api.map.MapDataRegistry;
import net.mehvahdjukaar.moonlight.api.map.markers.MapBlockMarker;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

//type itself can have two types: json defined or custom code defined. Do not subclass
//these are what is in json. Each json = a new instance of these Types
public interface MlMapDecorationType<D extends MLMapDecoration, M extends MapBlockMarker<D>> {

    //pain
    Codec<MlMapDecorationType<?, ?>> DIRECT_CODEC =
            Codec.either(MLSpecialMapDecorationType.CODEC, MLJsonMapDecorationType.CODEC).xmap(
                    either -> either.map(s -> s, c -> c),
                    type -> {
                        if (type == null) {
                            Moonlight.LOGGER.error("map decoration type cant be null. how did this happen?");
                        }
                        if (type instanceof MLSpecialMapDecorationType<?, ?> c) {
                            return Either.left(c);
                        }
                        return Either.right((MLJsonMapDecorationType) type);
                    });

    Codec<MlMapDecorationType<?, ?>> DIRECT_NETWORK_CODEC =
            Codec.either(MLSpecialMapDecorationType.CODEC, MLJsonMapDecorationType.NETWORK_CODEC).xmap(
                    either -> either.map(s -> s, c -> c),
                    type -> {
                        if (type == null) {
                            Moonlight.LOGGER.error("map decoration type cant be null. how did this happen?");
                        }
                        if (type instanceof MLSpecialMapDecorationType<?, ?> c) {
                            return Either.left(c);
                        }
                        return Either.right((MLJsonMapDecorationType) type);
                    });


    // registry reference codec
    Codec<Holder<MlMapDecorationType<?, ?>>> CODEC = RegistryFileCodec.create(MapDataRegistry.REGISTRY_KEY, DIRECT_CODEC);
    // registry reference network codec
    StreamCodec<RegistryFriendlyByteBuf, MlMapDecorationType<?, ?>> STREAM_CODEC = ByteBufCodecs.registry(MapDataRegistry.REGISTRY_KEY);

    /**
     * If this marker should be saved to disk
     */
    @ApiStatus.Internal
    boolean isFromWorld();

    default ResourceLocation getCustomFactoryID() {
        return ResourceLocation.parse("");
    }

    M createEmptyMarker();

    @Nullable
    M load(CompoundTag compound, HolderLookup.Provider registries);

    @Nullable
    M getWorldMarkerFromWorld(BlockGetter reader, BlockPos pos);

    default int getDefaultMapColor() {
        return 1;
    }

    default Optional<HolderSet<Structure>> getAssociatedStructure() {
        return Optional.empty();
    }

    StreamCodec<? super RegistryFriendlyByteBuf,? extends MLMapDecoration> getDecorationCodec();
}
