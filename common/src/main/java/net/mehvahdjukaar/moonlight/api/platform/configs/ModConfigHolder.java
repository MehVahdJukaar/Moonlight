package net.mehvahdjukaar.moonlight.api.platform.configs;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.moonlight.api.resources.assets.LangBuilder;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.network.ClientBoundSyncConfigsMessage;
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
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class ModConfigHolder {

    private static final Map<ResourceLocation, ModConfigHolder> CONFIG_STORAGE = new ConcurrentHashMap<>(); //wack. multithreading mod loading

    public static void addTrackedSpec(ModConfigHolder spec) {
        var old = CONFIG_STORAGE.put(spec.getId(), spec);
        if (old != null) {
            throw new IllegalStateException("Duplicate config type for with id " + spec.getId());
        }
    }

    public static Collection<ModConfigHolder> getTrackedSpecs() {
        return CONFIG_STORAGE.values();
    }

    @Nullable
    public static ModConfigHolder getConfigSpec(ResourceLocation configId) {
        return CONFIG_STORAGE.get(configId);
    }

    private final ResourceLocation configId;
    private final String fileName;
    private final Component readableName;
    private final Path filePath;
    private final ConfigType type;
    @Nullable
    private final Runnable changeCallback;

    protected ModConfigHolder(ResourceLocation id, String fileExtension, Path configDirectory, ConfigType type, @Nullable Runnable changeCallback) {
        this.configId = id;
        this.fileName = id.getNamespace() + "-" + id.getPath() + "." + fileExtension;
        this.filePath = configDirectory.resolve(fileName);
        this.type = type;
        this.changeCallback = changeCallback;
        this.readableName = Component.literal(LangBuilder.getReadableName(id.toDebugFileName() + "_configs"));

        ModConfigHolder.addTrackedSpec(this);
    }

    public Component getReadableName() {
        return readableName;
    }

    protected void onRefresh() {
        if (this.changeCallback != null) {
            this.changeCallback.run();
        }
    }

    public boolean isLoaded() {
        return true;
    }

    public abstract void forceLoad();

    public ConfigType getConfigType() {
        return type;
    }

    public String getModId() {
        return configId.getNamespace();
    }

    public ResourceLocation getId() {
        return configId;
    }

    public boolean isSynced() {
        return this.type.isSynced();
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
        if (this.isSynced()) {
            try {
                final byte[] configData = Files.readAllBytes(this.getFullPath());
                NetworkHelper.sendToClientPlayer(player, new ClientBoundSyncConfigsMessage(configData, this.getId()));
            } catch (IOException e) {
                Moonlight.LOGGER.error("Failed to sync common configs {}", this.getFileName(), e);
            }
        } else throw new UnsupportedOperationException("Tried to sync a config of type " + this.getConfigType());
    }


    //called on server. sync server -> all clients
    public void sendSyncedConfigsToAllPlayers() {
        if (this.isSynced()) {
            MinecraftServer currentServer = PlatHelper.getCurrentServer();
            if (currentServer != null) {
                PlayerList playerList = currentServer.getPlayerList();
                for (ServerPlayer player : playerList.getPlayers()) {
                    syncConfigsToPlayer(player);
                }
            }
        } else throw new UnsupportedOperationException("Tried to sync a config of type " + this.getConfigType());
    }

    public static class ConfigLoadingException extends RuntimeException {
        public ConfigLoadingException(ModConfigHolder config, Exception cause) {
            super("Failed to load config file " + config.getFileName() + " of type " + config.getConfigType() + " for mod " + config.getModId() + ". Try deleting it", cause);
        }
    }
}
