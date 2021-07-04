package net.mehvahdjukaar.selene.setup;

import net.mehvahdjukaar.selene.Selene;
import net.mehvahdjukaar.selene.fluids.FluidTextures;
import net.mehvahdjukaar.selene.fluids.client.FluidParticleColors;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = Selene.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientSetup {
    public static void init(final FMLClientSetupEvent event) {

    }

    @SubscribeEvent
    public static void onResourcePackChanged(ModelBakeEvent event) {
        FluidParticleColors.refresh();
    }

    //textures
    @SubscribeEvent
    public static void onTextureStitch(TextureStitchEvent.Pre event) {
        if (event.getMap().location().equals(AtlasTexture.LOCATION_BLOCKS)) {
            FluidTextures.getTexturesToStitch().forEach(event::addSprite);
        }
    }
}
