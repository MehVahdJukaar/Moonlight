package net.mehvahdjukaar.moonlight.core;

import net.mehvahdjukaar.moonlight.api.client.TextureCache;
import net.mehvahdjukaar.moonlight.api.platform.CPlatHelper;
import net.mehvahdjukaar.moonlight.core.client.SoftFluidClient;
import net.minecraft.client.renderer.texture.TextureAtlas;

public class MoonlightClient {

    public static void initClient() {
        CPlatHelper.addAtlasTextureCallback(TextureAtlas.LOCATION_BLOCKS, e -> {
            TextureCache.clear();
            SoftFluidClient.getTexturesToStitch().forEach(e::addSprite);
        });

        CPlatHelper.addClientReloadListener(new SoftFluidClient(), Moonlight.res("soft_fluids"));
    }
}
