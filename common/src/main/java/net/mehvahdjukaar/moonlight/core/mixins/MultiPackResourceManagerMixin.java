package net.mehvahdjukaar.moonlight.core.mixins;

import net.mehvahdjukaar.moonlight.api.events.EarlyPackReloadEvent;
import net.mehvahdjukaar.moonlight.api.events.MoonlightEventsHelper;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.resources.pack.DynamicResourcePack;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.misc.FilteredResManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.resources.Resource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;

@Mixin(MultiPackResourceManager.class)
public abstract class MultiPackResourceManagerMixin implements CloseableResourceManager {

    @Shadow
    public abstract Optional<Resource> getResource(ResourceLocation arg);

    //should fire right before add reload listener, before packs are reloaded and listeners called
    @Inject(method = "<init>", at = @At(value = "RETURN"))
    private void moonlight$dynamicPackEarlyReload(PackType type, List<PackResources> packs, CallbackInfo cir) {
        //fires on world load or on /reload
        //token to assure that modded resources are included
        if (!((Object)(this) instanceof FilteredResManager) &&
                Moonlight.CAN_EARLY_RELOAD_HACK.get() &&
                this.getResource(new ResourceLocation("moonlight:moonlight/token.json")).isPresent()) { //this assumes that it includes all pack including all mod assets
            //one would think that this would be fool proof. Well check again, some mod like to re create this resource manager during block load! All modded resources included aswell
            //so to be EXTRA safe we check if registry phase is over
            if (!PlatHelper.isInitializing()) {
                //reload dynamic packs before reloading data packs
                //first clear all packs. ugly I know
                DynamicResourcePack.clearBeforeReload(type);
                MoonlightEventsHelper.postEvent(new EarlyPackReloadEvent(packs, this, type), EarlyPackReloadEvent.class);
            }
        }
    }
}
