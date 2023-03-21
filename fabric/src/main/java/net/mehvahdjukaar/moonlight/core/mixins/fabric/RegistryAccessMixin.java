package net.mehvahdjukaar.moonlight.core.mixins.fabric;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.moonlight.api.map.MapDecorationRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistrySynchronization;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RegistrySynchronization.class)
public abstract class RegistryAccessMixin {

    @Shadow
    private static <E> void put(ImmutableMap.Builder<ResourceKey<? extends Registry<?>>, RegistrySynchronization.NetworkedRegistryData<?>> builder, ResourceKey<? extends Registry<E>> key, Codec<E> networkCodec) {
    }

    @Unique
    private static boolean initializedMlReg = false;

    @Inject(method = "put(Lcom/google/common/collect/ImmutableMap$Builder;Lnet/minecraft/resources/ResourceKey;Lcom/mojang/serialization/Codec;)V",
            at = @At("TAIL"))
    private static <E> void put(ImmutableMap.Builder<ResourceKey<? extends Registry<?>>, RegistrySynchronization.NetworkedRegistryData<?>> builder,
                                ResourceKey<? extends Registry<E>> key, Codec<E> networkCodec, CallbackInfo ci) {
        //who needs an event, nobody will use this but me anyway
        if (!initializedMlReg && key.registry().equals(Registries.DAMAGE_TYPE.registry())) {
            initializedMlReg = true;
            put(builder, SoftFluidRegistry.KEY, SoftFluid.CODEC);
            put(builder, MapDecorationRegistry.KEY, MapDecorationRegistry.TYPE_CODEC);
        }
    }


}