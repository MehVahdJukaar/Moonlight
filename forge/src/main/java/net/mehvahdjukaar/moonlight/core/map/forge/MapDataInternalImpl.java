package net.mehvahdjukaar.moonlight.core.map.forge;

import net.mehvahdjukaar.moonlight.core.map.MapDataInternal;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DataPackRegistryEvent;


public class MapDataInternalImpl {

    @SubscribeEvent
    public static void registerDataPackRegistry(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(MapDataInternal.KEY, MapDataInternal.CODEC, MapDataInternal.NETWORK_CODEC);
    }

    public static void init() {
        var bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.register(MapDataInternalImpl.class);
    }
}