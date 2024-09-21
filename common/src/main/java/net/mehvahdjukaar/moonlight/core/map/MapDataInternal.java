package net.mehvahdjukaar.moonlight.core.map;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.moonlight.api.map.CustomMapData;
import net.mehvahdjukaar.moonlight.api.map.decoration.MLMapDecoration;
import net.mehvahdjukaar.moonlight.api.map.decoration.MLMapDecorationType;
import net.mehvahdjukaar.moonlight.api.map.decoration.MLMapMarker;
import net.mehvahdjukaar.moonlight.api.map.decoration.MLSpecialMapDecorationType;
import net.mehvahdjukaar.moonlight.api.misc.MapRegistry;
import net.mehvahdjukaar.moonlight.api.misc.TriFunction;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.core.*;
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

    public static final MapRegistry<CustomMapData.Type<?, ?>> CUSTOM_MAP_DATA_TYPES = new MapRegistry<>("custom_map_data_types");

    /**
     * Registers a custom data type to be stored in map data. Type will provide its onw data implementation
     **/
    public static <P, T extends CustomMapData<?, P>> CustomMapData.Type<P, T> registerCustomMapSavedData(CustomMapData.Type<P, T> type) {
        if (CUSTOM_MAP_DATA_TYPES.containsKey(type.id())) {
            throw new IllegalArgumentException("Duplicate custom map data registration " + type.id());
        } else {
            CUSTOM_MAP_DATA_TYPES.register(type.id(), type);
        }
        return type;
    }

    //map markers

    public static final ResourceKey<Registry<MLMapDecorationType<?, ?>>> KEY = ResourceKey.createRegistryKey(Moonlight.res("map_marker"));
    public static final ResourceLocation GENERIC_STRUCTURE_ID = Moonlight.res("generic_structure");
    private static final MapRegistry<Supplier<MLSpecialMapDecorationType<?, ?>>> CODE_TYPES_FACTORIES = new MapRegistry<>("code_map_decoration_types_factories");

    public static MLMapDecorationType<?, ?> getGenericStructure() {
        return getOrDefault(GENERIC_STRUCTURE_ID);
    }

    /**
     * Call before mod setup. Register a code defined map marker type. You will still need to add a related json file
     */
    public static void registerCustomType(ResourceLocation id, Supplier<MLSpecialMapDecorationType<?, ?>> decorationType) {
        CODE_TYPES_FACTORIES.register(id, decorationType);
    }

    public static MLSpecialMapDecorationType<?, ?> createCustomType(ResourceLocation factoryID) {
        var factory = Objects.requireNonNull(CODE_TYPES_FACTORIES.getValue(factoryID),
                "No map decoration type with id: " + factoryID);
        return factory.get();
    }

    public static MLMapDecorationType<?, ?> getAssociatedType(Holder<Structure> structure) {
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

    public static Registry<MLMapDecorationType<?, ?>> hackyGetRegistry() {
        return Utils.hackyGetRegistryAccess().registryOrThrow(KEY);
    }

    public static Registry<MLMapDecorationType<?, ?>> getRegistry(RegistryAccess registryAccess) {
        return registryAccess.registryOrThrow(KEY);
    }

    public static Collection<MLMapDecorationType<?, ?>> getValues() {
        return hackyGetRegistry().stream().toList();
    }

    public static Set<Map.Entry<ResourceKey<MLMapDecorationType<?, ?>>, MLMapDecorationType<?, ?>>> getEntries() {
        return hackyGetRegistry().entrySet();
    }

    @Nullable
    public static MLMapDecorationType<? extends MLMapDecoration, ?> getOrDefault(String id) {
        return getOrDefault(ResourceLocation.parse(id));
    }

    public static MLMapDecorationType<?, ?> getOrDefault(ResourceLocation id) {
        var reg = hackyGetRegistry();
        var r = reg.get(id);
        if (r == null) return reg.get(GENERIC_STRUCTURE_ID);
        return r;
    }

    @Nullable
    public static Holder<MLMapDecorationType<?, ?>> getHolder(ResourceLocation id) {
        return hackyGetRegistry().getHolder(id).orElse(null);
    }

    public static Optional<MLMapDecorationType<?, ?>> getOptional(ResourceLocation id) {
        return hackyGetRegistry().getOptional(id);
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
    public static List<MLMapMarker<?>> getMarkersFromWorld(BlockGetter reader, BlockPos pos) {
        List<MLMapMarker<?>> list = new ArrayList<>();
        for (MLMapDecorationType<?, ?> type : getValues()) {
            MLMapMarker<?> c = type.createMarkerFromWorld(reader, pos);
            if (c != null) list.add(c);
        }
        return list;
    }

    //dynamic markers

    private static final List<TriFunction<Player, MapId, MapItemSavedData, Set<MLMapMarker<?>>>> DYNAMIC_SERVER = new ArrayList<>();
    private static final List<BiFunction<MapId, MapItemSavedData, Set<MLMapMarker<?>>>> DYNAMIC_CLIENT = new ArrayList<>();


    public static void addDynamicClientMarkersEvent(BiFunction<MapId, MapItemSavedData, Set<MLMapMarker<?>>> event) {
        DYNAMIC_CLIENT.add(event);
    }

    public static void addDynamicServerMarkersEvent(TriFunction<Player, MapId, MapItemSavedData, Set<MLMapMarker<?>>> event) {
        DYNAMIC_SERVER.add(event);
    }


}
