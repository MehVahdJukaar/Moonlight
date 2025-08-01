package net.mehvahdjukaar.moonlight.core.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.core.misc.FilteredResManager;
import net.mehvahdjukaar.moonlight.core.misc.MLEarlyReloadTask;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.util.Unit;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Mixin(ReloadableResourceManager.class)
public abstract class ReloadableClientResourcesMixin {

    @Shadow
    @Final
    public PackType type;

    @Shadow private CloseableResourceManager resources;

    //should fire right before add reload listener, before packs are reloaded and listeners called
    @ModifyArg(method = "createReload", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/resources/SimpleReloadInstance;create(Lnet/minecraft/server/packs/resources/ResourceManager;Ljava/util/List;Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;Ljava/util/concurrent/CompletableFuture;Z)Lnet/minecraft/server/packs/resources/ReloadInstance;"))
    private CompletableFuture<Unit> moonlight$dynamicPackEarlyReload(CompletableFuture<Unit> awaitedFor) {
        //fires on world load or on /reload
        //token to assure that modded resources are included
        if (!(this.resources instanceof FilteredResManager) &&
                this.resources.getResource(new ResourceLocation("moonlight:moonlight/token.json")).isPresent()) { //this assumes that it includes all pack including all mod assets
            //one would think that this would be fool proof. Well check again, some mod like to re create this resource manager during block load! All modded resources included aswell
            //so to be EXTRA safe we check if registry phase is over
            if (!PlatHelper.isInitializing()) {
               return MLEarlyReloadTask.run(type, this.resources).thenCompose((unit) -> awaitedFor);
            }
        }
        return awaitedFor;
    }
}
