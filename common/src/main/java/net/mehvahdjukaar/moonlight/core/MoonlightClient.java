package net.mehvahdjukaar.moonlight.core;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.mehvahdjukaar.moonlight.api.platform.ClientPlatformHelper;
import net.mehvahdjukaar.moonlight.core.client.SoftFluidClient;
import net.minecraft.client.renderer.texture.TextureAtlas;

public class MoonlightClient {

    public static void initClient() {
        ClientPlatformHelper.addAtlasTextureCallback(TextureAtlas.LOCATION_BLOCKS, e -> {
            SoftFluidClient.getTexturesToStitch().forEach(e::addSprite);
        });

        ClientPlatformHelper.addClientReloadListener(new SoftFluidClient(),Moonlight.res("soft_fluids"));
    }
}
