package net.mehvahdjukaar.moonlight;

import net.mehvahdjukaar.moonlight.client.SoftFluidClient;
import net.mehvahdjukaar.moonlight.client.TextureCache;
import net.mehvahdjukaar.moonlight.client.texture_renderer.RenderedTexturesManager;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.loading.FMLLoader;

@Mod.EventBusSubscriber(modid = Moonlight.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientSetup {

    @SubscribeEvent
    public static void init(final FMLClientSetupEvent event) {
    }

    @SubscribeEvent
    public static void onResourcePackChanged(ModelBakeEvent event) {
        SoftFluidClient.refresh();
        TextureCache.refresh();
        ForgeEventFactory
    }

    //textures
    @SubscribeEvent
    public static void onTextureStitch(TextureStitchEvent.Pre event) {
        if (event.getAtlas().location().equals(TextureAtlas.LOCATION_BLOCKS)) {
            SoftFluidClient.addTextures(event);
        }
    }

    @Mod.EventBusSubscriber(modid = Moonlight.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ClientEvents {

        private static final boolean on = !FMLLoader.getLaunchHandler().isProduction();

        @SubscribeEvent
        public static void aa(TickEvent.RenderTickEvent event) {
            if (event.phase == TickEvent.Phase.END) {
                RenderedTexturesManager.updateTextures();

/*
                try {
                    if( Minecraft.getInstance().level != null) {
                        if(Minecraft.getInstance().level.getGameTime()%40==0) {
                            FrameBufferBackedDynamicTexture t2 = RenderedTexturesManager
                                    .getFlatItemTexture(Items.DIAMOND, 512);
                            RenderedTexturesManager.drawItem(t2,Items.DIAMOND.getDefaultInstance());
                            FrameBufferBackedDynamicTexture texture = RenderedTexturesManager
                                    .getFlatItemTexture(Items.ENCHANTING_TABLE, 512);
                            RenderedTexturesManager.drawItem(texture,Items.ENCHANTING_TABLE.getDefaultInstance());
                            //FlatItemTextureManager.drawItem2(texture,new BlockPos(0,75,0), Direction.NORTH,event.renderTickTime);
                            Path outputFolder = Paths.get("texture_d111");
                            outputFolder = Files.createDirectories(outputFolder);
                            texture.saveTextureToFile(outputFolder);
                            t2.saveTextureToFile(outputFolder);
                        }
                    }
                } catch (Exception e) {

                }
*/
            }


        }


    }

}
