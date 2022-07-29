package net.mehvahdjukaar.moonlight.api.map.forge;

import net.mehvahdjukaar.moonlight.api.map.MapDecorationRegistry;
import net.mehvahdjukaar.moonlight.api.map.type.MapDecorationType;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class MapDecorationRegistryImpl {

    public static final ResourceKey<Registry<MapDecorationType<?, ?>>> KEY = ResourceKey.createRegistryKey(
            Moonlight.res("map_markers"));

    public static final DeferredRegister<MapDecorationType<?, ?>> DEFERRED_REGISTER = DeferredRegister.create(KEY, KEY.location().getNamespace());
    public static final Supplier<IForgeRegistry<MapDecorationType<?, ?>>> MAP_MARKERS = DEFERRED_REGISTER.makeRegistry(() ->
            new RegistryBuilder<MapDecorationType<?, ?>>()
                    .setDefaultKey(Moonlight.res("generic_structure"))
                    .dataPackRegistry(MapDecorationRegistry.TYPE_CODEC, MapDecorationRegistry.TYPE_CODEC)
                    .allowModification()
                    .disableSaving());

    private static final RegistryObject<MapDecorationType<?, ?>> GENERIC_STRUCTURE = DEFERRED_REGISTER
            .register("generic_structure", () -> MapDecorationRegistry.GENERIC_STRUCTURE_TYPE);


    public static void init() {
        var bus = FMLJavaModLoadingContext.get().getModEventBus();
        DEFERRED_REGISTER.register(bus);
    }

    public static ResourceKey<Registry<MapDecorationType<?, ?>>> getRegistryKey() {
        return KEY;
    }

    public static void registerInternal(ResourceLocation id, Supplier<MapDecorationType<?, ?>> markerType) {
        var reg = DEFERRED_REGISTER.register(id.getPath(), markerType);
    }

}
