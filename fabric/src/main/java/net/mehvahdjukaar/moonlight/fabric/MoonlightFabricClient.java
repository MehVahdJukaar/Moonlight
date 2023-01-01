package net.mehvahdjukaar.moonlight.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

public class MoonlightFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        //dont remove
        MoonlightFabric.commonSetup();
        FabricHooks.CLIENT_SETUP.forEach(Runnable::run);
        WorldRenderEvents.START.register((c) -> partialTicks = c.tickDelta());

    }

    public static float partialTicks;

}
