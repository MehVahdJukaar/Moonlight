package net.mehvahdjukaar.moonlight.api.platform.configs.platform;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.mehvahdjukaar.moonlight.api.integration.cloth_config.ClothConfigCompat;
import net.mehvahdjukaar.moonlight.api.integration.yacl.YACLCompat;
import net.mehvahdjukaar.moonlight.api.misc.EventCalled;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigType;
import net.mehvahdjukaar.moonlight.api.platform.configs.ModConfigHolder;
import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigCategory;
import net.mehvahdjukaar.moonlight.api.resources.pack.GlobalCachedStrategy;
import net.mehvahdjukaar.moonlight.core.ClientConfigs;
import net.mehvahdjukaar.moonlight.core.CompatHandler;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.client.config.MoonlightConfigScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.jetbrains.annotations.ApiStatus;

import java.io.*;
import java.nio.charset.StandardCharsets;

public final class FabricConfigHolder extends ModConfigHolder {

    @ApiStatus.Internal
    public static void loadAllConfigs() {
        for (var spec : getTrackedHolders()) {
            spec.forceLoad();
        }
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final JsonConfigCategory mainEntry;
    private final File file;
    private boolean initialized = false;
    private final ConfigCategory configRoot;

    public FabricConfigHolder(ResourceLocation name, JsonConfigCategory mainEntry, ConfigType type, Runnable changeCallback,
                              ConfigCategory configRoot) {
        super(name, "json", FabricLoader.getInstance().getConfigDir(), type, changeCallback);
        this.file = this.getFullPath().toFile();
        this.mainEntry = mainEntry;
        this.configRoot = configRoot;
        if (this.isSynced()) {
            ServerPlayConnectionEvents.JOIN.register(this::onPlayerLoggedIn);
        }
    }

    public JsonConfigCategory getMainEntry() {
        return mainEntry;
    }

    @Override
    public boolean isLoaded() {
        return initialized;
    }

    @Override
    public void forceLoad() {
        if (this.isLoaded()) return;

        try {
            JsonElement config = null;

            if (file.exists() && file.isFile()) {
                try (FileInputStream fileInputStream = new FileInputStream(file);
                     InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
                     BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {

                    config = GSON.fromJson(bufferedReader, JsonElement.class);
                }
            }

            if (config instanceof JsonObject jo) {
                loadFromJson(jo);
            }
            if (!initialized) {
                this.initialized = true;
                this.saveConfig();
                Moonlight.LOGGER.info("Loaded config {}", this.getFileName());
            }
        } catch (Exception e) {
            throw new ConfigLoadingException(this, e);
        }
    }

    private void loadFromJson(JsonObject jo) {
        //don't call a load directly, so we skip the main category name
        boolean invalidateDynamicPacks = false;
        for (var entry : mainEntry.getEntries()) {
            invalidateDynamicPacks |= entry.loadFromJson(jo);
        }
        if (invalidateDynamicPacks) {
            GlobalCachedStrategy.forceInvalidateState(getPackType());
        }
    }

    public void saveConfig() {
        try {
            JsonObject jo = new JsonObject();
            mainEntry.getEntries().forEach(e -> e.saveToJson(jo));

            try (FileOutputStream stream = new FileOutputStream(this.file);
                 Writer writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8)) {
                GSON.toJson(jo, writer);
            }

        } catch (IOException e) {
            Moonlight.LOGGER.error("Failed to save config {}:", this.getReadableName(), e);
        }
        this.onRefresh();
    }

    @Override
    public ConfigCategory getConfigRoot() {
        this.forceLoad();
        return configRoot;
    }

    @Override
    @Environment(value = EnvType.CLIENT)
    public Screen makeScreen(Screen parent, ResourceLocation background) {
        if (ClientConfigs.CUSTOM_CONFIG_SCREEN.get()) {
            ConfigCategory root = getConfigRoot();
            return root == null ? null : MoonlightConfigScreen.create(this, root, parent, background);
        }
        // custom screen disabled: fall back to the old Cloth Config / YACL screens if those mods are present
        if (CompatHandler.YACL) {
            return YACLCompat.makeScreen(parent, this, background);
        } else if (CompatHandler.CLOTH_CONFIG) {
            return ClothConfigCompat.makeScreen(parent, this, background);
        }
        return null;
    }

    @Override
    protected void saveToDisk() {
        this.saveConfig();
    }

    @Override
    public void loadFromBytes(InputStream stream, boolean readOnly) {
        //if (readOnly && PlatHelper.isIntegratedServer()) return;
        InputStreamReader inputStreamReader = new InputStreamReader(stream, StandardCharsets.UTF_8);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        JsonElement config = GSON.fromJson(bufferedReader, JsonElement.class);
        if (config instanceof JsonObject jo) {
            //don't call load directly, so we skip the main category name
            loadFromJson(jo);
        }
        this.onRefresh();
    }

    @EventCalled
    private void onPlayerLoggedIn(ServerGamePacketListenerImpl listener, PacketSender sender, MinecraftServer minecraftServer) {
        //send this configuration to connected clients
        syncConfigsToPlayer(listener.player);
    }

}
