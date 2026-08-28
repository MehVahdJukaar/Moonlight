package net.mehvahdjukaar.moonlight.core.map;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.mehvahdjukaar.moonlight.api.map.CustomMapData;
import net.mehvahdjukaar.moonlight.api.map.ExpandedMapData;
import net.mehvahdjukaar.moonlight.api.map.decoration.MLMapDecoration;
import net.mehvahdjukaar.moonlight.api.map.decoration.MLMapDecorationType;
import net.mehvahdjukaar.moonlight.api.map.decoration.MLMapMarker;
import net.mehvahdjukaar.moonlight.api.map.decoration.MLSpecialMapDecorationType;
import net.mehvahdjukaar.moonlight.api.misc.MapRegistry;
import net.mehvahdjukaar.moonlight.api.misc.TriFunction;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Supplier;

@ApiStatus.Internal
public class MapDataInternal {

    public static final Registry<CustomMapData.Type<?, ?>> CUSTOM_MAP_DATA_REGISTRY = RegHelper.registerRegistry(
            Moonlight.res("custom_map_data_types"), true);

    /**
     * Registers a custom data type to be stored in map data. Type will provide its onw data implementation
     **/
    public static <P, T extends CustomMapData<?, P>> CustomMapData.Type<P, T> registerCustomMapSavedData(CustomMapData.Type<P, T> type) {
        if (CUSTOM_MAP_DATA_REGISTRY.containsKey(type.id())) {
            throw new IllegalArgumentException("Duplicate custom map data registration " + type.id());
        } else {
            RegHelper.register(type.id(), () -> type, CUSTOM_MAP_DATA_REGISTRY.key());
        }
        return type;
    }

    //map markers

    public static final ResourceKey<Registry<MLMapDecorationType<?, ?>>> MAP_DECORATION_REGISTRY_KEY = ResourceKey.createRegistryKey(Moonlight.res("map_marker"));
    public static final Identifier GENERIC_STRUCTURE_ID = Moonlight.res("generic_structure");
    private static final MapRegistry<Supplier<MLSpecialMapDecorationType<?, ?>>> CODE_TYPES_FACTORIES = new MapRegistry<>("code_map_decoration_types_factories");

    /**
     * Call before mod setup. Register a code-defined map marker type. You will still need to add a related json file
     */
    public static void registerCustomType(Identifier id, Supplier<MLSpecialMapDecorationType<?, ?>> decorationType) {
        CODE_TYPES_FACTORIES.register(id, decorationType);
    }

    public static MLSpecialMapDecorationType<?, ?> createCustomType(Identifier factoryID) {
        var factory = Objects.requireNonNull(CODE_TYPES_FACTORIES.getValue(factoryID),
                "No map decoration type with id: " + factoryID);
        var specialType = factory.get();
        specialType.factoryID = factoryID;
        return specialType;
    }

    public static Holder<MLMapDecorationType<?, ?>> getDecorationFoStructure(Level level, Holder<Structure> structure) {
        Registry<MLMapDecorationType<?, ?>> reg = getMapDecorationRegistry(level.registryAccess());
        var matched = reg.listElements()
                .filter(
                        h -> h.value().getAssociatedStructure()
                                .map(s -> s.contains(structure))
                                .orElse(false)
                ).findFirst();

        return matched.orElseGet(() -> reg.get(GENERIC_STRUCTURE_ID).orElseThrow());
    }

    @ApiStatus.Internal
    public static void init() {
        //dumb.needed because this can be class loaded before init
        RegHelper.registerDataPackRegistry(MapDataInternal.MAP_DECORATION_REGISTRY_KEY,
                MLMapDecorationType.DIRECT_CODEC, MLMapDecorationType.DIRECT_CODEC);
    }

    public static Registry<CustomMapData.Type<?, ?>> getMapDataRegistry() {
        return CUSTOM_MAP_DATA_REGISTRY;
    }

    public static Registry<MLMapDecorationType<?, ?>> getMapDecorationRegistry(RegistryAccess registryAccess) {
        return registryAccess.lookupOrThrow(MAP_DECORATION_REGISTRY_KEY);
    }

    public static Set<MLMapMarker<?>> getDynamicServer(Player player, MapId mapId, MapItemSavedData data) {
        Set<MLMapMarker<?>> dynamic = new HashSet<>();
        for (var v : DYNAMIC_SERVER) {
            dynamic.addAll(v.apply(player, mapId, data));
        }
        return dynamic;
    }

    public static Set<MLMapMarker<?>> getDynamicClient(MapId mapId, MapItemSavedData data) {
        Set<MLMapMarker<?>> dynamic = new HashSet<>();
        for (var v : DYNAMIC_CLIENT) {
            dynamic.addAll(v.apply(mapId, data));
        }
        return dynamic;
    }

