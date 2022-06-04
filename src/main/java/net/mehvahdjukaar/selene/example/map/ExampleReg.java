package net.mehvahdjukaar.selene.example.map;

import net.mehvahdjukaar.selene.map.CustomMapDecoration;
import net.mehvahdjukaar.selene.map.type.CustomDecorationType;
import net.mehvahdjukaar.selene.map.type.IMapDecorationType;
import net.mehvahdjukaar.selene.map.MapDecorationRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
//just look at my mods to see how to use this code
public class ExampleReg {
    public static final CustomDecorationType<CustomMapDecoration, ExampleMarker> EXAMPLE_DECORATION_TYPE = new CustomDecorationType<>(
            new ResourceLocation("miecraft", "example"), ExampleMarker::new, ExampleMarker::getFromWorld, CustomMapDecoration::new);

    public static void init(FMLCommonSetupEvent event){
        MapDecorationRegistry.register(EXAMPLE_DECORATION_TYPE);
    }
}
