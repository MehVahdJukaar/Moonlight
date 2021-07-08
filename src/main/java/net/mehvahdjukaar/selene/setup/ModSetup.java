package net.mehvahdjukaar.selene.setup;

import net.mehvahdjukaar.selene.common.ModCriteriaTriggers;
import net.mehvahdjukaar.selene.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.selene.network.NetworkHandler;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class ModSetup {
    public static void init(final FMLCommonSetupEvent event) {
        event.enqueueWork(()->{
            ModCriteriaTriggers.init();
            NetworkHandler.registerMessages();
            SoftFluidRegistry.init();
        });

    }
}
