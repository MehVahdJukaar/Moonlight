package net.mehvahdjukaar.moonlight.api.map.decoration;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.mehvahdjukaar.moonlight.api.map.MapDataRegistry;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.map.MapDataInternal;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
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


    // don't use to save anything: allowInline=false only guards decode, and only when given RegistryOps that
    // actually have our registry. Encoding always inlines the whole type definition when it can't see it.
    // use REFERENCE_CODEC instead
    @ApiStatus.Internal
    public static final Codec<Holder<MLMapDecorationType<?, ?>>> CODEC = RegistryFileCodec.create(MapDataRegistry.MAP_DECORATION_REGISTRY_KEY, DIRECT_CODEC, false);

    // reference only. Unlike CODEC this never writes a type definition inline, so it errors out cleanly
    // when it's given ops without our registry instead of saving something that can't be read back
    public static final Codec<Holder<MLMapDecorationType<?, ?>>> REFERENCE_CODEC =
            RegistryFixedCodec.create(MapDataRegistry.MAP_DECORATION_REGISTRY_KEY);

    // registry reference network codec
    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<MLMapDecorationType<?, ?>>> STREAM_CODEC =
            ByteBufCodecs.holderRegistry(MapDataRegistry.MAP_DECORATION_REGISTRY_KEY);

    private final StreamCodec<RegistryFriendlyByteBuf, D> decorationCodec;
    private final MapCodec<M> markerCodec;

    protected MLMapDecorationType(MapCodec<M> markerCodec, StreamCodec<RegistryFriendlyByteBuf, D> decorationCodec) {
        this.decorationCodec = decorationCodec;
        this.markerCodec = markerCodec;
    }


    /**
     * If this marker should be saved to disk as its been grabbed from a world block
     */
    @ApiStatus.Internal
    abstract boolean isFromWorld();

    public abstract ResourceLocation getCustomFactoryID();

    @Nullable
    public abstract M createMarkerFromWorld(LevelAccessor reader, BlockPos pos);

    @Deprecated(forRemoval = true)
    public M createMarkerFromWorld(BlockGetter reader, BlockPos pos) {
        if (reader instanceof Level l) {
            return createMarkerFromWorld(l, pos);
        }
        return null;
    }

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

    @Deprecated(forRemoval = true)
    protected Holder<MLMapDecorationType<?, ?>> wrapAsHolder() {
        return MapDataInternal.hackyGetRegistry().wrapAsHolder(this);
    }

    protected Holder<MLMapDecorationType<?, ?>> wrapAsHolder(RegistryAccess reg) {
        return reg.registryOrThrow(MapDataRegistry.MAP_DECORATION_REGISTRY_KEY)
                .wrapAsHolder(this);
    }
}
