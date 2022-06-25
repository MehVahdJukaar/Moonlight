package net.mehvahdjukaar.moonlight.configs;

import net.mehvahdjukaar.moonlight.Moonlight;
import net.mehvahdjukaar.moonlight.network.NetworkHandler;
import net.mehvahdjukaar.moonlight.network.SyncCommonConfigsPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.config.IConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.io.IOException;
import java.nio.file.Files;

public class SyncedModConfigs extends ModConfig {

    public SyncedModConfigs(IConfigSpec<?> spec, ModContainer container, String fileName) {
        super(Type.COMMON, spec, container, fileName);
        this.register();
    }

    public SyncedModConfigs(IConfigSpec<?> spec, ModContainer activeContainer) {
        super(Type.COMMON, spec, activeContainer);
        this.register();
    }


    private void register() {
        var bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(this::onConfigChange);
        MinecraftForge.EVENT_BUS.addListener(this::onPlayerLoggedIn);
        MinecraftForge.EVENT_BUS.addListener(this::onPlayerLoggedOut);

    }


    protected void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getPlayer() instanceof ServerPlayer serverPlayer) {
            //send this configuration to connected clients
            syncConfigs(serverPlayer);
        }
    }

    protected void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getPlayer().level.isClientSide) {
            //ServerConfigs.cached.refresh();
            onRefresh();
        }
    }

    protected void onConfigChange(ModConfigEvent event) {
        if (event.getConfig().getSpec() == this.getSpec()) {
            //send this configuration to connected clients
            sendSyncedConfigsToAllPlayers();
            onRefresh();
        }
    }

    //called on server. sync server -> all clients
    public void sendSyncedConfigsToAllPlayers() {
        MinecraftServer currentServer = ServerLifecycleHooks.getCurrentServer();
        if (currentServer != null) {
            PlayerList playerList = currentServer.getPlayerList();
            for (ServerPlayer player : playerList.getPlayers()) {
                syncConfigs(player);
            }
        }
    }

    //send configs from server -> client
    public void syncConfigs(ServerPlayer player) {
        try {
            final byte[] configData = Files.readAllBytes(this.getFullPath());
            NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
                    new SyncCommonConfigsPacket(configData, this.getFileName()));
        } catch (IOException e) {
            Moonlight.LOGGER.error("Failed to sync common configs", e);
        }
    }

    public void onRefresh() {
    }
}
