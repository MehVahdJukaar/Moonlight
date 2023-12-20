package net.mehvahdjukaar.moonlight.core.map.forge;

import net.mehvahdjukaar.moonlight.core.map.MapDataInternal;
import net.mehvahdjukaar.moonlight.forge.MoonlightForge;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;


public class MapDataInternalImpl {

    @SubscribeEvent
    public static void registerDataPackRegistry(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(MapDataInternal.KEY, MapDataInternal.CODEC, MapDataInternal.NETWORK_CODEC);
    }

    public static void init() {
        var bus = MoonlightForge.getCurrentModBus();
        bus.register(MapDataInternalImpl.class);
    }
}