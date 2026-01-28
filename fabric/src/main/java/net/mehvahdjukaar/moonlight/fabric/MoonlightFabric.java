package net.mehvahdjukaar.moonlight.fabric;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.configs.fabric.FabricConfigSpec;
import net.mehvahdjukaar.moonlight.api.platform.fabric.RegHelperImpl;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkDir;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.fake_player.FPClientAccess;
import net.mehvahdjukaar.moonlight.core.fake_player.FakeGenericPlayer;
import net.mehvahdjukaar.moonlight.core.fluid.SoftFluidInternal;
import net.mehvahdjukaar.moonlight.core.misc.FakeLevelManager;
import net.mehvahdjukaar.moonlight.core.network.ClientBoundSendLoginPacket;
import net.mehvahdjukaar.moonlight.core.network.ModMessages;
import net.mehvahdjukaar.moonlight.core.network.fabric.ClientBoundOpenScreenMessage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MoonlightFabric implements ModInitializer, DedicatedServerModInitializer {

    private static boolean isInit = true;
    private static MinecraftServer currentServer;

    @Override
    public void onInitialize() {

        Moonlight.commonInit();

        ModMessages.CHANNEL.register(NetworkDir.PLAY_TO_CLIENT,
                ClientBoundOpenScreenMessage.class, ClientBoundOpenScreenMessage::new);

        ServerPlayConnectionEvents.JOIN.register((l, s, m) -> ModMessages.CHANNEL.sendToClientPlayer(l.player,
                new ClientBoundSendLoginPacket()));
        ServerLifecycleEvents.SERVER_STARTING.register(s -> {
            currentServer = s;
            Moonlight.beforeServerStart(s.registryAccess());
        });
        CommonLifecycleEvents.TAGS_LOADED.register(Moonlight::afterDataReload);
        ServerLifecycleEvents.SERVER_STOPPING.register(s -> {
            currentServer = null;
            FakeLevelManager.invalidateAll();
        });

        ServerWorldEvents.UNLOAD.register((server, world) -> {
            try {
                FakeGenericPlayer.unloadLevel(world);
                if (PlatHelper.getPhysicalSide().isClient()) {
                    //got to be careful with classloading
                    FPClientAccess.unloadLevel(world);
                }
            } catch (Exception e) {
                Moonlight.LOGGER.error("Failed to unload fake players for level {}", world, e);
            }
        });
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register(SoftFluidInternal::onDataSyncToPlayer);
        ServerPlayerEvents.COPY_FROM.register(Moonlight::onPlayerCloned);

        ResourceConditionsBridge.init();
    }

    //called after all other mod initialize have been called.
    // we can register extra stuff here that depends on those before client and server common setup is fired
    static void commonSetup() {


        RegHelperImpl.lateRegisterEntries();
        FabricConfigSpec.loadAllConfigs();
        MLFabricSetupCallbacks.COMMON_SETUP.forEach(Runnable::run);
        MLFabricSetupCallbacks.COMMON_SETUP.clear();

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


    public static final List<RepositorySource> EXTRA_DATA_PACK_SOURCES = new ArrayList<>();
}
