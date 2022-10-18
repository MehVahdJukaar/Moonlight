package net.mehvahdjukaar.moonlight.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.mehvahdjukaar.moonlight.api.client.texture_renderer.RenderedTexturesManager;

public class MoonlightFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        //WorldRenderEvents.START.register((c) -> RenderedTexturesManager.updateTextures());
        //dont remove
        MoonlightFabric.commonSetup();
        FabricSetupCallbacks.CLIENT_SETUP.forEach(Runnable::run);
    }
}
