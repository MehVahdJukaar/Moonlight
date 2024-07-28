package net.mehvahdjukaar.moonlight.api.map.type;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.moonlight.api.map.MapDataRegistry;
import net.mehvahdjukaar.moonlight.api.map.markers.MapBlockMarker;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockGetter;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;
import java.util.function.Function;

// Equivalent of TileEntityType.
// Singleton, which will be in charge of creating CustomDecoration and MapBlockMarker instances
// Used for custom implementations
public final class MLSpecialMapDecorationType<D extends MLMapDecoration, M extends MapBlockMarker<D>> implements MlMapDecorationType<D, M> {

    static final Codec<MLSpecialMapDecorationType<?, ?>> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("custom_type").forGetter(MLSpecialMapDecorationType::getCustomFactoryID)
    ).apply(instance, MapDataRegistry::getCustomType));

    //This is not the decoration id. Single instance will be registered with multiple ids based off json
    //this is the type id?
    @ApiStatus.Internal
    public ResourceLocation factoryId;

    //used to restore decorations from nbt
    private final StreamCodec<RegistryFriendlyByteBuf, D> decorationCodec;

    //creates empty marker
    @NotNull
    private final Function<MLSpecialMapDecorationType<D, M>, M> markerFactory;
    //creates marker from world
    @Nullable
    private final BiFunction<BlockGetter, BlockPos, M> markerFromWorldFactory;

    /**
     * Normal constructor for a decoration type that has a world marker associated. i.e: banners
     *
     * @param markerFactory          world marker factory
     * @param markerFromWorldFactory function that retrieves an optional world marker from the world at a certain pos
     * @param decorationCodec        read decoration data from buffer
     */
    private MLSpecialMapDecorationType(ResourceLocation typeId,
                                       StreamCodec<RegistryFriendlyByteBuf, D> decorationCodec,
                                       Function<MLSpecialMapDecorationType<D, M>, M> markerFactory,
                                       @Nullable BiFunction<BlockGetter, BlockPos, M> markerFromWorldFactory) {
        this.factoryId = typeId;
        this.markerFactory = markerFactory;
        this.markerFromWorldFactory = markerFromWorldFactory;

        this.decorationCodec = decorationCodec.map(d -> {
            d.type = this;
            return d;
        }, Function.identity());
    }

    /**
     * Use for decoration that is tied to an in world block (represented by their marker)
     */
    public static <D extends MLMapDecoration, M extends MapBlockMarker<D>> MLSpecialMapDecorationType<D, M> withWorldMarker(
            Function<MLSpecialMapDecorationType<D, M>, M> markerFactory,
            @Nullable BiFunction<BlockGetter, BlockPos, M> markerFromWorldFactory,
            StreamCodec<RegistryFriendlyByteBuf, D> decorationFactory) {
        return new MLSpecialMapDecorationType<>(null, decorationFactory, markerFactory, markerFromWorldFactory);
    }

    /**
     * For persistent decoration that is not associated to a world block. Still have a marker as they need to be saved
     */
    public static <D extends MLMapDecoration, M extends MapBlockMarker<D>> MLSpecialMapDecorationType<D, M> simple(
            Function<MLSpecialMapDecorationType<D, M>, M> markerFactory,
            StreamCodec<RegistryFriendlyByteBuf, D> decorationFactory) {
        return new MLSpecialMapDecorationType<>(null, decorationFactory, markerFactory, null);
    }

    @Override
    public ResourceLocation getCustomFactoryID() {
        return factoryId;
    }

    @Override
    public boolean isFromWorld() {
        return markerFromWorldFactory != null;
    }

    @Override
    @Nullable
    public M load(CompoundTag compound, HolderLookup.Provider registries) {
        M marker = markerFactory.apply(this);
        try {
            marker.load(compound, registries);
            return marker;
        } catch (Exception e) {
            Moonlight.LOGGER.warn("Failed to load world map marker for decoration type{}: {}", this, e);
        }
        return null;
    }

    @Override
    @Nullable
    public M getWorldMarkerFromWorld(BlockGetter reader, BlockPos pos) {
        return markerFromWorldFactory != null ? markerFromWorldFactory.apply(reader, pos) : null;
    }

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, ? extends MLMapDecoration> getDecorationCodec() {
        return decorationCodec;
    }

    @Override
    public M createEmptyMarker() {
        return markerFactory.apply(this);
    }
}
