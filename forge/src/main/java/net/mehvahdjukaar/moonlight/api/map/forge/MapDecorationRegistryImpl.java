package net.mehvahdjukaar.moonlight.api.map.forge;

import net.mehvahdjukaar.moonlight.api.map.MapDecorationRegistry;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DataPackRegistryEvent;

import static net.mehvahdjukaar.moonlight.api.map.MapDecorationRegistry.KEY;

public class MapDecorationRegistryImpl {

    @SubscribeEvent
    public static void registerDataPackRegistry(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(KEY, MapDecorationRegistry.CODEC, MapDecorationRegistry.NETROWK_CODEC);
    }

    public static void init() {
        var bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.register(MapDecorationRegistryImpl.class);
    }
}