package net.mehvahdjukaar.moonlight.fabric;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.mehvahdjukaar.moonlight.api.platform.configs.fabric.FabricConfigSpec;
import net.mehvahdjukaar.moonlight.api.platform.fabric.RegHelperImpl;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.fluid.SoftFluidInternal;
import net.mehvahdjukaar.moonlight.core.misc.DummyWorld;
import net.mehvahdjukaar.moonlight.core.network.ClientBoundSendLoginPacket;
import net.mehvahdjukaar.moonlight.core.network.ModNetworking;
import net.minecraft.server.MinecraftServer;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MoonlightFabric implements ModInitializer, DedicatedServerModInitializer {

    private static boolean isInit = true;
    private static MinecraftServer currentServer;

    @Override
    public void onInitialize() {

        Moonlight.commonInit();

        ServerPlayConnectionEvents.JOIN.register((l, s, m) -> ModNetworking.CHANNEL.sendToClientPlayer(l.player,
                new ClientBoundSendLoginPacket()));
        ServerLifecycleEvents.SERVER_STARTING.register(s -> {
            currentServer = s;
            Moonlight.beforeServerStart();
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(s -> {
            currentServer = null;
            DummyWorld.clearInstance();
        });
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register(SoftFluidInternal::onDataSyncToPlayer);
        ServerPlayerEvents.COPY_FROM.register(Moonlight::onPlayerCloned);
        ServerWorldEvents.LOAD.register((s, l) -> {
            if (!l.getLevel().isClientSide) Moonlight.checkDatapackRegistry();
        });

        ResourceConditionsBridge.init();
    }

    //called after all other mod initialize have been called.
    // we can register extra stuff here that depends on those before client and server common setup is fired
    static void commonSetup() {


        RegHelperImpl.lateRegisterEntries();
        FabricConfigSpec.loadAllConfigs();

        isInit = false;

        PRE_SETUP_WORK.forEach(Runnable::run);
        COMMON_SETUP_WORK.forEach(Runnable::run);
        AFTER_SETUP_WORK.forEach(Runnable::run);
        PRE_SETUP_WORK.clear();
        COMMON_SETUP_WORK.clear();
        AFTER_SETUP_WORK.clear();
    }

    @Override
    public void onInitializeServer() {
        commonSetup();
    }


    public static MinecraftServer getCurrentServer() {
        return currentServer;
    }

    public static boolean isInitializing() {
        return isInit;
    }

    public static final Queue<Runnable> COMMON_SETUP_WORK = new ConcurrentLinkedQueue<>();
    public static final Queue<Runnable> PRE_SETUP_WORK = new ConcurrentLinkedQueue<>();
    public static final Queue<Runnable> AFTER_SETUP_WORK = new ConcurrentLinkedQueue<>();


}
