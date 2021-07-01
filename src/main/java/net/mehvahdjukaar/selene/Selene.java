package net.mehvahdjukaar.selene;

import net.mehvahdjukaar.selene.setup.ClientSetup;
import net.mehvahdjukaar.selene.setup.ModSetup;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Selene.MOD_ID)
public class Selene {
    public static final String MOD_ID = "selene";

    public static final Logger LOGGER = LogManager.getLogger();

    public Selene() {

        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        bus.addListener(ModSetup::init);
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> bus.addListener(ClientSetup::init));
    }


}
