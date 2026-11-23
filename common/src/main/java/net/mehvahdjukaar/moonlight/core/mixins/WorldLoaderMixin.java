package net.mehvahdjukaar.moonlight.core.mixins;

import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.moonlight.api.misc.IProgressTracker;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.misc.FilteredResManager;
import net.mehvahdjukaar.moonlight.core.misc.ReloadInstanceWrapper;
import net.minecraft.server.WorldLoader;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.world.level.WorldDataConfiguration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.function.Supplier;

//for client
@Mixin(WorldLoader.class)
public abstract class WorldLoaderMixin {

    //since we are on server thread we can use old method of doing stuff since its non blocking
    //must use this way since other one happens too late as its after datapack registry load.
    // we need here for DATAPACK REGISTRIES

    //fires right after the resource manager is made, before WorldLoader loads the datapack registries
    @ModifyArg(method = "load", index = 0, at = @At(value = "INVOKE",
            target = "Ljava/util/concurrent/CompletableFuture;supplyAsync(Ljava/util/function/Supplier;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"))
    private static Supplier<Pair<WorldDataConfiguration, CloseableResourceManager>> moonlight$serverDynamicPackEarlyReload(
            Supplier<Pair<WorldDataConfiguration, CloseableResourceManager>> original) {
        return () -> {
            var packsAndManager = original.get();
            CloseableResourceManager manager = packsAndManager.getSecond();
            //token to assure that modded resources are included
            if (!(manager instanceof FilteredResManager) &&
                    manager.getResource(Moonlight.res("moonlight/token.json")).isPresent()) { //this assumes that it includes all pack including all mod assets
                //one would think that this would be fool proof. Well check again, some mod like to re create this resource manager during block load! All modded resources included aswell
                //so to be EXTRA safe we check if registry phase is over
                if (!PlatHelper.isInitializing()) {
                    //reload dynamic packs before reloading data packs
                    ReloadInstanceWrapper.executeEarlyReloadBlocking(PackType.SERVER_DATA, manager,
                            IProgressTracker.createTree(1));
                }
            }
            return packsAndManager;
        };
    }
}
