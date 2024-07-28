package net.mehvahdjukaar.moonlight.core.map;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.moonlight.api.map.CustomMapData;
import net.mehvahdjukaar.moonlight.api.map.type.MLMapDecoration;
import net.mehvahdjukaar.moonlight.api.map.markers.MapBlockMarker;
import net.mehvahdjukaar.moonlight.api.map.type.MLSpecialMapDecorationType;
import net.mehvahdjukaar.moonlight.api.map.type.MlMapDecorationType;
import net.mehvahdjukaar.moonlight.api.misc.TriFunction;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
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

    //data holder
    @ApiStatus.Internal
    public static final Map<ResourceLocation, CustomMapData.Type<?>> CUSTOM_MAP_DATA_TYPES = new LinkedHashMap<>();

    /**
     * Registers a custom data type to be stored in map data. Type will provide its onw data implementation
     **/
    public static <T extends CustomMapData<?>> CustomMapData.Type<T> registerCustomMapSavedData(CustomMapData.Type<T> type) {
        if (CUSTOM_MAP_DATA_TYPES.containsKey(type.id())) {
            throw new IllegalArgumentException("Duplicate custom map data registration " + type.id());
        } else {
            CUSTOM_MAP_DATA_TYPES.put(type.id(), type);
        }
        return type;
    }

    //map markers

    public static final ResourceKey<Registry<MlMapDecorationType<?, ?>>> KEY = ResourceKey.createRegistryKey(Moonlight.res("map_markers"));
    public static final ResourceLocation GENERIC_STRUCTURE_ID = Moonlight.res("generic_structure");
    private static final BiMap<ResourceLocation, Supplier<MLSpecialMapDecorationType<?, ?>>> CODE_TYPES_FACTORIES = HashBiMap.create();

    public static MlMapDecorationType<?, ?> getGenericStructure() {
        return get(GENERIC_STRUCTURE_ID);
    }

    /**
     * Call before mod setup. Register a code defined map marker type. You will still need to add a related json file
     */
    public static void registerCustomType(ResourceLocation id, Supplier<MLSpecialMapDecorationType<?, ?>> decorationType) {
        CODE_TYPES_FACTORIES.put(id, decorationType);
    }

    //TODO: redo in 1.20.6
    //maybe rename the decoration and decortion type to MapDecorationInstance and MapDecorationType to MapDecoration and the factory to type
    public static MLSpecialMapDecorationType<?, ?> createCustomType(ResourceLocation factoryID) {
        var factory = Objects.requireNonNull(CODE_TYPES_FACTORIES.get(factoryID),
                "No map decoration type with id: " + factoryID);
        var t = factory.get();
        //TODO: improve
        t.factoryId = factoryID;
        return t;
    }

    public static MlMapDecorationType<?, ?> getAssociatedType(Holder<Structure> structure) {
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

    public static Registry<MlMapDecorationType<?, ?>> hackyGetRegistry() {
        return Utils.hackyGetRegistryAccess().registryOrThrow(KEY);
    }

    public static Registry<MlMapDecorationType<?, ?>> getRegistry(RegistryAccess registryAccess) {
        return registryAccess.registryOrThrow(KEY);
    }

    public static Collection<MlMapDecorationType<?, ?>> getValues() {
        return hackyGetRegistry().stream().toList();
    }

    public static Set<Map.Entry<ResourceKey<MlMapDecorationType<?, ?>>, MlMapDecorationType<?, ?>>> getEntries() {
        return hackyGetRegistry().entrySet();
    }

    @Nullable
    public static MlMapDecorationType<? extends MLMapDecoration, ?> get(String id) {
        return get(ResourceLocation.parse(id));
    }

    public static MlMapDecorationType<?, ?> get(ResourceLocation id) {
        var reg = hackyGetRegistry();
        var r = reg.get(id);
        if (r == null) return reg.get(GENERIC_STRUCTURE_ID);
        return r;
    }

    public static Optional<MlMapDecorationType<?, ?>> getOptional(ResourceLocation id) {
        return hackyGetRegistry().getOptional(id);
    }

    public static Set<MapBlockMarker<?>> getDynamicServer(Player player, MapId mapId, MapItemSavedData data) {
        Set<MapBlockMarker<?>> dynamic = new HashSet<>();
        for (var v : DYNAMIC_SERVER) {
            dynamic.addAll(v.apply(player, mapId, data));
        }
        return dynamic;
    }

    public static Set<MapBlockMarker<?>> getDynamicClient(MapId mapId, MapItemSavedData data) {
        Set<MapBlockMarker<?>> dynamic = new HashSet<>();
        for (var v : DYNAMIC_CLIENT) {
            dynamic.addAll(v.apply(mapId, data));
        }
        return dynamic;
    }


    @Nullable
    public static MapBlockMarker<?> readWorldMarker(CompoundTag compound) {
        for (var id : compound.getAllKeys()) {
            return get(ResourceLocation.parse(id)).load(compound.getCompound(id));
        }
        return null;
    }

    /**
     * returns a list of suitable world markers associated to a position. called by mixin code
     *
     * @param reader world
     * @param pos    world position
     * @return markers found, empty list if none found
     */
    public static List<MapBlockMarker<?>> getMarkersFromWorld(BlockGetter reader, BlockPos pos) {
        List<MapBlockMarker<?>> list = new ArrayList<>();
        for (MlMapDecorationType<?, ?> type : getValues()) {
            MapBlockMarker<?> c = type.getWorldMarkerFromWorld(reader, pos);
            if (c != null) list.add(c);
        }
        return list;
    }

    //dynamic markers

    private static final List<TriFunction<Player, MapId, MapItemSavedData, Set<MapBlockMarker<?>>>> DYNAMIC_SERVER = new ArrayList<>();
    private static final List<BiFunction<MapId, MapItemSavedData, Set<MapBlockMarker<?>>>> DYNAMIC_CLIENT = new ArrayList<>();


    public static void addDynamicClientMarkersEvent(BiFunction<MapId, MapItemSavedData, Set<MapBlockMarker<?>>> event) {
        DYNAMIC_CLIENT.add(event);
    }

    public static void addDynamicServerMarkersEvent(TriFunction<Player, MapId, MapItemSavedData, Set<MapBlockMarker<?>>> event) {
        DYNAMIC_SERVER.add(event);
    }

}
