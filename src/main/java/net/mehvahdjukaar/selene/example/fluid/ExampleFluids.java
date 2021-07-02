package net.mehvahdjukaar.selene.example.fluid;

import net.mehvahdjukaar.selene.fluids.SoftFluid;
import net.mehvahdjukaar.selene.fluids.SoftFluidRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class ExampleFluids {
    public static void init(FMLCommonSetupEvent event){
        SoftFluid exampleFluid;

        exampleFluid = SoftFluidRegistry.register(new SoftFluid(
            new SoftFluid.Builder("minecraft:glass","minecraft:glass","liquid_jar")
                .fromMod("supplementaries")
                .color(0xa8b966)
                .translationKey("item.supplementaries.jar")
                .food("minecraft:golden_apple")
                .emptyHandContainerItem("supplementaries:jar",3))
        );
    }
}
