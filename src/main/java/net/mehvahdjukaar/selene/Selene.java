package net.mehvahdjukaar.selene;

import com.mojang.blaze3d.platform.NativeImage;
import net.mehvahdjukaar.selene.data.ModCriteriaTriggers;
import net.mehvahdjukaar.selene.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.selene.network.NetworkHandler;
import net.mehvahdjukaar.selene.textures.SpriteUtils;
import net.mehvahdjukaar.selene.util.BlockSetHandler;
import net.mehvahdjukaar.selene.villager_ai.VillagerAIManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;

@Mod(Selene.MOD_ID)
public class Selene {

    public static final String MOD_ID = "selene";

    public static final Logger LOGGER = LogManager.getLogger();

    public Selene() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        VillagerAIManager.SCHEDULES.register(bus);
        bus.addListener(Selene::init);
        //bus.addGenericListener(Item.class, EventPriority.HIGHEST, BlockSetHandler::registerModLateBlockAndItems);
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> bus.addListener(ClientSetup::init));

        /**
        BlockSetHandler.addWoodRegistrationCallback((a,b)->{
            a.getRegistry().register(
                    new BubbleColumnBlock(BlockBehaviour.Properties.of(Material.BUBBLE_COLUMN).noCollission().noDrops())
                            .setRegistryName("aaa"));

        }, Block.class, MOD_ID);
         **/
    }

    public static void init(final FMLCommonSetupEvent event) {
        event.enqueueWork(()->{
            ModCriteriaTriggers.init();
            NetworkHandler.registerMessages();
            SoftFluidRegistry.init();
            VillagerAIManager.init();
            //BlockSetHandler.onModSetup();
        });

    }


}
