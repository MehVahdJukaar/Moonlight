package net.mehvahdjukaar.moonlight.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.registry.DynamicRegistrySetupCallback;
import net.mehvahdjukaar.moonlight.api.client.texture_renderer.RenderedTexturesManager;
import net.mehvahdjukaar.moonlight.api.resources.pack.DynamicResourcePack;
import net.mehvahdjukaar.moonlight.api.resources.pack.DynamicTexturePack;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.profiling.jfr.event.WorldLoadFinishedEvent;
import net.minecraft.world.entity.Entity;

public class MoonlightFabricClient implements ClientModInitializer {


    @Override
    public void onInitializeClient() {
        //dont remove
        MoonlightFabric.commonSetup();
        FabricSetupCallbacks.CLIENT_SETUP.forEach(Runnable::run);
    }

    public static void onAtlasReload() {
        for (var p : DynamicResourcePack.INSTANCES) {
            if (p instanceof DynamicTexturePack) {
                p.clearResources();
            }
        }
    }

}
