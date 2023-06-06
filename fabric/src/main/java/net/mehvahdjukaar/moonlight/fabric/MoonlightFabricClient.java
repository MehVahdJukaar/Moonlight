package net.mehvahdjukaar.moonlight.fabric;

import net.fabricmc.api.ClientModInitializer;

public class MoonlightFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        //dont remove
        MoonlightFabric.commonSetup();
        MLFabricSetupCallbacks.CLIENT_SETUP.forEach(Runnable::run);
    }

}
