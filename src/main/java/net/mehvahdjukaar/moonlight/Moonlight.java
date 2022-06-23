package net.mehvahdjukaar.moonlight;

import net.mehvahdjukaar.moonlight.block_set.BlockSetManager;
import net.mehvahdjukaar.moonlight.block_set.leaves.LeavesTypeRegistry;
import net.mehvahdjukaar.moonlight.block_set.wood.WoodTypeRegistry;
import net.mehvahdjukaar.moonlight.builtincompat.CompatWoodTypes;
import net.mehvahdjukaar.moonlight.client.SoftFluidClient;
import net.mehvahdjukaar.moonlight.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.moonlight.map.MapDecorationRegistry;
import net.mehvahdjukaar.moonlight.misc.ModCriteriaTriggers;
import net.mehvahdjukaar.moonlight.network.ClientBoundSyncMapDecorationTypesPacket;
import net.mehvahdjukaar.moonlight.network.NetworkHandler;
import net.mehvahdjukaar.moonlight.villager_ai.VillagerAIManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

@Mod(Moonlight.MOD_ID)
public class Moonlight {

    public static final String MOD_ID = "moonlight";

    public static final Logger LOGGER = LogManager.getLogger();

    public Moonlight() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        VillagerAIManager.SCHEDULES.register(bus);
        SoftFluidRegistry.DEFERRED_REGISTER.register(bus);
        bus.addListener(Moonlight::init);
        MinecraftForge.EVENT_BUS.register(this);
        BlockSetManager.registerBlockSetDefinition(new WoodTypeRegistry());
        BlockSetManager.registerBlockSetDefinition(new LeavesTypeRegistry());
        CompatWoodTypes.init();

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            Minecraft mc = Minecraft.getInstance();
            ((ReloadableResourceManager) mc.getResourceManager())
                    .registerReloadListener(new SoftFluidClient());

        });
    }

    public static ResourceLocation res(String replace) {
        return new ResourceLocation(MOD_ID,replace);
    }

    @SubscribeEvent
    public void addJsonListener(final AddReloadListenerEvent event) {
        event.addListener(MapDecorationRegistry.DATA_DRIVEN_REGISTRY);
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        if(!event.getWorld().isClientSide()){
            SoftFluidRegistry.postInitServer();
        }
    }

    @SubscribeEvent
    public void onDataLoad(OnDatapackSyncEvent event) {

        // if we're on the server, send syncing packets
        if (ServerLifecycleHooks.getCurrentServer() != null) {
            List<ServerPlayer> playerList = event.getPlayer() != null ? List.of(event.getPlayer()) : event.getPlayerList().getPlayers();
            playerList.forEach(serverPlayer -> {

              //  NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer),
               //         new ClientBoundSyncFluidsPacket(SoftFluidRegistryOld.getValues()));
                NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer),
                        new ClientBoundSyncMapDecorationTypesPacket(MapDecorationRegistry.DATA_DRIVEN_REGISTRY.getTypes()));
            });
        }
    }

    public static void init(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            NetworkHandler.registerMessages();
            ModCriteriaTriggers.init();
            VillagerAIManager.init();
        });
    }

}
