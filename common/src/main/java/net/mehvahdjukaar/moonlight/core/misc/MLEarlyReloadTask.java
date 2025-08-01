package net.mehvahdjukaar.moonlight.core.misc;

import net.mehvahdjukaar.moonlight.api.events.EarlyPackReloadEvent;
import net.mehvahdjukaar.moonlight.api.events.MoonlightEventsHelper;
import net.mehvahdjukaar.moonlight.api.resources.pack.DynResourceGenerator;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Unit;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MLEarlyReloadTask {

    public static CompletableFuture<Unit> run(PackType type, ResourceManager manager) {
        return CompletableFuture.supplyAsync(() -> {
            DynResourceGenerator.clearBeforeReload(type);
            MoonlightEventsHelper.postEvent(new EarlyPackReloadEvent(List.of(), manager, type), EarlyPackReloadEvent.class);
            return Unit.INSTANCE;
        });
    }

}
