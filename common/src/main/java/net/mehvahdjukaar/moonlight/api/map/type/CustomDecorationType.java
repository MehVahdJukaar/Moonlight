package net.mehvahdjukaar.moonlight.api.map.type;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.moonlight.api.map.CustomMapDecoration;
import net.mehvahdjukaar.moonlight.api.map.MapDataRegistry;
import net.mehvahdjukaar.moonlight.api.map.markers.MapBlockMarker;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockGetter;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

// Equivalent of TileEntityType.
// Singleton, which will be in charge of creating CustomDecoration and MapBlockMarker instances
// Used for custom implementations
public final class CustomDecorationType<D extends CustomMapDecoration, M extends MapBlockMarker<D>> implements MapDecorationType<D, M> {

    public static final Codec<CustomDecorationType<?, ?>> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("custom_type").forGetter(CustomDecorationType::getCustomFactoryID)
    ).apply(instance, MapDataRegistry::getCustomType));

    //This is not the decoration id. Single instance will be registered with multiple ids based off json
    @ApiStatus.Internal
    public ResourceLocation factoryId;

    //used to restore decorations from nbt
    private final BiFunction<MapDecorationType<?, ?>, FriendlyByteBuf, D> decorationFactory;

    //creates empty marker
    @NotNull
    private final Function<CustomDecorationType<D, M>, M> markerFactory;
    //creates marker from world
    @Nullable
    private final BiFunction<BlockGetter, BlockPos, M> markerFromWorldFactory;

    /**
     * Normal constructor for a decoration type that has a world marker associated. i.e: banners
     *
     * @param markerFactory          world marker factory
     * @param markerFromWorldFactory function that retrieves an optional world marker from the world at a certain pos
     * @param decorationFactory      read decoration data from buffer
     */
    private CustomDecorationType(ResourceLocation typeId,
                                 BiFunction<MapDecorationType<?, ?>, FriendlyByteBuf, D> decorationFactory,
                                 Function<CustomDecorationType<D, M>,M> markerFactory,
                                 @Nullable BiFunction<BlockGetter, BlockPos, M> markerFromWorldFactory) {
        this.factoryId = typeId;
        this.markerFactory = markerFactory;
        this.markerFromWorldFactory = markerFromWorldFactory;
        this.decorationFactory = decorationFactory;
    }

    /**
     * Use for decoration that is tied to an in world block (represented by their marker)
     */
    public static <D extends CustomMapDecoration, M extends MapBlockMarker<D>> CustomDecorationType<D, M> withWorldMarker(
            Function<CustomDecorationType<D, M>, M> markerFactory,
            @Nullable BiFunction<BlockGetter, BlockPos, M> markerFromWorldFactory,
            BiFunction<MapDecorationType<?, ?>, FriendlyByteBuf, D> decorationFactory) {
        return new CustomDecorationType<>(null, decorationFactory, markerFactory, markerFromWorldFactory);
    }

    @Deprecated(forRemoval = true)
    public static <D extends CustomMapDecoration, M extends MapBlockMarker<D>> CustomDecorationType<D, M> withWorldMarker(
            ResourceLocation typeId, Supplier<M> markerFactory,
            @Nullable BiFunction<BlockGetter, BlockPos, M> markerFromWorldFactory,
            BiFunction<MapDecorationType<?, ?>, FriendlyByteBuf, D> decorationFactory) {
        return new CustomDecorationType<>(typeId, decorationFactory, t -> markerFactory.get(), markerFromWorldFactory);
    }

    @Deprecated(forRemoval = true)
    public static <D extends CustomMapDecoration, M extends MapBlockMarker<D>> CustomDecorationType<D, M> simple(
            ResourceLocation typeId, Supplier<M> markerFactory,
            BiFunction<MapDecorationType<?, ?>, FriendlyByteBuf, D> decorationFactory) {
        return new CustomDecorationType<>(typeId, decorationFactory, t -> markerFactory.get(), null);
    }

    /**
     * For persistent decoration that is not associated to a world block. Still have a marker as they need to be saved
     */
    public static <D extends CustomMapDecoration, M extends MapBlockMarker<D>> CustomDecorationType<D, M> simple(
            Function<CustomDecorationType<D, M>, M> markerFactory,
            BiFunction<MapDecorationType<?, ?>, FriendlyByteBuf, D> decorationFactory) {
        return new CustomDecorationType<>(null, decorationFactory, markerFactory, null);
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
    public D loadDecorationFromBuffer(FriendlyByteBuf buffer) {
        try {
            return decorationFactory.apply(this, buffer);
        } catch (Exception e) {
            Moonlight.LOGGER.warn("Failed to load custom map decoration for decoration type" + this + ": " + e);
        }
        return null;
    }

    @Override
    @Nullable
    public M loadMarkerFromNBT(CompoundTag compound) {
        M marker = markerFactory.apply(this);
        try {
            marker.loadFromNBT(compound);
            return marker;
        } catch (Exception e) {
            Moonlight.LOGGER.warn("Failed to load world map marker for decoration type" + this + ": " + e);
        }
        return null;
    }

    @Override
    @Nullable
    public M getWorldMarkerFromWorld(BlockGetter reader, BlockPos pos) {
        return markerFromWorldFactory != null ? markerFromWorldFactory.apply(reader, pos) : null;
    }

    @Override
    public M createEmptyMarker() {
        return markerFactory.apply(this);
    }
}
