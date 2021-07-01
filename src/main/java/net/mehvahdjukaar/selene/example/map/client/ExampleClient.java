package net.mehvahdjukaar.selene.example.map.client;

import net.mehvahdjukaar.selene.example.map.ExampleReg;
import net.mehvahdjukaar.selene.map.client.DecorationRenderer;
import net.mehvahdjukaar.selene.map.client.MapDecorationRenderHandler;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ExampleClient {
    private static final ResourceLocation SPRITE = new ResourceLocation("minecraft:textures/item/oak_sign.png");

    public static void init(FMLClientSetupEvent event){
        MapDecorationRenderHandler.bindDecorationRenderer(ExampleReg.EXAMPLE_DECORATION_TYPE, new DecorationRenderer<>(SPRITE));
    }
}
