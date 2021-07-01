package net.mehvahdjukaar.selene.example.map;

import net.mehvahdjukaar.selene.map.CustomDecoration;
import net.mehvahdjukaar.selene.map.CustomDecorationType;
import net.mehvahdjukaar.selene.map.MapDecorationHandler;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class ExampleReg {
    public static final CustomDecorationType<CustomDecoration, ExampleMarker> EXAMPLE_DECORATION_TYPE = new CustomDecorationType<>(
            new ResourceLocation("miecraft", "example"), ExampleMarker::new, ExampleMarker::getFromWorld, CustomDecoration::new);

    public static void init(FMLCommonSetupEvent event){
        MapDecorationHandler.register(EXAMPLE_DECORATION_TYPE);
    }
}
