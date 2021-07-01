package net.mehvahdjukaar.selene.setup;

import net.mehvahdjukaar.selene.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.selene.network.NetworkHandler;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class ModSetup {
    public static void init(final FMLCommonSetupEvent event) {
        NetworkHandler.registerMessages();
        SoftFluidRegistry.init();
    }
}
