package net.mehvahdjukaar.moonlight.api.platform.configs.fabric;

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
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigSpec;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigType;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.jetbrains.annotations.ApiStatus;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static net.mehvahdjukaar.moonlight.core.CompatHandler.CLOTH_CONFIG;
import static net.mehvahdjukaar.moonlight.core.CompatHandler.YACL;

public final class FabricConfigSpec extends ConfigSpec {

    @ApiStatus.Internal
    public static void loadAllConfigs() {
        for (var spec : getTrackedSpecs()) {
            spec.forceLoad();
        }
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final ConfigSubCategory mainEntry;
    private final File file;
    private boolean initialized = false;

    public FabricConfigSpec(ResourceLocation name, ConfigSubCategory mainEntry, ConfigType type, Runnable changeCallback) {
        super(name, "json", FabricLoader.getInstance().getConfigDir(), type, changeCallback);
        this.file = this.getFullPath().toFile();
        this.mainEntry = mainEntry;
        if (this.isSynced()) {
            ServerPlayConnectionEvents.JOIN.register(this::onPlayerLoggedIn);
        }
    }

    public ConfigSubCategory getMainEntry() {
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
                //don't call a load directly, so we skip the main category name
                mainEntry.getEntries().forEach(e -> e.loadFromJson(jo));
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

    public void saveConfig() {
        try (FileOutputStream stream = new FileOutputStream(this.file);
             Writer writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8)) {

            JsonObject jo = new JsonObject();
            jo.addProperty("#README", "This config file does not support comments. To see them configure it in-game using YACL or Cloth Config (or just use Forge)");
            mainEntry.getEntries().forEach(e -> e.saveToJson(jo));

            GSON.toJson(jo, writer);
        } catch (IOException e) {
            Moonlight.LOGGER.error("Failed to save config {}:", this.getReadableName(), e);
        }
        this.onRefresh();
    }


    @Override
    @Environment(EnvType.CLIENT)
    public Screen makeScreen(Screen parent, ResourceLocation background) {
        if (YACL) {
            return YACLCompat.makeScreen(parent, this, background);
        } else if (CLOTH_CONFIG) {
            return ClothConfigCompat.makeScreen(parent, this, background);
        }
        return null;
    }

    @Override
    public boolean hasConfigScreen() {
        return CLOTH_CONFIG || YACL;
    }

    @Override
    public void loadFromBytes(InputStream stream) {
        InputStreamReader inputStreamReader = new InputStreamReader(stream, StandardCharsets.UTF_8);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        JsonElement config = GSON.fromJson(bufferedReader, JsonElement.class);
        if (config instanceof JsonObject jo) {
            //don't call load directly, so we skip the main category name
            mainEntry.getEntries().forEach(e -> e.loadFromJson(jo));
        }
        this.onRefresh();
    }

    @EventCalled
    private void onPlayerLoggedIn(ServerGamePacketListenerImpl listener, PacketSender sender, MinecraftServer minecraftServer) {
        //send this configuration to connected clients
        syncConfigsToPlayer(listener.player);
    }

}
