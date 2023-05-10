package net.mehvahdjukaar.moonlight.api.map.fabric;

import net.mehvahdjukaar.moonlight.api.map.MapDecorationRegistry;
import net.mehvahdjukaar.moonlight.api.map.type.MapDecorationType;
import net.mehvahdjukaar.moonlight.api.map.type.SimpleDecorationType;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;
import java.util.function.Supplier;

public class MapDecorationRegistryImpl {
    //rest done by mixin

    public static void init() {
    }

    public static void bootstrap(BootstapContext<MapDecorationType<?, ?>> bootstapContext) {
        bootstapContext.register(ResourceKey.create(MapDecorationRegistry.KEY,
                MapDecorationRegistry.GENERIC_STRUCTURE_ID), new SimpleDecorationType(Optional.empty()));

    }
}
