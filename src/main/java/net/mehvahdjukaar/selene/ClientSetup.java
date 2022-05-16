package net.mehvahdjukaar.selene;

import net.mehvahdjukaar.selene.client.texture_renderer.FlatItemTextureManager;
import net.mehvahdjukaar.selene.fluids.FluidTextures;
import net.mehvahdjukaar.selene.fluids.client.FluidParticleColors;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = Selene.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientSetup {

    @SubscribeEvent
    public static void init(final FMLClientSetupEvent event) {
    }

    @SubscribeEvent
    public static void onResourcePackChanged(ModelBakeEvent event) {
        FluidParticleColors.refresh();
    }

    //textures
    @SubscribeEvent
    public static void onTextureStitch(TextureStitchEvent.Pre event) {
        if (event.getAtlas().location().equals(TextureAtlas.LOCATION_BLOCKS)) {
            FluidTextures.getTexturesToStitch().forEach(event::addSprite);
        }
    }

    @Mod.EventBusSubscriber(modid = Selene.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ClientEvents {

        @SubscribeEvent
        public static void aa(TickEvent.RenderTickEvent event) {
            if (event.phase == TickEvent.Phase.START) {
                FlatItemTextureManager.updateTextures();

                try {
                    /*
                    if(Minecraft.getInstance().level != null) {
                        if(Minecraft.getInstance().level.getGameTime()%40==0) {
                            FrameBufferBackedDynamicTexture texture = FlatItemTextureManager
                                    .getFlatItemTexture(Items.ENCHANTING_TABLE, 128);
                            FlatItemTextureManager.drawItem(texture, Items.ENCHANTING_TABLE);
                            Path outputFolder = Paths.get("texture_d111");
                            outputFolder = Files.createDirectories(outputFolder);
                            texture.saveTextureToFile(outputFolder);
                        }
                    }*/
                } catch (Exception e) {

                }

            }


        }


    }

}
