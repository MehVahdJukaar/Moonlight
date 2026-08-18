package net.mehvahdjukaar.moonlight.api.platform.configs;

import net.mehvahdjukaar.candlelight.api.ClientOnly;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigCategory;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.moonlight.api.resources.pack.GlobalCachedStrategy;
import net.minecraft.server.packs.PackType;
import net.mehvahdjukaar.moonlight.api.util.TextHelper;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.network.SyncConfigsMessage;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.jetbrains.annotations.ApiStatus;
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

    private static final Map<ResourceLocation, ModConfigHolder> TRACKED_HOLDERS = new ConcurrentHashMap<>(); //wack. multithreading mod loading

    public static void registerHolder(ModConfigHolder holder) {
        var old = TRACKED_HOLDERS.put(holder.getId(), holder);
        if (old != null) {
            throw new IllegalStateException("Duplicate config id " + holder.getId());
        }
    }

    public static Collection<ModConfigHolder> getTrackedHolders() {
        return TRACKED_HOLDERS.values();
    }

    @Nullable
    public static ModConfigHolder getHolder(ResourceLocation configId) {
        return TRACKED_HOLDERS.get(configId);
    }

    @Deprecated(forRemoval = true)
    public static void addTrackedSpec(ModConfigHolder holder) {
        registerHolder(holder);
    }

    @Deprecated(forRemoval = true)
    public static Collection<ModConfigHolder> getTrackedSpecs() {
        return getTrackedHolders();
    }

    @Deprecated(forRemoval = true)
    @Nullable
    public static ModConfigHolder getConfigSpec(ResourceLocation configId) {
        return getHolder(configId);
    }

    private final ResourceLocation configId;
    private final String fileName;
    private final Component readableName;
    private final Path filePath;
    private final ConfigType type;
    @Nullable
    private final Runnable changeCallback;
    // short name and full dotted path -> effective enabled supplier, filled in by the builder at build()
    private Map<String, Supplier<Boolean>> featureToggles = Map.of();

    protected ModConfigHolder(ResourceLocation id, String fileExtension, Path configDirectory, ConfigType type, @Nullable Runnable changeCallback) {
        this(id, fileExtension, configDirectory, type, changeCallback, true);
    }

    // untracked holders only mirror another mod's config (the foreign-config bridge). They stay out of the global
    // registry, both to avoid a duplicate-id clash on re-open and to stay out of sync/enumeration logic
    protected ModConfigHolder(ResourceLocation id, String fileExtension, Path configDirectory, ConfigType type, @Nullable Runnable changeCallback, boolean tracked) {
        this.configId = id;
        this.fileName = id.getNamespace() + "-" + id.getPath() + "." + fileExtension;
        this.filePath = configDirectory.resolve(fileName);
        this.type = type;
        this.changeCallback = changeCallback;
        this.readableName = Component.literal(TextHelper.getReadableName(id.toDebugFileName() + "_configs"));

        if (tracked) ModConfigHolder.registerHolder(this);
    }

    public Component getReadableName() {
        return readableName;
    }

    @ApiStatus.Internal
    public void setFeatureToggles(Map<String, Supplier<Boolean>> featureToggles) {
        this.featureToggles = Map.copyOf(featureToggles);
    }

    public boolean isFeatureEnabled(String nameOrPath) {
        Supplier<Boolean> toggle = this.featureToggles.get(nameOrPath);
        return toggle == null || Boolean.TRUE.equals(toggle.get());
    }

    public Map<String, Supplier<Boolean>> getFeatureToggles() {
        return this.featureToggles;
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

    protected PackType getPackType() {
        return this.type == ConfigType.CLIENT ? PackType.CLIENT_RESOURCES : PackType.SERVER_DATA;
    }

    public <T> void manuallySetValue(Supplier<T> config, T value) {
        if (!(config instanceof IConfigValue<T> handle)) {
            throw new IllegalArgumentException("Config value is not settable: " + config);
        }
        if (handle.setValue(value) && handle.affectsDynamicPacks()) {
            GlobalCachedStrategy.forceInvalidateState(this.getPackType());
        }
        this.saveToDisk();
    }

    protected abstract void saveToDisk();

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
}
