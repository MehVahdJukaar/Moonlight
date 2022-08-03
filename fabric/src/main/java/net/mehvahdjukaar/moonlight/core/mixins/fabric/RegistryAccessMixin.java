package net.mehvahdjukaar.moonlight.core.mixins.fabric;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.fabric.SoftFluidRegistryImpl;
import net.mehvahdjukaar.moonlight.api.map.MapDecorationRegistry;
import net.mehvahdjukaar.moonlight.api.map.fabric.MapDecorationRegistryImpl;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RegistryAccess.class)
public interface RegistryAccessMixin {

    @Inject(method = "put(Lcom/google/common/collect/ImmutableMap$Builder;Lnet/minecraft/resources/ResourceKey;Lcom/mojang/serialization/Codec;)V",
            at = @At("TAIL"))
    private static <E> void put(ImmutableMap.Builder<ResourceKey<? extends Registry<?>>, RegistryAccess.RegistryData<?>> builder,
                                ResourceKey<? extends Registry<E>> registryKey, Codec<E> elementCodec, CallbackInfo ci) {

        if (registryKey.location() == Registry.FLAT_LEVEL_GENERATOR_PRESET_REGISTRY.location()) {
            builder.put(SoftFluidRegistryImpl.KEY, new RegistryAccess.RegistryData<>(
                    SoftFluidRegistryImpl.KEY, SoftFluid.CODEC, SoftFluid.CODEC));

            builder.put(MapDecorationRegistryImpl.KEY, new RegistryAccess.RegistryData<>(
                    MapDecorationRegistryImpl.KEY, MapDecorationRegistry.TYPE_CODEC, MapDecorationRegistry.TYPE_CODEC));
        }

    }


}