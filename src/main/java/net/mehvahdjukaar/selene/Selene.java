package net.mehvahdjukaar.selene;

import net.mehvahdjukaar.selene.block_set.BlockSetManager;
import net.mehvahdjukaar.selene.block_set.leaves.LeavesType;
import net.mehvahdjukaar.selene.block_set.leaves.LeavesTypeRegistry;
import net.mehvahdjukaar.selene.block_set.wood.WoodType;
import net.mehvahdjukaar.selene.block_set.wood.WoodTypeRegistry;
import net.mehvahdjukaar.selene.builtincompat.CompatWoodTypes;
import net.mehvahdjukaar.selene.misc.ModCriteriaTriggers;
import net.mehvahdjukaar.selene.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.selene.network.ClientBoundSyncFluidsPacket;
import net.mehvahdjukaar.selene.network.NetworkHandler;
import net.mehvahdjukaar.selene.villager_ai.VillagerAIManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

@Mod(Selene.MOD_ID)
public class Selene {

    public static final String MOD_ID = "selene";

    public static final Logger LOGGER = LogManager.getLogger();

    public Selene() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        VillagerAIManager.SCHEDULES.register(bus);
        bus.addListener(Selene::init);
        MinecraftForge.EVENT_BUS.register(this);
        BlockSetManager.registerBlockSetDefinition(WoodType.class, new WoodTypeRegistry());
        BlockSetManager.registerBlockSetDefinition(LeavesType.class, new LeavesTypeRegistry());
        CompatWoodTypes.init();
    }

    @SubscribeEvent
    public void addJsonListener(final AddReloadListenerEvent event) {
        event.addListener(SoftFluidRegistry.INSTANCE);
    }

    @SubscribeEvent
    public void onDataLoad(OnDatapackSyncEvent event) {
        // if we're on the server, send syncing packets
        if (ServerLifecycleHooks.getCurrentServer() != null) {
            List<ServerPlayer> playerList = event.getPlayer() != null ? List.of(event.getPlayer()) : event.getPlayerList().getPlayers();
            playerList.forEach(serverPlayer -> NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer),
                    new ClientBoundSyncFluidsPacket(SoftFluidRegistry.getRegisteredFluids())));
        }
    }

    public static void init(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ModCriteriaTriggers.init();
            NetworkHandler.registerMessages();
            VillagerAIManager.init();

        });

    }

}
