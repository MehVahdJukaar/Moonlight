package net.mehvahdjukaar.moonlight.api.map.type;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.moonlight.api.map.CustomMapDecoration;
import net.mehvahdjukaar.moonlight.api.map.MapDecorationRegistry;
import net.mehvahdjukaar.moonlight.api.map.markers.MapBlockMarker;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockGetter;

import org.jetbrains.annotations.Nullable;
import java.util.function.BiFunction;
import java.util.function.Supplier;

//equivalent of TileEntityType. Singleton which will be in charge of creating CustomDecoration and MapBlockMarker instances
//used for custom implementations
public final class CustomDecorationType<D extends CustomMapDecoration, M extends MapBlockMarker<D>> extends MapDecorationType<D, M> {


    public static final Codec<CustomDecorationType<?, ?>> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("custom_type").forGetter(MapDecorationType::getCustomFactoryID)
    ).apply(instance, MapDecorationRegistry::getCustomType));


    private final Supplier<M> markerFactory;
    private final BiFunction<BlockGetter, BlockPos, M> markerFromWorldFactory;
    private final BiFunction<MapDecorationType<?, ?>, FriendlyByteBuf, D> decorationFactory;
    private final ResourceLocation factoryID;

    /**
     * Normal constructor for decoration type that has a world marker associated. i.e: banners
     *
     * @param markerFactory          world marker factory
     * @param markerFromWorldFactory function that retrieves an optional world marker from the world at a certain pos
     * @param decorationFactory      read decoration data from buffer
     */
    public CustomDecorationType(ResourceLocation typeId, Supplier<M> markerFactory, BiFunction<BlockGetter, BlockPos, M> markerFromWorldFactory,
                                BiFunction<MapDecorationType<?, ?>, FriendlyByteBuf, D> decorationFactory) {
        this.markerFactory = markerFactory;
        this.markerFromWorldFactory = markerFromWorldFactory;
        this.decorationFactory = decorationFactory;
        this.factoryID = typeId;
    }

    public CustomDecorationType(ResourceLocation typeId, BiFunction<MapDecorationType<?, ?>,
            FriendlyByteBuf, D> decoFromBuffer) {
        this(typeId, null, null, decoFromBuffer);
    }

    @Override
    public ResourceLocation getCustomFactoryID() {
        return factoryID;
    }

    @Override
    public boolean hasMarker() {
        return markerFactory != null;
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
        if (hasMarker()) {
            M marker = markerFactory.get();
            try {
                marker.loadFromNBT(compound);
                return marker;
            } catch (Exception e) {
                Moonlight.LOGGER.warn("Failed to load world map marker for decoration type" + this + ": " + e);
            }
        }
        return null;
    }

    @Override
    @Nullable
    public M getWorldMarkerFromWorld(BlockGetter reader, BlockPos pos) {
        return hasMarker() ? markerFromWorldFactory.apply(reader, pos) : null;
    }

}
