package net.mehvahdjukaar.moonlight.api.platform.configs;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.network.ClientBoundSyncConfigsPacket;
import net.mehvahdjukaar.moonlight.core.network.ModMessages;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class ConfigSpec {

    private final String fileName;
    private final String modId;
    private final Path filePath;
    private final ConfigType type;
    private final boolean synced;

    public ConfigSpec(ResourceLocation name, Path configDirectory, ConfigType type) {
        this(name, configDirectory, type, false);
    }

    public ConfigSpec(ResourceLocation name, Path configDirectory, ConfigType type, boolean synced) {
        this.fileName = name.getNamespace() + "-" + name.getPath() + ".json";
        this.modId = name.getNamespace();
        this.filePath = configDirectory.resolve(fileName);
        this.type = type;
        this.synced = synced;
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

    @Nullable
    @Environment(EnvType.CLIENT)
    public Screen makeScreen(Screen parent) {
        return makeScreen(parent, null);
    }

    @Nullable
    @Environment(EnvType.CLIENT)
    public abstract Screen makeScreen(Screen parent, @Nullable ResourceLocation background);

    //send configs from server -> client
    public void syncConfigsToPlayer(ServerPlayer player) {
        if (this.getConfigType() == ConfigType.COMMON) {
            try {
                final byte[] configData = Files.readAllBytes(this.getFullPath());
                ModMessages.CHANNEL.sendToPlayerClient(player, new ClientBoundSyncConfigsPacket(configData, this.getFileName(), this.getModId()));
            } catch (IOException e) {
                Moonlight.LOGGER.error("Failed to sync common configs {}", this.getFileName(), e);
            }
        }
    }

}
