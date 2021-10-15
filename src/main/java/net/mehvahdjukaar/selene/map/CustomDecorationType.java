package net.mehvahdjukaar.selene.map;

import net.mehvahdjukaar.selene.Selene;
import net.mehvahdjukaar.selene.map.markers.MapWorldMarker;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;

import javax.annotation.Nullable;
import java.util.function.BiFunction;
import java.util.function.Supplier;

//equivalent of TileEntityType
public class CustomDecorationType<D extends CustomDecoration, M extends MapWorldMarker<D>> {
    private final ResourceLocation id;
    private final Supplier<M> markerFactory;
    private final BiFunction<BlockGetter,BlockPos,M> markerFromWorldFactory;
    private final BiFunction<CustomDecorationType<?,?>,FriendlyByteBuf,D> decorationFactory;
    private final boolean hasMarker;

    /**
     * Normal constructor for decoration type that has a world marker associated. i.e: banners
     * @param id registry id
     * @param markerFactory world marker factory
     * @param markerFromWorldFactory function that retrieves an optional world marker from the world at a certain pos
     * @param decorationFactory read decoration data from buffer
     */
    public CustomDecorationType(ResourceLocation id, Supplier<M> markerFactory, BiFunction<BlockGetter,BlockPos,M>markerFromWorldFactory,
                                BiFunction<CustomDecorationType<?,?>,FriendlyByteBuf,D> decorationFactory){
        this.id = id;
        this.markerFactory = markerFactory;
        this.markerFromWorldFactory = markerFromWorldFactory;
        this.decorationFactory = decorationFactory;
        this.hasMarker = true;
    }
    public CustomDecorationType(ResourceLocation id,BiFunction<CustomDecorationType<?,?>,FriendlyByteBuf,D> decoFromBuffer){
        this.id = id;
        this.markerFactory = ()->null;
        this.markerFromWorldFactory = (s, d)->null;
        this.decorationFactory = decoFromBuffer;
        this.hasMarker = false;
    }


    public boolean hasMarker() {
        return hasMarker;
    }

    public ResourceLocation getId() {
        return id;
    }

    public String getRegistryId() {
        return id.toString();
    }

    @Nullable
    public D loadDecorationFromBuffer(FriendlyByteBuf buffer){
        try {
            return decorationFactory.apply(this, buffer);
        }catch (Exception e){
            Selene.LOGGER.warn("Failed to load custom map decoration for decoration type"+this.getRegistryId()+": "+e);
        }
        return null;
    }

    @Nullable
    public M loadMarkerFromNBT(CompoundTag compound){
        if(hasMarker){
            M marker = markerFactory.get();
            try {
                marker.loadFromNBT(compound);
                return marker;
            }catch (Exception e){
                Selene.LOGGER.warn("Failed to load world map marker for decoration type"+this.getRegistryId()+": "+e);
            }
        }
        return null;
    }

    @Nullable
    public M getWorldMarkerFromWorld(BlockGetter reader, BlockPos pos){
        return hasMarker ? markerFromWorldFactory.apply(reader,pos) : null;
    }


}
