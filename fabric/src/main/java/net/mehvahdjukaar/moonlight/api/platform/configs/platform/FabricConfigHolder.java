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
import net.mehvahdjukaar.candlelight.api.ClientOnly;
import net.mehvahdjukaar.moonlight.api.integration.cloth_config.ClothConfigCompat;
import net.mehvahdjukaar.moonlight.api.integration.yacl.YACLCompat;
import net.mehvahdjukaar.moonlight.api.misc.EventCalled;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigType;
import net.mehvahdjukaar.moonlight.api.platform.configs.ModConfigHolder;
import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigCategory;
import net.mehvahdjukaar.moonlight.api.resources.pack.GlobalCachedStrategy;
import net.mehvahdjukaar.moonlight.api.platform.configs.platform.values.*;
import net.mehvahdjukaar.moonlight.core.ClientConfigs;
import net.mehvahdjukaar.moonlight.core.CompatHandler;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.client.config.MoonlightConfigScreen;
import net.mehvahdjukaar.moonlight.platform.MoonlightFabricClient;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.jetbrains.annotations.ApiStatus;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public final class FabricConfigHolder extends ModConfigHolder {

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
    private final ConfigCategory configRoot;

    public FabricConfigHolder(ResourceLocation name, ConfigSubCategory mainEntry, ConfigType type, Runnable changeCallback,
                              ConfigCategory configRoot) {
        super(name, "json", FabricLoader.getInstance().getConfigDir(), type, changeCallback);
        this.file = this.getFullPath().toFile();
        this.mainEntry = mainEntry;
        this.configRoot = configRoot;
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
            //jo.addProperty("#README", "This config file does not support comments. To see them configure it in-game using YACL or Cloth Config (or just use Forge)");
            mainEntry.getEntries().forEach(e -> e.saveToJson(jo));

            // 1) prepare a map of comments for top-level keys.
            // Assumes each entry exposes getName() and getComment(). Adjust as needed.
            Map<String, String> comments = new LinkedHashMap<>();
            mainEntry.gatherAllValues().forEach(e -> {
                String key = e.getName();
                String comment = e.getRawComment();
                String extraComment = e.getExtraInfo();
                if (!extraComment.isEmpty()) {
                    if (!comment.isEmpty()) {
                        comment += "\n";
                    }
                    comment += extraComment;
                }
                if (!comment.isEmpty()) {
                    comments.put(key, comment);
                }
            });

            // 2) pretty-print JSON into a string
            String json = GSON.toJson(jo);

            // 3) inject comments into the pretty JSON string
            String commentedJson = injectCommentsBeforeKeys(json, comments);

            // 4) write to file (UTF-8)
            try (FileOutputStream stream = new FileOutputStream(this.file);
                 Writer writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8)) {
                writer.write(commentedJson);
            }

        } catch (IOException e) {
            Moonlight.LOGGER.error("Failed to save config {}:", this.getReadableName(), e);
        }
        this.onRefresh();
    }

    private static String injectCommentsBeforeKeys(String prettyJson, Map<String, String> commentsByKey) {
        if (commentsByKey == null || commentsByKey.isEmpty()) return prettyJson;

        StringBuilder out = new StringBuilder();
        String[] lines = prettyJson.split("\n", -1);

        for (String line : lines) {
            String trimmed = line.trim();

            boolean inserted = false;
            // check each key — keys are expected to appear as: "key": ...
            for (Map.Entry<String, String> entry : commentsByKey.entrySet()) {
                String key = entry.getKey();
                if (key == null) continue;
                String quotedKey = "\"" + key + "\"";
                if (trimmed.startsWith(quotedKey + ":") || trimmed.startsWith(quotedKey + " :")) {
                    // preserve indentation
                    int indentLen = line.indexOf(quotedKey);
                    String indent = indentLen > 0 ? line.substring(0, indentLen) : "";

                    // write comment lines, support multi-line comments
                    String comment = entry.getValue();
                    String[] commentLines = comment.split("\n");
                    for (String cLine : commentLines) {
                        out.append(indent).append("// ").append(cLine).append('\n');
                    }
                    // now append the actual key line
                    out.append(line).append('\n');
                    inserted = true;
                    break; // matched a key for this line — go to next line
                }
            }
            if (!inserted) {
                out.append(line).append('\n');
            }
        }
        return out.toString();
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
    protected void persist() {
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
