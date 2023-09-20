package net.mehvahdjukaar.moonlight.api.map;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.moonlight.api.map.markers.MapBlockMarker;
import net.mehvahdjukaar.moonlight.api.map.type.CustomDecorationType;
import net.mehvahdjukaar.moonlight.api.map.type.JsonDecorationType;
import net.mehvahdjukaar.moonlight.api.map.type.MapDecorationType;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.core.*;
import net.minecraft.data.models.blockstates.PropertyDispatch;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Function;

//TODO: split into api and core
public class MapDecorationRegistry {

    //pain
    @ApiStatus.Internal
    public static final Codec<MapDecorationType<?, ?>> CODEC =
            Codec.either(CustomDecorationType.CODEC, JsonDecorationType.CODEC).xmap(
                    either -> either.map(s -> s, c -> c),
                    type -> {
                        if (type == null) {
                            Moonlight.LOGGER.error("map decoration type cant be null. how did this happen?");
                        }
                        if (type instanceof CustomDecorationType<?, ?> c) {
                            return Either.left(c);
                        }
                        return Either.right((JsonDecorationType) type);
                    });
    @ApiStatus.Internal
    public static final Codec<MapDecorationType<?, ?>> NETWORK_CODEC =
            Codec.either(CustomDecorationType.CODEC, JsonDecorationType.NETWORK_CODEC).xmap(
                    either -> either.map(s -> s, c -> c),
                    type -> {
                        if (type == null) {
                            Moonlight.LOGGER.error("map decoration type cant be null. how did this happen?");
                        }
                        if (type instanceof CustomDecorationType<?, ?> c) {
                            return Either.left(c);
                        }
                        return Either.right((JsonDecorationType) type);
                    });

    //data holder
    @ApiStatus.Internal
    public static final Map<ResourceLocation, CustomMapData.Type<?>> CUSTOM_MAP_DATA_TYPES = new LinkedHashMap<>();

    /**
     * Registers a custom data type to be stored in map data. Type will provide its onw data implementation
     **/
    public static <T extends CustomMapData> CustomMapData.Type<T> registerCustomMapSavedData(CustomMapData.Type<T> type) {
        if (CUSTOM_MAP_DATA_TYPES.containsKey(type.id())) {
            throw new IllegalArgumentException("Duplicate custom map data registration " + type.id());
        } else {
            CUSTOM_MAP_DATA_TYPES.put(type.id(), type);
        }
        return type;
    }
    public static <T extends CustomMapData> CustomMapData.Type<T> registerCustomMapSavedData(ResourceLocation id, Function<CompoundTag, T> factory) {
        return registerCustomMapSavedData(new CustomMapData.Type<>(id, factory));
    }


    //map markers

    public static final ResourceKey<Registry<MapDecorationType<?, ?>>> KEY = ResourceKey.createRegistryKey(Moonlight.res("map_markers"));
    public static final ResourceLocation GENERIC_STRUCTURE_ID = Moonlight.res("generic_structure");
    private static final Map<ResourceLocation, CustomDecorationType<?, ?>> CODE_TYPES_FACTORIES = new HashMap<>();

    public static MapDecorationType<?, ?> getGenericStructure() {
        return get(GENERIC_STRUCTURE_ID);
    }

    /**
     * Call before mod setup. Register a code defined map marker type. You will still need to add a related json file
     */
    public static <T extends CustomDecorationType<?, ?>> void registerCustomType(T decorationType) {
        CODE_TYPES_FACTORIES.put(decorationType.getCustomFactoryID(), decorationType);
    }

    public static CustomDecorationType<?, ?> getCustomType(ResourceLocation resourceLocation) {
        return Objects.requireNonNull(CODE_TYPES_FACTORIES.get(resourceLocation), "No map decoration type with id: " + resourceLocation);
    }

    public static MapDecorationType<?, ?> getAssociatedType(Holder<Structure> structure) {
        for (var v : getValues()) {
            Optional<HolderSet<Structure>> associatedStructure = v.getAssociatedStructure();
            if (associatedStructure.isPresent() && associatedStructure.get().contains(structure)) {
                return v;
            }
        }
        return getGenericStructure();
    }

    @ApiStatus.Internal
    @ExpectPlatform
    public static void init() {
        throw new AssertionError();
    }

    public static Registry<MapDecorationType<?, ?>> hackyGetRegistry() {
        return Utils.hackyGetRegistryAccess().registryOrThrow(KEY);
    }

    public static Registry<MapDecorationType<?, ?>> getRegistry(RegistryAccess registryAccess) {
        return registryAccess.registryOrThrow(KEY);
    }

    public static Collection<MapDecorationType<?, ?>> getValues() {
        return hackyGetRegistry().stream().toList();
    }

    public static Set<Map.Entry<ResourceKey<MapDecorationType<?, ?>>, MapDecorationType<?, ?>>> getEntries() {
        return hackyGetRegistry().entrySet();
    }

    @Nullable
    public static MapDecorationType<? extends CustomMapDecoration, ?> get(String id) {
        return get(new ResourceLocation(id));
    }

    public static MapDecorationType<?, ?> get(ResourceLocation id) {
        var reg = hackyGetRegistry();
        var r = reg.get(id);
        if (r == null) return reg.get(GENERIC_STRUCTURE_ID);
        return r;
    }

    public static Optional<MapDecorationType<?, ?>> getOptional(ResourceLocation id) {
        return hackyGetRegistry().getOptional(id);
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
