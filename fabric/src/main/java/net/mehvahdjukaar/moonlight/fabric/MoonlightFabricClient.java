package net.mehvahdjukaar.moonlight.fabric;

import net.fabricmc.api.ClientModInitializer;

public class MoonlightFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        //dont remove
        MoonlightFabric.commonSetup();
        FabricHooks.CLIENT_SETUP.forEach(Runnable::run);
    }

}
