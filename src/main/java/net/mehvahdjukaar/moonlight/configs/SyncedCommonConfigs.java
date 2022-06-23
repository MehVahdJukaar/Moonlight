package net.mehvahdjukaar.moonlight.configs;

import net.mehvahdjukaar.moonlight.Moonlight;
import net.mehvahdjukaar.moonlight.network.NetworkHandler;
import net.mehvahdjukaar.moonlight.network.SyncCommonConfigsPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.config.IConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.io.IOException;
import java.nio.file.Files;

public class SyncedCommonConfigs extends ModConfig {

    public SyncedCommonConfigs(IConfigSpec<?> spec, ModContainer container, String fileName) {
        super(Type.COMMON, spec, container, fileName);
        this.register();
    }

    public SyncedCommonConfigs(IConfigSpec<?> spec, ModContainer activeContainer) {
        super(Type.COMMON, spec, activeContainer);
        this.register();
    }


    private void register() {
        var bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(this::onPlayerLoggedIn);
        bus.addListener(this::onPlayerLoggedOut);
        bus.addListener(this::onConfigChange);
    }


    protected void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getPlayer() instanceof ServerPlayer serverPlayer) {
            //send this configuration to connected clients
            syncServerConfigs(serverPlayer);
        }
    }

    protected void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getPlayer().level.isClientSide) {
            //ServerConfigs.cached.refresh();
        }
    }

    protected void onConfigChange(ModConfigEvent event) {
        if (event.getConfig().getSpec() == this.getSpec()) {
            //send this configuration to connected clients
            sendSyncedConfigsToAllPlayers();
        }
    }

    //called on server. sync server -> all clients
    public void sendSyncedConfigsToAllPlayers() {
        MinecraftServer currentServer = ServerLifecycleHooks.getCurrentServer();
        if (currentServer != null) {
            PlayerList playerList = currentServer.getPlayerList();
            for (ServerPlayer player : playerList.getPlayers()) {
                syncServerConfigs(player);
            }
        }
    }

    //send configs from server -> client
    public void syncServerConfigs(ServerPlayer player) {
        try {
            var string = this.getFullPath();
            var s2 = FMLPaths.CONFIGDIR.get().resolve(this.getFileName()).toAbsolutePath();
            if (s2 != string) {
                int aaa = 1;
            }

            final byte[] configData = Files.readAllBytes(string);
            NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
                    new SyncCommonConfigsPacket(configData, this.getFileName()));
        } catch (IOException e) {
            Moonlight.LOGGER.error("Failed to sync common configs", e);
        }
    }

    public void onRefresh() {
    }
}
