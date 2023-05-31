package net.mehvahdjukaar.moonlight.core;

import net.mehvahdjukaar.moonlight.api.map.client.MapDecorationClientManager;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.mehvahdjukaar.moonlight.core.client.SoftFluidParticleColors;

public class MoonlightClient {

    public static void initClient() {

        ClientHelper.addClientReloadListener(SoftFluidParticleColors::new, Moonlight.res("soft_fluids"));
        ClientHelper.addClientReloadListener(MapDecorationClientManager::new, Moonlight.res("map_markers"));
    }
}
