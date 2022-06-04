package net.mehvahdjukaar.selene.map.type;

import net.mehvahdjukaar.selene.Selene;
import net.mehvahdjukaar.selene.map.CustomMapDecoration;
import net.mehvahdjukaar.selene.map.markers.MapBlockMarker;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockGetter;

import javax.annotation.Nullable;
import java.util.function.BiFunction;
import java.util.function.Supplier;

//equivalent of TileEntityType. Singleton which will be in charge of creating CustomDecoration and MapBlockMarker instances
//used for custom implementations
public class CustomDecorationType<D extends CustomMapDecoration, M extends MapBlockMarker<D>> implements IMapDecorationType<D, M> {

    protected final ResourceLocation id;
    protected final Supplier<M> markerFactory;
    protected final BiFunction<BlockGetter, BlockPos, M> markerFromWorldFactory;
    protected final BiFunction<IMapDecorationType<?, ?>, FriendlyByteBuf, D> decorationFactory;
    protected final boolean hasMarker;

    /**
     * Normal constructor for decoration type that has a world marker associated. i.e: banners
     *
     * @param id                     registry id
     * @param markerFactory          world marker factory
     * @param markerFromWorldFactory function that retrieves an optional world marker from the world at a certain pos
     * @param decorationFactory      read decoration data from buffer
     */
    public CustomDecorationType(ResourceLocation id, Supplier<M> markerFactory, BiFunction<BlockGetter, BlockPos, M> markerFromWorldFactory,
                                BiFunction<IMapDecorationType<?, ?>, FriendlyByteBuf, D> decorationFactory) {
        this.id = id;
        this.markerFactory = markerFactory;
        this.markerFromWorldFactory = markerFromWorldFactory;
        this.decorationFactory = decorationFactory;
        this.hasMarker = true;
    }

    public CustomDecorationType(ResourceLocation id, BiFunction<IMapDecorationType<?, ?>,
            FriendlyByteBuf, D> decoFromBuffer) {
        this.id = id;
        this.markerFactory = () -> null;
        this.markerFromWorldFactory = (s, d) -> null;
        this.decorationFactory = decoFromBuffer;
        this.hasMarker = false;
    }

    @Override
    public boolean hasMarker() {
        return hasMarker;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public String toString() {
        return getId().toString();
    }

    @Override
    @Nullable
    public D loadDecorationFromBuffer(FriendlyByteBuf buffer) {
        try {
            return decorationFactory.apply(this, buffer);
        } catch (Exception e) {
            Selene.LOGGER.warn("Failed to load custom map decoration for decoration type" + this.getId() + ": " + e);
        }
        return null;
    }


    @Override
    @Nullable
    public M loadMarkerFromNBT(CompoundTag compound) {
        if (hasMarker) {
            M marker = markerFactory.get();
            try {
                marker.loadFromNBT(compound);
                return marker;
            } catch (Exception e) {
                Selene.LOGGER.warn("Failed to load world map marker for decoration type" + this.getId() + ": " + e);
            }
        }
        return null;
    }

    @Override
    @Nullable
    public M getWorldMarkerFromWorld(BlockGetter reader, BlockPos pos) {
        return hasMarker ? markerFromWorldFactory.apply(reader, pos) : null;
    }

}
