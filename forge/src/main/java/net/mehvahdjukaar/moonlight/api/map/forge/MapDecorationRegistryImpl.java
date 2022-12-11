package net.mehvahdjukaar.moonlight.api.map.forge;

import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.forge.SoftFluidRegistryImpl;
import net.mehvahdjukaar.moonlight.api.map.MapDecorationRegistry;
import net.mehvahdjukaar.moonlight.api.map.type.MapDecorationType;
import net.mehvahdjukaar.moonlight.api.map.type.SimpleDecorationType;
import net.mehvahdjukaar.moonlight.api.misc.EventCalled;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.api.platform.forge.RegHelperImpl;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.*;

import java.util.Optional;
import java.util.function.Supplier;

public class MapDecorationRegistryImpl {

    public static final ResourceKey<Registry<MapDecorationType<?, ?>>> KEY = ResourceKey.createRegistryKey(
            Moonlight.res("map_markers"));

    public static final DeferredRegister<MapDecorationType<?, ?>> DEFERRED_REGISTER = DeferredRegister.create(KEY, KEY.location().getNamespace());
    public static final Supplier<IForgeRegistry<MapDecorationType<?, ?>>> MAP_MARKERS = DEFERRED_REGISTER.makeRegistry(() ->
            new RegistryBuilder<MapDecorationType<?, ?>>()
                    .setDefaultKey(MapDecorationRegistry.GENERIC_STRUCTURE_ID)
                    .allowModification()
                    .disableSaving());

    private static final RegistryObject<MapDecorationType<?, ?>> GENERIC_STRUCTURE = DEFERRED_REGISTER
            .register(MapDecorationRegistry.GENERIC_STRUCTURE_ID.getPath(), () -> new SimpleDecorationType(Optional.empty()));


    public static void init() {
        var bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(MapDecorationRegistryImpl::registerDataPackRegistry);
        DEFERRED_REGISTER.register(bus);
    }

    public static ResourceKey<Registry<MapDecorationType<?, ?>>> getRegistryKey() {
        return KEY;
    }

    public static void registerInternal(ResourceLocation id, Supplier<MapDecorationType<?, ?>> markerType) {
        RegHelperImpl.register(id, markerType, KEY);
    }
    @EventCalled
    public static void registerDataPackRegistry(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(KEY, MapDecorationRegistry.TYPE_CODEC, MapDecorationRegistry.TYPE_CODEC);
    }
}
