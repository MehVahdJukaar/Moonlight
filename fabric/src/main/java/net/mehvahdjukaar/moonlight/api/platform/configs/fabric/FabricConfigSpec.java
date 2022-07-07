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
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigSpec;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigType;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class FabricConfigSpec extends ConfigSpec {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final Map<String, Map<ConfigType, FabricConfigSpec>> CONFIG_STORAGE = new HashMap<>();
    private final ResourceLocation res;

    public static void saveSpec(FabricConfigSpec spec) {
        var map = CONFIG_STORAGE.computeIfAbsent(spec.getModId(), n -> new HashMap<>());
        map.put(spec.getConfigType(), spec);
    }

    @Nullable
    public static FabricConfigSpec getSpec(String modId, ConfigType type) {
        var map = CONFIG_STORAGE.get(modId);
        if (map != null) {
            return map.getOrDefault(type, null);
        }
        return null;
    }

    private final ConfigCategory mainEntry;
    private final File file;

    public FabricConfigSpec(ResourceLocation name, ConfigCategory mainEntry, ConfigType type) {
        this(name, mainEntry, type, false);
    }

    public FabricConfigSpec(ResourceLocation name, ConfigCategory mainEntry, ConfigType type, boolean synced) {
        super(name, FabricLoader.getInstance().getConfigDir(), type, synced);
        this.file = this.getFullPath().toFile();
        this.mainEntry = mainEntry;
        this.res = name;
    }

    public ConfigCategory getMainEntry() {
        return mainEntry;
    }

    @Override
    public void register() {
        FabricConfigSpec.saveSpec(this);
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
        return "config." + this.res.toLanguageKey();
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
