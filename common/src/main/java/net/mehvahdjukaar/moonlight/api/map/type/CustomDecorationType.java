package net.mehvahdjukaar.moonlight.api.map.type;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.moonlight.api.map.CustomMapDecoration;
import net.mehvahdjukaar.moonlight.api.map.MapDecorationRegistry;
import net.mehvahdjukaar.moonlight.api.map.markers.DummyMapBlockMarker;
import net.mehvahdjukaar.moonlight.api.map.markers.MapBlockMarker;
import net.mehvahdjukaar.moonlight.api.misc.TriFunction;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;
import java.util.function.Supplier;

//equivalent of TileEntityType. Singleton which will be in charge of creating CustomDecoration and MapBlockMarker instances
//used for custom implementations
public final class CustomDecorationType<D extends CustomMapDecoration, M extends MapBlockMarker<D>> extends MapDecorationType<D, M> {

    public static final Codec<CustomDecorationType<?, ?>> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("custom_type").forGetter(MapDecorationType::getCustomFactoryID)
    ).apply(instance, MapDecorationRegistry::getCustomType));

    private final ResourceLocation id; //just stored here instead than in a registry

    //used to restore decorations from nbt
    private final BiFunction<MapDecorationType<?, ?>, FriendlyByteBuf, D> decorationFactory;

    //creates empty marker. optional
    private final Supplier<M> markerFactory;
    //creates marker from world
    private final BiFunction<BlockGetter, BlockPos, M> markerFromWorldFactory;
    //for non-permanent decoration like player icon, only visible to player holding the map
    private final TriFunction<MapDecorationType<?, ?>, Player, MapItemSavedData, D> volatileFactory;

    /**
     * Normal constructor for decoration type that has a world marker associated. i.e: banners
     *
     * @param markerFactory          world marker factory
     * @param markerFromWorldFactory function that retrieves an optional world marker from the world at a certain pos
     * @param decorationFactory      read decoration data from buffer
     * @param volatileFactory        creates a non-persistent decoration onlh visible to the active player
     */
    public CustomDecorationType(ResourceLocation typeId,
                                BiFunction<MapDecorationType<?, ?>, FriendlyByteBuf, D> decorationFactory,
                                @Nullable Supplier<M> markerFactory,
                                @Nullable BiFunction<BlockGetter, BlockPos, M> markerFromWorldFactory,
                                @Nullable TriFunction<MapDecorationType<?, ?>, Player, MapItemSavedData, D> volatileFactory) {
        this.id = typeId;
        this.markerFactory = markerFactory;
        this.markerFromWorldFactory = markerFromWorldFactory;
        this.decorationFactory = decorationFactory;
        this.volatileFactory = volatileFactory;
    }

    /**
     * Use for decoration that is tied to an in world block (represented by their marker)
     */
    public static <D extends CustomMapDecoration, M extends MapBlockMarker<D>> CustomDecorationType<D, M> withWorldMarker(ResourceLocation typeId, @Nullable Supplier<M> markerFactory,
                                                                                                                          @Nullable BiFunction<BlockGetter, BlockPos, M> markerFromWorldFactory,
                                                                                                                          BiFunction<MapDecorationType<?, ?>, FriendlyByteBuf, D> decorationFactory) {
        return new CustomDecorationType<>(typeId, decorationFactory, markerFactory, markerFromWorldFactory, null);
    }

    /**
     * For persistent decoration that is not associated to a world block
     */
    public static <D extends CustomMapDecoration, M extends MapBlockMarker<D>> CustomDecorationType<D, M> simple(
            ResourceLocation typeId, BiFunction<MapDecorationType<?, ?>, FriendlyByteBuf, D> decorationFactory) {
        return new CustomDecorationType<>(typeId, decorationFactory, null, null, null);
    }

    /**
     * For non-persistent decoration that is only visible to the player holding the map
     */
    public static <D extends CustomMapDecoration, M extends MapBlockMarker<D>> CustomDecorationType<D, M> dynamic(
            ResourceLocation typeId,
            BiFunction<MapDecorationType<?, ?>, FriendlyByteBuf, D> decorationFactory,
            TriFunction<MapDecorationType<?, ?>, Player, MapItemSavedData, D> volatileFactory
    ) {
        return new CustomDecorationType<>(typeId, decorationFactory, null, null, volatileFactory);
    }

    @Deprecated(forRemoval = true)
    public CustomDecorationType(ResourceLocation typeId, Supplier<M> markerFactory, BiFunction<BlockGetter, BlockPos, M> markerFromWorldFactory,
                                BiFunction<MapDecorationType<?, ?>, FriendlyByteBuf, D> decorationFactory) {
        this(typeId, decorationFactory, markerFactory, markerFromWorldFactory, null);
    }

    /**
     * For one with no marker
     */
    @Deprecated(forRemoval = true)
    public CustomDecorationType(ResourceLocation typeId, BiFunction<MapDecorationType<?, ?>,
            FriendlyByteBuf, D> decoFromBuffer) {
        this(typeId, null, null, decoFromBuffer);
    }

    @Override
    public ResourceLocation getCustomFactoryID() {
        return id;
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
    public D getDynamicDecoration(Player player, MapItemSavedData data){
        if(volatileFactory != null){
            return volatileFactory.apply(this, player, data);
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

    @Override
    public MapBlockMarker<D> getDefaultMarker(BlockPos pos) {
        if (markerFactory != null) {
            var m = markerFactory.get();
            m.setPos(pos);
            return m;
        }
        return new DummyMapBlockMarker<>(this, pos);
    }
}