    /**
     * returns a list of suitable world markers associated to a position. called by mixin code
     *
     * @param reader world
     * @param pos    world position
     * @return markers found, empty list if none found
     */
    public static List<MLMapMarker<?>> getMarkersFromWorld(LevelAccessor reader, BlockPos pos) {
        List<MLMapMarker<?>> list = new ArrayList<>();
        for (var type : getMapDecorationRegistry(reader.registryAccess())) {
            MLMapMarker<?> c = type.createMarkerFromWorld(reader, pos);
            if (c != null) list.add(c);
        }
        return list;
    }

    //dynamic markers

    private static final List<TriFunction<Player, MapId, MapItemSavedData, Set<MLMapMarker<?>>>> DYNAMIC_SERVER = Collections.synchronizedList(new ArrayList<>());

    private static final List<BiFunction<MapId, MapItemSavedData, Set<MLMapMarker<?>>>> DYNAMIC_CLIENT = Collections.synchronizedList(new ArrayList<>());

    public static void addDynamicClientMarkersEvent(BiFunction<MapId, MapItemSavedData, Set<MLMapMarker<?>>> event) {
        DYNAMIC_CLIENT.add(event);
    }

    public static void addDynamicServerMarkersEvent(TriFunction<Player, MapId, MapItemSavedData, Set<MLMapMarker<?>>> event) {
        DYNAMIC_SERVER.add(event);
    }

    private static final String MARKERS_KEY = "customMarkers";

    /** Vanilla's codec with markers and custom data added as extra nbt keys. */
    public static final Codec<MapItemSavedData> EXPANDED_CODEC = expand(MapItemSavedData.CODEC);

    private static Codec<MapItemSavedData> expand(Codec<MapItemSavedData> vanilla) {
        return new Codec<>() {
            @Override
            public <T> DataResult<Pair<MapItemSavedData, T>> decode(DynamicOps<T> ops, T input) {
                return vanilla.decode(ops, input).map(pair -> {
                    if (ops.convertTo(NbtOps.INSTANCE, input) instanceof CompoundTag tag) {
                        loadCustomData(tag, pair.getFirst());
                    }
                    return pair;
                });
            }

            @Override
            public <T> DataResult<T> encode(MapItemSavedData input, DynamicOps<T> ops, T prefix) {
                return vanilla.encode(input, ops, prefix).flatMap(encoded -> {
                    CompoundTag extra = new CompoundTag();
                    saveCustomData(extra, input);
                    if (extra.isEmpty()) return DataResult.success(encoded);
                    T converted = NbtOps.INSTANCE.convertTo(ops, extra);
                    return ops.getMap(converted).flatMap(entries -> ops.mergeToMap(encoded, entries));
                });
            }
        };
    }

    private static void saveCustomData(CompoundTag tag, MapItemSavedData data) {
        if (!(data instanceof ExpandedMapData expanded)) return;
        HolderLookup.Provider registries = Utils.hackyGetRegistryAccess();

        var markers = expanded.ml$getCustomMarkers();
        if (!markers.isEmpty()) {
            MLMapMarker.assertCanSerialize(registries);
            RegistryOps<Tag> ops = registries.createSerializationContext(NbtOps.INSTANCE);
            ListTag list = new ListTag();
            for (MLMapMarker<?> marker : markers.values()) {
                if (marker.shouldSave()) {
                    list.add(MLMapMarker.CODEC.encodeStart(ops, marker).getOrThrow());
                }
            }
            if (!list.isEmpty()) tag.put(MARKERS_KEY, list);
        }
        expanded.ml$getCustomData().values().forEach(d -> d.save(tag, registries));
    }

    private static void loadCustomData(CompoundTag tag, MapItemSavedData data) {
        if (!(data instanceof ExpandedMapData expanded)) return;
        HolderLookup.Provider registries = Utils.hackyGetRegistryAccess();

        ListTag list = tag.getListOrEmpty(MARKERS_KEY);
        if (!list.isEmpty()) {
            MLMapMarker.assertCanSerialize(registries);
            RegistryOps<Tag> ops = registries.createSerializationContext(NbtOps.INSTANCE);
            for (Tag element : list) {
                MLMapMarker.CODEC.parse(ops, element)
                        .resultOrPartial(s -> Moonlight.LOGGER.warn("Failed to parse moonlight map marker: '{}'", s))
                        .ifPresent(marker -> {
                            expanded.ml$getCustomMarkers().put(marker.getMarkerUniqueId(), marker);
                            expanded.ml$addCustomMarker(marker);
                        });
            }
        }
        expanded.ml$getCustomData().values().forEach(d -> d.load(tag, registries));
    }

}
