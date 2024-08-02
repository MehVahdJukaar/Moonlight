package net.mehvahdjukaar.moonlight.core.map.fabric;


import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.mehvahdjukaar.moonlight.api.map.decoration.MLMapDecorationType;
import net.mehvahdjukaar.moonlight.core.map.MapDataInternal;

public class MapDataInternalImpl {
    //rest done by mixin

    public static void init() {
        DynamicRegistries.registerSynced(MapDataInternal.KEY, MLMapDecorationType.DIRECT_CODEC, MLMapDecorationType.DIRECT_NETWORK_CODEC, DynamicRegistries.SyncOption.SKIP_WHEN_EMPTY);
    }


}
