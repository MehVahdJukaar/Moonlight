package net.mehvahdjukaar.moonlight.api.platform.configs;

import net.mehvahdjukaar.candlelight.api.ClientOnly;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigCategory;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.moonlight.api.resources.assets.LangBuilder;
import net.mehvahdjukaar.moonlight.api.resources.pack.GlobalCachedStrategy;
import net.minecraft.server.packs.PackType;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.network.SyncConfigsMessage;
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
import java.util.function.Supplier;

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

    /** The pack kind whose cache a dynamic-pack-affecting value of this config should invalidate. */
    protected PackType getPackType() {
        return this.type == ConfigType.CLIENT ? PackType.CLIENT_RESOURCES : PackType.SERVER_DATA;
    }

    /** Invalidates this config's pack cache when a just-written value both changed and affects dynamic packs. */
    protected void invalidatePacksIfChanged(ConfigValueHandle<?> handle, boolean changed) {
        if (changed && handle.affectsDynamicPacks()) {
            GlobalCachedStrategy.forceInvalidateState(this.getPackType());
        }
    }

    public String getFileName() {
        return fileName;
    }

    public Path getFullPath() {
        return filePath;
    }

    public abstract void loadFromBytes(InputStream stream, boolean readOnly);

    @Nullable
    @ClientOnly
    public Screen makeScreen(Screen parent) {
        return makeScreen(parent, null);
    }

    @Nullable
    @ClientOnly
    public abstract Screen makeScreen(Screen parent, @Nullable ResourceLocation background);

    /**
     * Loader independent, server safe description of this config as a navigable tree. The client side
     * {@code MoonlightConfigScreen} consumes it; this base class never references the screen so the holder stays
     * server safe. Returns null if this holder doesn't expose one.
     */
    @Nullable
    public ConfigCategory getConfigRoot() {
        return null;
    }

    //send configs from server -> client
    public void syncConfigsToPlayer(ServerPlayer player) {
        if (this.isSynced()) {
            try {
                final byte[] configData = getConfigFileData();
                NetworkHelper.sendToClientPlayer(player, new SyncConfigsMessage(configData, this.getId()));
            } catch (IOException e) {
                Moonlight.LOGGER.error("Failed to sync common configs {}", this.getFileName(), e);
            }
        } else throw new UnsupportedOperationException("Tried to sync a config of type " + this.getConfigType());
    }

    //send configs from client -> server
    public void sendChangedConfigToServer() {
        if (this.isSynced()) {
            try {
                final byte[] configData = getConfigFileData();
                NetworkHelper.sendToServer(new SyncConfigsMessage(configData, this.getId()));
            } catch (IOException e) {
                Moonlight.LOGGER.error("Failed to sync common configs {}", this.getFileName(), e);
            }
        }
    }

    protected byte[] getConfigFileData() throws IOException {
        return Files.readAllBytes(this.getFullPath());
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

    public abstract <T> void manuallySetValue(Supplier<T> config, T value);
}
