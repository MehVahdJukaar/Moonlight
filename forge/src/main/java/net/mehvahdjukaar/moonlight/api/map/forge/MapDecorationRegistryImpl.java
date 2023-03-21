package net.mehvahdjukaar.moonlight.api.map.forge;

import net.mehvahdjukaar.moonlight.api.map.MapDecorationRegistry;
import net.mehvahdjukaar.moonlight.api.map.type.MapDecorationType;
import net.mehvahdjukaar.moonlight.api.map.type.SimpleDecorationType;
import net.mehvahdjukaar.moonlight.api.platform.forge.RegHelperImpl;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.*;

import java.util.Optional;
import java.util.function.Supplier;

import static net.mehvahdjukaar.moonlight.api.map.MapDecorationRegistry.KEY;

public class MapDecorationRegistryImpl {

    public static final DeferredRegister<MapDecorationType<?, ?>> DEFERRED_REGISTER = DeferredRegister.create(KEY, KEY.location().getNamespace());
    public static final Supplier<IForgeRegistry<MapDecorationType<?, ?>>> MAP_MARKERS = DEFERRED_REGISTER.makeRegistry(() ->
            new RegistryBuilder<MapDecorationType<?, ?>>()
                    .setDefaultKey(MapDecorationRegistry.GENERIC_STRUCTURE_ID)
                    .allowModification()
                    .disableSaving());

    @SubscribeEvent
    public static void registerDataPackRegistry(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(KEY, MapDecorationRegistry.TYPE_CODEC, MapDecorationRegistry.TYPE_CODEC);
    }

    private static final RegistryObject<MapDecorationType<?, ?>> GENERIC_STRUCTURE = DEFERRED_REGISTER
            .register(MapDecorationRegistry.GENERIC_STRUCTURE_ID.getPath(), () -> new SimpleDecorationType(Optional.empty()));


    public static void init() {
        var bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(MapDecorationRegistryImpl::registerDataPackRegistry);
        DEFERRED_REGISTER.register(bus);
    }

    public static void registerInternal(ResourceLocation id, Supplier<MapDecorationType<?, ?>> markerType) {
        RegHelperImpl.register(id, markerType, KEY);
    }
}