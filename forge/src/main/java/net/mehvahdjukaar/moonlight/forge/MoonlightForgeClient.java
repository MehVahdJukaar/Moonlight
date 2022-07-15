package net.mehvahdjukaar.moonlight.forge;


import net.mehvahdjukaar.moonlight.api.client.texture_renderer.RenderedTexturesManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MoonlightForge.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MoonlightForgeClient {

    // @SubscribeEvent
    // public static void init(final FMLClientSetupEvent event) {
    //     MoonlightClient.
    //  }


    @SubscribeEvent
    public static void onClientTick(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            RenderedTexturesManager.updateTextures();
        }
    }

}
