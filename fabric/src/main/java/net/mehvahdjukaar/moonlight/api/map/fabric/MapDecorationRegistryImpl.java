package net.mehvahdjukaar.moonlight.api.map.fabric;

import net.mehvahdjukaar.moonlight.api.map.MapDecorationRegistry;
import net.mehvahdjukaar.moonlight.api.map.type.MapDecorationType;
import net.mehvahdjukaar.moonlight.api.map.type.JsonDecorationType;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;

import java.util.Optional;

public class MapDecorationRegistryImpl {
    //rest done by mixin

    public static void init() {
    }

    public static void bootstrap(BootstapContext<MapDecorationType<?, ?>> bootstapContext) {
        bootstapContext.register(ResourceKey.create(MapDecorationRegistry.KEY,
                MapDecorationRegistry.GENERIC_STRUCTURE_ID), new JsonDecorationType(Optional.empty()));

    }
}
