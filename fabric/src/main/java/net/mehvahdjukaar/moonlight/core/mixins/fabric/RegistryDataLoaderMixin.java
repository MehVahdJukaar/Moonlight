package net.mehvahdjukaar.moonlight.core.mixins.fabric;

import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.moonlight.api.map.MapDecorationRegistry;
import net.minecraft.resources.RegistryDataLoader;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(RegistryDataLoader.class)
public abstract class RegistryDataLoaderMixin {
    @Final
    @Shadow
    @Mutable
    public static List<RegistryDataLoader.RegistryData<?>> WORLDGEN_REGISTRIES;


    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void addMoonlightRegistries(CallbackInfo ci){
        WORLDGEN_REGISTRIES = new ArrayList<>(WORLDGEN_REGISTRIES);
        WORLDGEN_REGISTRIES.add(new RegistryDataLoader.RegistryData<>(SoftFluidRegistry.KEY, SoftFluid.CODEC));
        WORLDGEN_REGISTRIES.add(new RegistryDataLoader.RegistryData<>(MapDecorationRegistry.KEY, MapDecorationRegistry.TYPE_CODEC));
    }
}
