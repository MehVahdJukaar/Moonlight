package net.mehvahdjukaar.selene;

import net.mehvahdjukaar.selene.block_set.BlockSetManager;
import net.mehvahdjukaar.selene.block_set.wood.WoodTypeRegistry;
import net.mehvahdjukaar.selene.block_set.wood.WoodType;
import net.mehvahdjukaar.selene.builtincompat.mapatlas.CompatWoodTypes;
import net.mehvahdjukaar.selene.data.ModCriteriaTriggers;
import net.mehvahdjukaar.selene.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.selene.network.NetworkHandler;
import net.mehvahdjukaar.selene.villager_ai.VillagerAIManager;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Selene.MOD_ID)
public class Selene {

    public static final String MOD_ID = "selene";

    public static final Logger LOGGER = LogManager.getLogger();

    public Selene() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        VillagerAIManager.SCHEDULES.register(bus);
        bus.addListener(Selene::init);
        BlockSetManager.registerBlockSetDefinition(WoodType.class, new WoodTypeRegistry());
        BlockSetManager.addBlockSetRegistrationCallback((a,b)->{}, Block.class, WoodType.class);
        CompatWoodTypes.init();
    }

    public static void init(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ModCriteriaTriggers.init();
            NetworkHandler.registerMessages();
            SoftFluidRegistry.init();
            VillagerAIManager.init();
            //BlockSetHandler.onModSetup();
        });

    }


}
