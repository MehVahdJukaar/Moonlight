package net.mehvahdjukaar.moonlight.core.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
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

//for client
@Mixin(WorldLoader.PackConfig.class)
public abstract class PackConfigMixin {

    //since we are on server thread we can use old method of doing stuff since its non blocking
    //must use this way since other one happens too late as its after datapack registry load.
    // we need here for DATAPACK REGISTRIES

    //fires right after the resource manager is made, before WorldLoader loads the datapack registries
    @ModifyReturnValue(method = "createResourceManager", at = @At("RETURN"))
    private Pair<WorldDataConfiguration, CloseableResourceManager> moonlight$serverDynamicPackEarlyReload(
            Pair<WorldDataConfiguration, CloseableResourceManager> original) {
        CloseableResourceManager manager = original.getSecond();
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
        return original;
    }
}
