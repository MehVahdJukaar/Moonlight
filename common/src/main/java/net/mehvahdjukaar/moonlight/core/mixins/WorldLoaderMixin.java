package net.mehvahdjukaar.moonlight.core.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import net.mehvahdjukaar.moonlight.api.misc.IProgressTracker;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.misc.FilteredResManager;
import net.mehvahdjukaar.moonlight.core.misc.ReloadInstanceWrapper;
import net.minecraft.server.WorldLoader;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

//for client
@Mixin(WorldLoader.class)
public abstract class WorldLoaderMixin {

    //since we are on server thread we can use old method of doing stuff since its non blocking
    //must use this way since other one happens too late as its after datapack registry load.
    // we need here for DATAPACK REGISTRIES

    //should fire right before add reload listener, before packs are reloaded and listeners called
    @Inject(method = "load", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/RegistryLayer;createRegistryAccess()Lnet/minecraft/core/LayeredRegistryAccess;",
            shift = At.Shift.BEFORE))
    private static void moonlight$serverDynamicPackEarlyReload(WorldLoader.InitConfig initConfig,
                                                               WorldLoader.WorldDataSupplier worldDataSupplier,
                                                               WorldLoader.ResultFactory resultFactory,
                                                               Executor backgroundExecutor, Executor gameExecutor,
                                                               CallbackInfoReturnable<CompletableFuture> cir,
                                                               @Local CloseableResourceManager manager) {
        //fires on world load or on /reload
        //token to assure that modded resources are included
        if (!( (manager) instanceof FilteredResManager) &&
                manager.getResource(Moonlight.res("moonlight/token.json")).isPresent()) { //this assumes that it includes all pack including all mod assets
            //one would think that this would be fool proof. Well check again, some mod like to re create this resource manager during block load! All modded resources included aswell
            //so to be EXTRA safe we check if registry phase is over
            if (!PlatHelper.isInitializing()) {
                //reload dynamic packs before reloading data packs
                ReloadInstanceWrapper.executeEarlyReloadBlocking(PackType.SERVER_DATA, manager,
                        IProgressTracker.createTree(1));
            }
        }
    }
}

