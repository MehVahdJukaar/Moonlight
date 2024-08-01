package net.mehvahdjukaar.moonlight.core.map.forge;

import net.mehvahdjukaar.moonlight.api.map.decoration.MLMapDecorationType;
import net.mehvahdjukaar.moonlight.core.map.MapDataInternal;
import net.mehvahdjukaar.moonlight.forge.MoonlightForge;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;


public class MapDataInternalImpl {

    @SubscribeEvent
    public static void registerDataPackRegistry(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(MapDataInternal.KEY, MLMapDecorationType.DIRECT_CODEC, MLMapDecorationType.DIRECT_NETWORK_CODEC);
    }

    public static void init() {
        var bus = MoonlightForge.getCurrentBus();
        bus.register(MapDataInternalImpl.class);
    }
}