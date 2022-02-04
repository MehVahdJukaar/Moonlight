package net.mehvahdjukaar.selene;

import net.mehvahdjukaar.selene.util.BlockSetHandler;
import net.mehvahdjukaar.selene.setup.ClientSetup;
import net.mehvahdjukaar.selene.setup.ModSetup;
import net.mehvahdjukaar.selene.villager_ai.VillagerAIManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.GameData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Selene.MOD_ID)
public class Selene {

    public static final String MOD_ID = "selene";

    public static final Logger LOGGER = LogManager.getLogger();

    public Selene() {

        MOD_BUS = FMLJavaModLoadingContext.get().getModEventBus();
        VillagerAIManager.SCHEDULES.register(MOD_BUS);
        MOD_BUS.addListener(ModSetup::init);
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> MOD_BUS.addListener(ClientSetup::init));

    }

    public static IEventBus MOD_BUS;




}
