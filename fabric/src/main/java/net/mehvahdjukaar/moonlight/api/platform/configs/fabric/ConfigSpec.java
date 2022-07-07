package net.mehvahdjukaar.moonlight.api.platform.configs.fabric;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.mehvahdjukaar.moonlight.api.integration.ClothConfigCompat;
import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigType;
import net.mehvahdjukaar.moonlight.api.platform.configs.IConfigSpec;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class ConfigSpec implements IConfigSpec {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final Map<String, Map<ConfigType, ConfigSpec>> CONFIG_STORAGE = new HashMap<>();
    private final ConfigType type;

    public static void saveSpec(ConfigSpec spec) {
        String modId = spec.getName().getNamespace();
        var map = CONFIG_STORAGE.computeIfAbsent(modId, n -> new HashMap<>());
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

    private final ConfigCategory mainEntry;

    private final ResourceLocation name;
    private final File file;

    public ConfigSpec(ResourceLocation name, ConfigCategory mainEntry, String filePath, ConfigType type) {
        this.name = name;
        this.mainEntry = mainEntry;
        this.file = new File(FabricLoader.getInstance().getConfigDir().toFile(), filePath);
        this.type = type;
    }

    @Override
    public ConfigType getConfigType() {
        return type;
    }

    public ConfigCategory getMainEntry() {
        return mainEntry;
    }

    public ResourceLocation getName() {
        return name;
    }

    @Override
    public void register() {
        ConfigSpec.saveSpec(this);
    }

    @Override
    public void loadFromFile() {
        JsonElement config = null;

        if (file.exists() && file.isFile()) {
            try (FileInputStream fileInputStream = new FileInputStream(file);
                 InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
                 BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {

                config = GSON.fromJson(bufferedReader, JsonElement.class);
            } catch (IOException e) {
                throw new RuntimeException("Failed to load config", e);
            }
        }

        if (config instanceof JsonObject jo) {
            //dont call load directly so we skip the main category name
            mainEntry.getEntries().forEach(e -> e.loadFromJson(jo));
        }
    }

    public void saveConfig() {
        try (FileOutputStream stream = new FileOutputStream(this.file);
             Writer writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8)) {

            JsonObject jo = new JsonObject();
            mainEntry.getEntries().forEach(e -> e.saveToJson(jo));

            GSON.toJson(jo, writer);
        } catch (IOException ignored) {
        }
    }

    public String getTitleKey() {
        return "config." + this.getName().toLanguageKey();
    }


    private static final boolean hasScreen = PlatformHelper.isModLoaded("cloth_config");

    @Override
    @Environment(EnvType.CLIENT)
    public Screen makeScreen(Screen parent, ResourceLocation background) {
        if (hasScreen) {
            return ClothConfigCompat.makeScreen(parent, this, background);
        }
        return null;
    }
}
