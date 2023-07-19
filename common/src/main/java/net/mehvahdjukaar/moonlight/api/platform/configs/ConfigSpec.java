package net.mehvahdjukaar.moonlight.api.platform.configs;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.network.ClientBoundSyncConfigsMessage;
import net.mehvahdjukaar.moonlight.core.network.ModMessages;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;

import org.jetbrains.annotations.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class ConfigSpec {

    protected static final Map<String, Map<ConfigType, ConfigSpec>> CONFIG_STORAGE = new ConcurrentHashMap<>(); //wack. multithreading mod loading


    public static void addTrackedSpec(ConfigSpec spec) {
        var map = CONFIG_STORAGE.computeIfAbsent(spec.getModId(), n -> new HashMap<>());
        map.put(spec.getConfigType(), spec);
    }

    @Nullable
    public static ConfigSpec getSpec(String modId, ConfigType type) {
        var map = CONFIG_STORAGE.get(modId);
        if (map != null) {
            return map.getOrDefault(type, null);
        }
        return null;
    }

    private final String fileName;
    private final String modId;
    private final Path filePath;
    private final ConfigType type;
    private final boolean synced;
    @Nullable
    private final Runnable changeCallback;

    protected ConfigSpec(String modId, String fileName, Path configDirectory, ConfigType type, boolean synced, @Nullable Runnable changeCallback) {
        this.fileName = fileName;
        this.modId = modId;
        this.filePath = configDirectory.resolve(fileName);
        this.type = type;
        this.synced = synced;
        this.changeCallback = changeCallback;
    }

    public abstract Component getName();

    protected void onRefresh(){
        if(this.changeCallback!= null){
            this.changeCallback.run();
        }
    }

    public boolean isLoaded(){
        return true;
    }

    public abstract void loadFromFile();

    public abstract void register();

    public ConfigType getConfigType() {
        return type;
    }

    public String getModId() {
        return modId;
    }

    public boolean isSynced() {
        return synced;
    }

    public String getFileName() {
        return fileName;
    }

    public Path getFullPath() {
        return filePath;
    }

    public abstract void loadFromBytes(InputStream stream);

    @Nullable
    @Environment(EnvType.CLIENT)
    public Screen makeScreen(Screen parent) {
        return makeScreen(parent, null);
    }

    @Nullable
    @Environment(EnvType.CLIENT)
    public abstract Screen makeScreen(Screen parent, @Nullable ResourceLocation background);

    //serverside method
    public abstract boolean hasConfigScreen();

    //send configs from server -> client
    public void syncConfigsToPlayer(ServerPlayer player) {
        if (this.getConfigType() == ConfigType.COMMON && this.isSynced()) {
            try {
                final byte[] configData = Files.readAllBytes(this.getFullPath());
                ModMessages.CHANNEL.sendToClientPlayer(player, new ClientBoundSyncConfigsMessage(configData, this.getFileName(), this.getModId()));
            } catch (IOException e) {
                Moonlight.LOGGER.error("Failed to sync common configs {}", this.getFileName(), e);
            }
        } else throw new UnsupportedOperationException();
    }


    //called on server. sync server -> all clients
    public void sendSyncedConfigsToAllPlayers() {
        if (this.getConfigType() == ConfigType.COMMON && this.isSynced()) {
            MinecraftServer currentServer = PlatHelper.getCurrentServer();
            if (currentServer != null) {
                PlayerList playerList = currentServer.getPlayerList();
                for (ServerPlayer player : playerList.getPlayers()) {
                    syncConfigsToPlayer(player);
                }
            }
        } else throw new UnsupportedOperationException();
    }

}
