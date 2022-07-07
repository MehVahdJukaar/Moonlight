package net.mehvahdjukaar.moonlight.fabric;

import net.mehvahdjukaar.moonlight.core.MoonlightClient;
import net.fabricmc.api.ClientModInitializer;

public class MoonlightFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        MoonlightClient.initClient();


    }


}
