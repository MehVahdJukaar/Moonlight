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

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.function.Consumer;

@Mod(Selene.MOD_ID)
public class Selene {

    public static final String MOD_ID = "selene";

    public static final Logger LOGGER = LogManager.getLogger();

    //mod specific bus. important because it will have lowest priority thanks to wood loader dummy mod
    public static IEventBus MOD_BUS = null;
    
    public Selene() {

        MOD_BUS = FMLJavaModLoadingContext.get().getModEventBus();
        VillagerAIManager.SCHEDULES.register(MOD_BUS);
        MOD_BUS.addListener(ModSetup::init);
        MOD_BUS.addListener(BlockSetHandler::detectWoodTypes);
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> MOD_BUS.addListener(ClientSetup::init));

        BUS_WORK_QUEUE.forEach(w->w.accept(MOD_BUS));
    }

    private static final Queue<Consumer<IEventBus>> BUS_WORK_QUEUE = new PriorityQueue<>();

    //tries to run some code on the mod bus as late as possible
    public static void enqueueLateBusWork(Consumer<IEventBus> work){
        if(MOD_BUS == null){
            BUS_WORK_QUEUE.add(work);
        }
        else{
            //if bus is not null means this current mod is running AFTER this so we can register on ITS bus
            work.accept(FMLJavaModLoadingContext.get().getModEventBus());
            //work.accept(MOD_BUS);

        }
    }





}
