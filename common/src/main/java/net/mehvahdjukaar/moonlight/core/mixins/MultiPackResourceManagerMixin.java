package net.mehvahdjukaar.moonlight.core.mixins;

import net.mehvahdjukaar.moonlight.api.platform.event.EventHelper;
import net.mehvahdjukaar.moonlight.api.events.IEarlyPackReloadEvent;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(MultiPackResourceManager.class)
public abstract class MultiPackResourceManagerMixin implements CloseableResourceManager {

    @Shadow @Final private List<PackResources> packs;

    //should fire right before add reload listener, before packs are reloaded and listeners called
    @Inject(method = "<init>", at = @At(value = "RETURN"))
    private void dynamicPackEarlyReload(PackType type, List<PackResources> packs, CallbackInfo cir) {
        //fires on world load or on /reload
        if (type == PackType.SERVER_DATA) {
            //reload dynamic packs before reloading data packs
            EventHelper.postEvent(IEarlyPackReloadEvent.create(packs, this), IEarlyPackReloadEvent.class);
            //ServerEarlyResourceManager.loadResources(packs, this);
        }
    }
}
