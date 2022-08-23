package net.mehvahdjukaar.moonlight.api.map;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.KeyDispatchCodec;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.moonlight.api.map.markers.MapBlockMarker;
import net.mehvahdjukaar.moonlight.api.map.type.CustomDecorationType;
import net.mehvahdjukaar.moonlight.api.map.type.MapDecorationType;
import net.mehvahdjukaar.moonlight.api.map.type.SimpleDecorationType;
import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.data.models.blockstates.PropertyDispatch;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class MapDecorationRegistry {

    public static final Codec<MapDecorationType<?, ?>> TYPE_CODEC =
            Codec.either(CustomDecorationType.CODEC, SimpleDecorationType.CODEC).xmap(
                    either -> either.map(s -> s, c -> c),
                    type -> {
                        if(type == null){
                            Moonlight.LOGGER.error("map decoration type cant be null. how did this happen?");
                        }
                        if (type instanceof CustomDecorationType<?, ?> c) {
                            return Either.left(c);
                        }
                        return Either.right((SimpleDecorationType) type);
                    });

    //data holder


    public static final Map<ResourceLocation, CustomDataHolder<?>> CUSTOM_MAP_DATA_TYPES = new HashMap<>();


    /**
     * Registers a custom data type to be stored in map data
     *
     * @param name          id
     * @param type          data type class
     * @param load          load function
     * @param save          save function
     * @param onItemUpdate  callback for when a map item is updated
     * @param onItemTooltip callback on map item tooltip
     * @param <T>           data type
     */
    public static <T> void registerCustomMapSavedData(ResourceLocation name, Class<T> type,
                                                      Function<CompoundTag, T> load,
                                                      BiConsumer<CompoundTag, T> save,
                                                      PropertyDispatch.TriFunction<MapItemSavedData, Entity, T, Boolean> onItemUpdate,
                                                      PropertyDispatch.TriFunction<MapItemSavedData, ItemStack, T, Component> onItemTooltip) {
        if (CUSTOM_MAP_DATA_TYPES.containsKey(name)) {
            throw new IllegalArgumentException("Duplicate custom map data registration " + name);
        } else {
            CUSTOM_MAP_DATA_TYPES.put(name, new CustomDataHolder<>(name, load, save, onItemUpdate, onItemTooltip));
        }
    }


    //map markers

    public static final MapDecorationType<CustomMapDecoration, ?> GENERIC_STRUCTURE_TYPE = new SimpleDecorationType(Optional.empty());

    public static final Map<ResourceLocation, Supplier<CustomDecorationType<?, ?>>> CODE_TYPES_FACTORIES = new HashMap<>();

    /**
     * Call before mod setup. Register a code defined map marker
     * For now only works for FORGE as fabric has some unknown bugs I cant figure out involving vanilla codecs
     */
    public static void register(ResourceLocation id, Supplier<CustomDecorationType<?, ?>> markerType) {
        if(PlatformHelper.getPlatform().isForge()) {
            CODE_TYPES_FACTORIES.put(id, markerType);
            registerInternal(id, markerType::get);
        }
    }

    /**
     * creates & registers a simple decoration with no associated world marker.<br>
     * useful for exploration maps. It's however better to add these via data pack
     *
     * @param modId mod id
     * @param name  decoration name
     */
    public static void registerSimple(String modId, String name) {
        var id = new ResourceLocation(modId, name);
        register(id, () -> new CustomDecorationType<>(id, CustomMapDecoration::new));
    }

    @ExpectPlatform
    private static void registerInternal(ResourceLocation id, Supplier<MapDecorationType<?, ?>> markerType) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void init() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static ResourceKey<Registry<MapDecorationType<?, ?>>> getRegistryKey() {
        throw new AssertionError();
    }

    public static Registry<MapDecorationType<?, ?>> getDataPackRegistry() {
        return Utils.hackyGetRegistryAccess().registryOrThrow(getRegistryKey());
    }

    public static Collection<MapDecorationType<?, ?>> getValues() {
        return getDataPackRegistry().stream().toList();
    }

    public static Set<Map.Entry<ResourceKey<MapDecorationType<?, ?>>, MapDecorationType<?, ?>>> getEntries() {
        return getDataPackRegistry().entrySet();
    }

    @Nullable
    public static ResourceLocation getID(MapDecorationType<?, ?> s) {
        return getDataPackRegistry().getKey(s);
    }

    @Nullable
    public static MapDecorationType<? extends CustomMapDecoration, ?> get(String id) {
        return get(new ResourceLocation(id));
    }

    public static MapDecorationType<?, ?> get(ResourceLocation id) {
        return getDataPackRegistry().get(id);
    }

    public static Optional<MapDecorationType<?, ?>> getOptional(ResourceLocation id) {
        return getDataPackRegistry().getOptional(id);
    }


    @Nullable
    @ApiStatus.Internal
    public static MapBlockMarker<?> readWorldMarker(CompoundTag compound) {
        for (var e : getEntries()) {
            String id = e.getKey().location().toString();
            if (compound.contains(id)) {
                return e.getValue().loadMarkerFromNBT(compound.getCompound(id));
            }
        }
        return null;
    }

    /**
     * returns a list of suitable world markers associated to a position. called by mixin code
     *
     * @param reader world
     * @param pos    world position
     * @return markers found, null if none found
     */
    @ApiStatus.Internal
    public static List<MapBlockMarker<?>> getMarkersFromWorld(BlockGetter reader, BlockPos pos) {
        List<MapBlockMarker<?>> list = new ArrayList<>();
        for (MapDecorationType<?, ?> type : getValues()) {
            MapBlockMarker<?> c = type.getWorldMarkerFromWorld(reader, pos);
            if (c != null) list.add(c);
        }
        return list;
    }


}
