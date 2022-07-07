package net.mehvahdjukaar.moonlight.forge;

import net.mehvahdjukaar.moonlight.core.MoonlightClient;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = MoonlightForge.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class MoonlightForgeClient {

    @SubscribeEvent
    public static void init(final FMLClientSetupEvent event) {
        MoonlightClient.initClient();
    }

}
