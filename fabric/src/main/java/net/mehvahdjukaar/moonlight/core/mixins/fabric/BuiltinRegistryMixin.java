package net.mehvahdjukaar.moonlight.core.mixins.fabric;
/*
import net.fabricmc.fabric.api.event.registry.DynamicRegistrySetupCallback;
import net.fabricmc.fabric.impl.registry.sync.RegistryMapSerializer;
import net.fabricmc.fabric.impl.registry.sync.RegistrySyncManager;
import net.fabricmc.loader.impl.gui.FabricGuiEntry;
import net.mehvahdjukaar.moonlight.api.fluids.fabric.SoftFluidRegistryImpl;
import net.mehvahdjukaar.moonlight.api.map.fabric.MapDecorationRegistryImpl;
import net.mehvahdjukaar.moonlight.api.misc.DataObjectReference;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BuiltinRegistries.class)
public abstract class BuiltinRegistryMixin {

    @Inject(method = "<clinit>", at = @At(value = "INVOKE_ASSIGN", target ="Lnet/minecraft/data/BuiltinRegistries;registerSimple(Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/data/BuiltinRegistries$RegistryBootstrap;)Lnet/minecraft/core/Registry;",
    ordinal = 0))
    private static void registerAdditional(CallbackInfo ci){
        SoftFluidRegistryImpl.REG = BuiltinRegistries
                .registerSimple(SoftFluidRegistryImpl.KEY,SoftFluidRegistryImpl::getDefaultValue);
        MapDecorationRegistryImpl.REG = BuiltinRegistries
                .registerSimple(MapDecorationRegistryImpl.KEY,MapDecorationRegistryImpl::getDefaultValue);
    }
}*///TODO
