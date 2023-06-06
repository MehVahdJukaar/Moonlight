package net.mehvahdjukaar.moonlight.core;

import net.mehvahdjukaar.moonlight.api.map.client.MapDecorationClientManager;
import net.mehvahdjukaar.moonlight.api.misc.EventCalled;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.mehvahdjukaar.moonlight.api.resources.pack.DynamicResourcePack;
import net.mehvahdjukaar.moonlight.api.resources.pack.DynamicTexturePack;
import net.mehvahdjukaar.moonlight.core.client.SoftFluidParticleColors;

public class MoonlightClient {

    public static void initClient() {

        ClientHelper.addClientReloadListener(SoftFluidParticleColors::new, Moonlight.res("soft_fluids"));
        ClientHelper.addClientReloadListener(MapDecorationClientManager::new, Moonlight.res("map_markers"));
    }

    @EventCalled
    public static void afterTextureReload() {
        DynamicResourcePack.clearAfterReload(true);
    }
}
