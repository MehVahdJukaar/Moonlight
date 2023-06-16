package net.mehvahdjukaar.moonlight.fabric;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.configs.fabric.FabricConfigSpec;
import net.mehvahdjukaar.moonlight.api.platform.fabric.RegHelperImpl;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkDir;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.MoonlightClient;
import net.mehvahdjukaar.moonlight.core.network.ClientBoundSendLoginPacket;
import net.mehvahdjukaar.moonlight.core.network.ModMessages;
import net.mehvahdjukaar.moonlight.core.network.fabric.ClientBoundOpenScreenMessage;
import net.minecraft.server.MinecraftServer;

public class MoonlightFabric implements ModInitializer, DedicatedServerModInitializer {

    @Override
    public void onInitialize() {
        Moonlight.commonInit();
        //client init
        if (PlatHelper.getPhysicalSide().isClient()) {
             MoonlightClient.initClient();
        }

        ModMessages.CHANNEL.register(NetworkDir.PLAY_TO_CLIENT,
                ClientBoundOpenScreenMessage.class, ClientBoundOpenScreenMessage::new);

        ServerPlayConnectionEvents.JOIN.register((l, s, m) -> ModMessages.CHANNEL.sendToClientPlayer(l.player,
                new ClientBoundSendLoginPacket()));
        ServerLifecycleEvents.SERVER_STARTING.register(s -> currentServer = s);
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register(SoftFluidRegistry::onDataSyncToPlayer);
        ServerLifecycleEvents.SERVER_STARTED.register((s) -> SoftFluidRegistry.onDataLoad()); //need this too because fabric is stupid
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((a, b, c) -> SoftFluidRegistry.onDataLoad()); //only fire after reload command
        ServerPlayerEvents.COPY_FROM.register(Moonlight::onPlayerCloned);

        ResourceConditionsBridge.init();

    }

    //called after all other mod initialize have been called.
    // we can register extra stuff here that depends on those before client and server common setup is fired
    static void commonSetup() {
        RegHelperImpl.lateRegisterEntries();
        FabricConfigSpec.loadAllConfigs();
        MLFabricSetupCallbacks.BEFORE_COMMON_SETUP.forEach(Runnable::run);
        MLFabricSetupCallbacks.COMMON_SETUP.forEach(Runnable::run);

        MLFabricSetupCallbacks.BEFORE_COMMON_SETUP.clear();
        MLFabricSetupCallbacks.COMMON_SETUP.clear();
    }

    public static MinecraftServer currentServer;


    @Override
    public void onInitializeServer() {
        commonSetup();
    }

}
