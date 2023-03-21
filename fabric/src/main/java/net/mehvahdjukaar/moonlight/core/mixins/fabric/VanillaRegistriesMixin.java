package net.mehvahdjukaar.moonlight.core.mixins.fabric;

import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.moonlight.api.fluids.fabric.SoftFluidRegistryImpl;
import net.mehvahdjukaar.moonlight.api.map.MapDecorationRegistry;
import net.mehvahdjukaar.moonlight.api.map.fabric.MapDecorationRegistryImpl;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.registries.VanillaRegistries;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VanillaRegistries.class)
public abstract class VanillaRegistriesMixin {

    @Unique
    private static boolean initializedMLReg = false;

    @Final
    @Shadow
    private static RegistrySetBuilder BUILDER;

    @Inject(method = "createLookup", at = @At(value = "HEAD", target = "Lnet/minecraft/data/BuiltInRegistries;registerSimple(Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/data/BuiltInRegistries$RegistryBootstrap;)Lnet/minecraft/core/Registry;",
            ordinal = 0))
    private static void addMoonlightRegistries(CallbackInfoReturnable<HolderLookup.Provider> cir) {
        if (!initializedMLReg) {
            BUILDER.add(SoftFluidRegistry.KEY, SoftFluidRegistryImpl::bootstrap);
            BUILDER.add(MapDecorationRegistry.KEY, MapDecorationRegistryImpl::bootstrap);
            initializedMLReg = true;
        }
    }
}
