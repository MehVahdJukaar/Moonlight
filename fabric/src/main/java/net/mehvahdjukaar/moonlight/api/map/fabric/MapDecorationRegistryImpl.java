package net.mehvahdjukaar.moonlight.api.map.fabric;

import net.mehvahdjukaar.moonlight.api.map.MapDecorationRegistry;
import net.mehvahdjukaar.moonlight.api.map.type.MapDecorationType;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

public class MapDecorationRegistryImpl {
    //rest done by mixin
    public static final ResourceKey<Registry<MapDecorationType<?,?>>> KEY = ResourceKey.createRegistryKey(
            Moonlight.res("moonlight/map_markers"));

    public static Registry<MapDecorationType<?,?>> REG;

    public static void init() {
    }

    public static ResourceKey<Registry<MapDecorationType<?, ?>>> getRegistryKey() {
        return KEY;
    }

    public static void registerInternal(ResourceLocation id, Supplier<MapDecorationType<?, ?>> markerType) {
        BuiltinRegistries.register(REG, ResourceKey.create(KEY, id), markerType.get()); //hacky
    }

    //get value and bootstrap
    public static Holder<? extends MapDecorationType<?, ?>> getDefaultValue(Registry<MapDecorationType<?, ?>> reg) {
        //called by mixin so its too early to register builtin stuff here
        return BuiltinRegistries.register(reg, ResourceKey.create(KEY, Moonlight.res("generic_structure")), MapDecorationRegistry.GENERIC_STRUCTURE_TYPE);
    }
}
