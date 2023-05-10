package net.mehvahdjukaar.moonlight.api.map.forge;

import net.mehvahdjukaar.moonlight.api.map.MapDecorationRegistry;
import net.mehvahdjukaar.moonlight.api.map.type.MapDecorationType;
import net.mehvahdjukaar.moonlight.api.misc.EventCalled;
import net.mehvahdjukaar.moonlight.api.platform.forge.RegHelperImpl;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DataPackRegistryEvent;

import java.util.function.Supplier;

import static net.mehvahdjukaar.moonlight.api.map.MapDecorationRegistry.KEY;

public class MapDecorationRegistryImpl {

    @SubscribeEvent
    public static void registerDataPackRegistry(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(KEY, MapDecorationRegistry.TYPE_CODEC, MapDecorationRegistry.TYPE_CODEC);
    }

    public static void init() {
        var bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.register(MapDecorationRegistryImpl.class);
    }
}