package net.mehvahdjukaar.moonlight.api.map.decoration;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.moonlight.api.misc.TriFunction;
import net.mehvahdjukaar.moonlight.core.map.MapDataInternal;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockGetter;
import org.jetbrains.annotations.Nullable;

// Equivalent of TileEntityType.
// Singleton, which will be in charge of creating CustomDecoration and MapBlockMarker instances
// Used for custom implementations
public final class MLSpecialMapDecorationType<D extends MLMapDecoration, M extends MLMapMarker<D>> extends MLMapDecorationType<D, M> {

    static final Codec<MLSpecialMapDecorationType<?, ?>> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("custom_type").forGetter(MLSpecialMapDecorationType::getCustomFactoryID)
    ).apply(instance, MapDataInternal::createCustomType));

    //creates marker from world
    @Nullable
    private final TriFunction<Holder<MLMapDecorationType<?, ?>>, BlockGetter, BlockPos, M> markerFromWorldFactory;

    /**
     * Normal constructor for a decoration type that has a world marker associated. i.e: banners
     *
     * @param markerFromWorldFactory function that retrieves an optional world marker from the world at a certain pos
     * @param decorationCodec        read decoration data from buffer
     */
    private MLSpecialMapDecorationType(MapCodec<M> markerCodec,
                                       StreamCodec<RegistryFriendlyByteBuf, D> decorationCodec,
                                       @Nullable TriFunction<Holder<MLMapDecorationType<?, ?>>, BlockGetter, BlockPos, M> markerFromWorldFactory) {
        super(markerCodec, decorationCodec);
        this.markerFromWorldFactory = markerFromWorldFactory;
    }

    /**
     * Use for decoration that is tied to an in world block (represented by their marker)
     */
    public static <D extends MLMapDecoration, M extends MLMapMarker<D>> MLSpecialMapDecorationType<D, M> fromWorldCustomMarker(
            MapCodec<M> markerCodec,
            StreamCodec<RegistryFriendlyByteBuf, D> decorationCodec,
            TriFunction<Holder<MLMapDecorationType<?, ?>>, BlockGetter, BlockPos, M> markerFromWorldFactory) {
        return new MLSpecialMapDecorationType<>(markerCodec, decorationCodec, markerFromWorldFactory);
    }

    public static MLSpecialMapDecorationType<MLMapDecoration, SimpleMapMarker> fromWorldSimple(
            TriFunction<Holder<MLMapDecorationType<?, ?>>, BlockGetter, BlockPos, SimpleMapMarker> markerFromWorldFactory) {
        return fromWorldCustomMarker(SimpleMapMarker.DIRECT_CODEC, MLMapDecoration.DIRECT_CODEC, markerFromWorldFactory);
    }

    /**
     * For persistent decoration that is not associated to a world block. Still have a marker as they need to be saved
     */
    public static <D extends MLMapDecoration, M extends MLMapMarker<D>> MLSpecialMapDecorationType<D, M> standaloneCustomMarker(
            MapCodec<M> markerCodec,
            StreamCodec<RegistryFriendlyByteBuf, D> decorationCode) {
        return new MLSpecialMapDecorationType<>(markerCodec, decorationCode, null);
    }

    @Override
    public boolean isFromWorld() {
        return markerFromWorldFactory != null;
    }

    @Override
    @Nullable
    public M createMarkerFromWorld(BlockGetter reader, BlockPos pos) {
        return markerFromWorldFactory != null ? markerFromWorldFactory.apply(wrapAsHolder(), reader, pos) : null;
    }
}
