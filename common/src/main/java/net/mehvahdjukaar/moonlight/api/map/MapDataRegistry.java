package net.mehvahdjukaar.moonlight.api.map;

import net.mehvahdjukaar.moonlight.api.map.decoration.MLMapMarker;
import net.mehvahdjukaar.moonlight.api.map.decoration.MLSpecialMapDecorationType;
import net.mehvahdjukaar.moonlight.api.map.decoration.MLMapDecorationType;
import net.mehvahdjukaar.moonlight.api.misc.TriFunction;
import net.mehvahdjukaar.moonlight.core.map.MapDataInternal;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class MapDataRegistry {

    public static final ResourceKey<Registry<MLMapDecorationType<?, ?>>> REGISTRY_KEY = MapDataInternal.KEY;

    /**
     * Registers a custom data type to be stored in map data. Type will provide its onw data implementation
     **/
    public static <T extends CustomMapData<?>> CustomMapData.Type<T> registerCustomMapSavedData(CustomMapData.Type<T> type) {
        return MapDataInternal.registerCustomMapSavedData(type);
    }

    public static <T extends CustomMapData<?>> CustomMapData.Type<T> registerCustomMapSavedData(ResourceLocation id, Supplier<T> factory) {
        return registerCustomMapSavedData(new CustomMapData.Type<>(id, factory));
    }

    public static MLMapDecorationType<?, ?> getDefaultType() {
        return MapDataInternal.getGenericStructure();
    }

    /**
     * Call before mod setup. Register a code defined map marker type. You will still need to add a related json file
     */
    //TODO: this is bad. rethink type stuff
    //we have instances of markers per map. these have a type which determines their type
    //each type is assigned to one and one only json file. essntally the type is what is parsed from json.
    //each type can intern have its own type.., the custom factory
    @Deprecated(forRemoval = true)
    public static <T extends MLSpecialMapDecorationType<?, ?>> T registerCustomType(T decorationType) {
         MapDataInternal.registerCustomType(decorationType.getCustomFactoryID(), ()->decorationType);
         return decorationType;
    }

    public static void registerCustomType(ResourceLocation factoryId, Supplier<MLSpecialMapDecorationType<?,?>> decorationTypeFactory) {
         MapDataInternal.registerCustomType(factoryId, decorationTypeFactory);
    }

    /**
     * Use to add non-permanent decoration like player icon, only visible to player holding the map.
     * Called by the client every time map marker would change.
     * This means that for moving ones you should manage this yourself with a client tick event
     *
     * @param event callback
     */
    public static void addDynamicClientMarkersEvent(BiFunction<MapId, MapItemSavedData, Set<MLMapMarker<?>>> event) {
        MapDataInternal.addDynamicClientMarkersEvent(event);
    }

    /**
     * Use to add non-permanent per-player decoration like player icon, only visible to player holding the map.
     * Called by server every couple minutes and synced to the client.
     * Whether these will be saved or not is up to the marker provided
     *
     * @param event callback
     */
    public static void addDynamicServerMarkersEvent(TriFunction<Player, MapId, MapItemSavedData, Set<MLMapMarker<?>>> event) {
        MapDataInternal.addDynamicServerMarkersEvent(event);
    }

    public static MLSpecialMapDecorationType<?, ?> getCustomType(ResourceLocation resourceLocation) {
        return MapDataInternal.createCustomType(resourceLocation);
    }

    public static MLMapDecorationType<?, ?> getAssociatedType(Holder<Structure> structure) {
        return MapDataInternal.getAssociatedType(structure);
    }

    public static Registry<MLMapDecorationType<?, ?>> getRegistry(RegistryAccess registryAccess) {
        return MapDataInternal.getRegistry(registryAccess);
    }

    public static MLMapDecorationType<?, ?> get(ResourceLocation id) {
        return MapDataInternal.get(id);
    }

    public static Optional<MLMapDecorationType<?, ?>> getOptional(ResourceLocation id) {
        return MapDataInternal.getOptional(id);
    }

}
