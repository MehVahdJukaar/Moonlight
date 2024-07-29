package net.mehvahdjukaar.moonlight.api.map.decoration;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.mehvahdjukaar.moonlight.api.map.MapDataRegistry;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
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

//these are what is in json. Each json = a new instance of these Types
public sealed abstract class MLMapDecorationType<D extends MLMapDecoration, M extends MLMapMarker<D>> permits MLJsonMapDecorationType, MLSpecialMapDecorationType {

    //pain
    public static final Codec<MLMapDecorationType<?, ?>> DIRECT_CODEC =
            Codec.lazyInitialized(() -> Codec.either(MLSpecialMapDecorationType.CODEC, MLJsonMapDecorationType.CODEC).xmap(
                    either -> either.map(s -> s, c -> c),
                    type -> {
                        if (type == null) {
                            Moonlight.LOGGER.error("map decoration type cant be null. how did this happen?");
                        }
                        if (type instanceof MLSpecialMapDecorationType<?, ?> c) {
                            return Either.left(c);
                        }
                        return Either.right((MLJsonMapDecorationType) type);
                    }));

    public static final Codec<MLMapDecorationType<?, ?>> DIRECT_NETWORK_CODEC =
            Codec.lazyInitialized(() -> Codec.either(MLSpecialMapDecorationType.CODEC, MLJsonMapDecorationType.NETWORK_CODEC).xmap(
                    either -> either.map(s -> s, c -> c),
                    type -> {
                        if (type == null) {
                            Moonlight.LOGGER.error("map decoration type cant be null. how did this happen?");
                        }
                        if (type instanceof MLSpecialMapDecorationType<?, ?> c) {
                            return Either.left(c);
                        }
                        return Either.right((MLJsonMapDecorationType) type);
                    }));


    // registry reference codec
    public static final Codec<Holder<MLMapDecorationType<?, ?>>> CODEC = RegistryFileCodec.create(MapDataRegistry.REGISTRY_KEY, DIRECT_CODEC);
    // registry reference network codec
    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<MLMapDecorationType<?, ?>>> STREAM_CODEC = ByteBufCodecs.holderRegistry(MapDataRegistry.REGISTRY_KEY);

    private final StreamCodec<RegistryFriendlyByteBuf, D> decorationCodec;
    private final MapCodec<M> markerCodec;

    protected MLMapDecorationType( MapCodec<M> markerCodec, StreamCodec<RegistryFriendlyByteBuf, D> decorationCodec) {
        this.decorationCodec = decorationCodec;
        this.markerCodec = markerCodec;
    }


    /**
     * If this marker should be saved to disk as its been grabbed from a world block
     */
    @ApiStatus.Internal
    abstract boolean isFromWorld();

    public ResourceLocation getCustomFactoryID() {
        return ResourceLocation.parse("");
    }

    @Nullable
    public abstract M createMarkerFromWorld(BlockGetter reader, BlockPos pos);

    public int getDefaultMapColor() {
        return 1;
    }

    public Optional<HolderSet<Structure>> getAssociatedStructure() {
        return Optional.empty();
    }

    //decoration, not saved, sent to the client
    public StreamCodec<? super RegistryFriendlyByteBuf, D> getDecorationCodec() {
        return decorationCodec;
    }

    //markers. saved and stored in nbt
    public MapCodec<M> getMarkerCodec() {
        return markerCodec;
    }

}
