package net.mehvahdjukaar.moonlight.core.map.fabric;


import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.mehvahdjukaar.moonlight.core.map.MapDataInternal;

public class MapDataInternalImpl {
    //rest done by mixin

    public static void init() {
        DynamicRegistries.registerSynced(MapDataInternal.KEY, MapDataInternal.CODEC, MapDataInternal.NETWORK_CODEC, DynamicRegistries.SyncOption.SKIP_WHEN_EMPTY);
    }


}
