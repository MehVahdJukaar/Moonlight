package net.mehvahdjukaar.moonlight.api.map.fabric;

import net.mehvahdjukaar.moonlight.api.fluids.FluidContainerList;
import net.mehvahdjukaar.moonlight.api.map.MapDecorationRegistry;
import net.mehvahdjukaar.moonlight.api.map.type.MapDecorationType;
import net.mehvahdjukaar.moonlight.api.map.type.SimpleDecorationType;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;
import java.util.function.Supplier;

public class MapDecorationRegistryImpl {
    //rest done by mixin
    public static final ResourceKey<Registry<MapDecorationType<?, ?>>> KEY = ResourceKey.createRegistryKey(
            Moonlight.res("moonlight/map_markers"));

    public static Registry<MapDecorationType<?, ?>> REG;

    public static void init() {
    }

    public static ResourceKey<Registry<MapDecorationType<?, ?>>> getRegistryKey() {
        return KEY;
    }

    public static void registerInternal(ResourceLocation id, Supplier<MapDecorationType<?, ?>> markerType) {
        //TODO: this could be what causes issues? its currently disabled
        RegHelper.registerAsync(id, markerType, REG); //register immediately

      //  BuiltinRegistries.register(REG, ResourceKey.create(KEY, id), markerType.get()); //hacky
    }

    //get value and bootstrap
    public static Holder<? extends MapDecorationType<?, ?>> getDefaultValue(Registry<MapDecorationType<?, ?>> reg) {
        //called by mixin, so It's too early to register builtin stuff here.tho I guess I could use registry queue
        return BuiltinRegistries.register(reg, ResourceKey.create(KEY, MapDecorationRegistry.GENERIC_STRUCTURE_ID),  new SimpleDecorationType(Optional.empty()));
    }
}
