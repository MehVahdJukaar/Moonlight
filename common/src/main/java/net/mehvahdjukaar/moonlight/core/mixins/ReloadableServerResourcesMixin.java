package net.mehvahdjukaar.moonlight.core.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.core.misc.FilteredResManager;
import net.mehvahdjukaar.moonlight.core.misc.MLEarlyReloadTask;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Unit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.concurrent.CompletableFuture;

@Mixin(ReloadableServerResources.class)
public abstract class ReloadableServerResourcesMixin {

    //should fire right before add reload listener, before packs are reloaded and listeners called
    //should fire right before add reload listener, before packs are reloaded and listeners called
    @ModifyArg(method = "loadResources", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/resources/SimpleReloadInstance;create(Lnet/minecraft/server/packs/resources/ResourceManager;Ljava/util/List;Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;Ljava/util/concurrent/CompletableFuture;Z)Lnet/minecraft/server/packs/resources/ReloadInstance;"))
    private static CompletableFuture<Unit> moonlight$dynamicPackEarlyReload(CompletableFuture<Unit> awaitedFor,
                                                                            @Local(argsOnly = true) ResourceManager resourceManager  ) {
        //fires on world load or on /reload
        //token to assure that modded resources are included
        if (!(resourceManager instanceof FilteredResManager) &&
                 (resourceManager instanceof MultiPackResourceManager mp) &&
                resourceManager.getResource(new ResourceLocation("moonlight:moonlight/token.json")).isPresent()) { //this assumes that it includes all pack including all mod assets
            //one would think that this would be fool proof. Well check again, some mod like to re create this resource manager during block load! All modded resources included aswell
            //so to be EXTRA safe we check if registry phase is over
            if (!PlatHelper.isInitializing()) {
                //hack.we assume its of server type
                return MLEarlyReloadTask.run(PackType.SERVER_DATA, resourceManager).thenCompose((unit) -> awaitedFor);
            }
        }
        return awaitedFor;
    }
}
