package net.mehvahdjukaar.selene.map;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.mehvahdjukaar.selene.Moonlight;
import net.mehvahdjukaar.selene.map.client.MapDecorationRenderHandler;
import net.mehvahdjukaar.selene.map.markers.MapBlockMarker;
import net.mehvahdjukaar.selene.map.type.CustomDecorationType;
import net.mehvahdjukaar.selene.map.type.IMapDecorationType;
import net.mehvahdjukaar.selene.map.type.SimpleDecorationType;
import net.minecraft.core.BlockPos;
import net.minecraft.data.models.blockstates.PropertyDispatch;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class MapDecorationRegistry extends SimpleJsonResourceReloadListener {

    //data driven stuff
    private final List<SimpleDecorationType> dynamicTypes = new ArrayList<>();

    public MapDecorationRegistry() {
        super(new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create(), "map_markers");
    }

    //for simple markers
    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsons, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        //remove data driven types
        dynamicTypes.forEach(t -> DECORATION_TYPES.remove(t.getId()));
        dynamicTypes.clear();
        for (var j : jsons.entrySet()) {
            Optional<SimpleDecorationType> result = SimpleDecorationType.CODEC.parse(JsonOps.INSTANCE, j.getValue())
                    .resultOrPartial(e -> Moonlight.LOGGER.error("Failed to parse Map Decoration JSON object for {} : {}", j.getKey(), e));

            if (result.isPresent()) {
                dynamicTypes.add(result.get());
                register(result.get());
            }
        }
    }

    public void acceptClientTypes(Collection<SimpleDecorationType> serverTypes) {
        for (var old : dynamicTypes) {
            MapDecorationRenderHandler.unbindRenderer(old);
            //for client. this map is the same
            DECORATION_TYPES.remove(old.getId());
        }
        dynamicTypes.clear();
        for (var type : serverTypes) {
            dynamicTypes.add(type);
            register(type);
            MapDecorationRenderHandler.bindSimpleRenderer(type);
        }
    }

    /**
     * Data driven markers id
     */
    public Collection<SimpleDecorationType> getTypes() {
        return dynamicTypes;
    }

    public static final MapDecorationRegistry DATA_DRIVEN_REGISTRY = new MapDecorationRegistry();


    //static stuff

    public static final Map<ResourceLocation, CustomDataHolder<?>> CUSTOM_MAP_DATA_TYPES = new HashMap<>();

    private static final Map<ResourceLocation, IMapDecorationType<? extends CustomMapDecoration, ?>> DECORATION_TYPES = new HashMap<>();

    //public static final IMapDecorationType<CustomMapDecoration, ?> GENERIC_STRUCTURE_TYPE = makeSimpleType(Selene.MOD_ID, "generic_structure");

    /**
     * registers a decoration type. use register simple for decoration that doesn't need a world marker
     *
     * @param newType new decoration type
     */
    public static <T extends CustomMapDecoration> void register(IMapDecorationType<T, ?> newType) {
        ResourceLocation id = newType.getId();
        if (DECORATION_TYPES.containsKey(id)) {
            DECORATION_TYPES.put(id, newType);
            Moonlight.LOGGER.error("Duplicate Map Marker registration: {}. This might be unwanted", id);
            //throw new IllegalArgumentException("Duplicate map decoration registration " + id);
        } else {
            DECORATION_TYPES.put(id, newType);
        }
    }

    /**
     * creates & registers a simple decoration with no associated world marker.<br>
     * useful for exploration maps
     *
     * @param modId mod id
     * @param name  decoration name
     */
    public static void registerSimple(String modId, String name) {
        register(makeSimpleType(modId, name));
    }

    /**
     * creates a simple decoration type with no associated marker
     *
     * @param modId mod id
     * @param name  decoration name
     * @return newly created decoration type
     */
    public static CustomDecorationType<CustomMapDecoration, ?> makeSimpleType(String modId, String name) {
        return new CustomDecorationType<>(new ResourceLocation(modId, name), CustomMapDecoration::new);
    }

    @Nullable
    public static IMapDecorationType<?, ?> get(ResourceLocation id) {
        return DECORATION_TYPES.get(id);
    }

    @Nullable
    public static IMapDecorationType<? extends CustomMapDecoration, ?> get(String id) {
        return get(new ResourceLocation(id));
    }

    @Nullable
    @ApiStatus.Internal
    public static MapBlockMarker<?> readWorldMarker(CompoundTag compound) {
        for (ResourceLocation id : DECORATION_TYPES.keySet()) {
            String s = id.toString();
            if (compound.contains(s)) {
                return DECORATION_TYPES.get(id).loadMarkerFromNBT(compound.getCompound(s));
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
        for (IMapDecorationType<?, ?> type : DECORATION_TYPES.values()) {
            MapBlockMarker<?> c = type.getWorldMarkerFromWorld(reader, pos);
            if (c != null) list.add(c);
        }
        return list;
    }

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
        if (CUSTOM_MAP_DATA_TYPES.containsKey("name")) {
            throw new IllegalArgumentException("Duplicate custom map data registration " + name);
        } else {
            CUSTOM_MAP_DATA_TYPES.put(name, new CustomDataHolder<>(name, load, save, onItemUpdate, onItemTooltip));
        }
    }


}
