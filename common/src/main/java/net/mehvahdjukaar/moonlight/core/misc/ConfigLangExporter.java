package net.mehvahdjukaar.moonlight.core.misc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigBuilder;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

// A hack that allows us to automatically populate en_us with config lang names when booting
public class ConfigLangExporter {

    public static final Set<String> BUILTIN_NAMES = Set.of(
            ConfigBuilder.FEATURE_TOGGLE_NAME, "min", "max", "x", "y", "z");

    private static final String ENABLED_PROPERTY = "moonlight.langExport";

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final boolean ENABLED = Boolean.parseBoolean(System.getProperty(ENABLED_PROPERTY, "true"));

    public static void exportInDev(String modId, Map<String, String> translations, Map<String, String> alreadyTranslated) {
        if (!ENABLED || !PlatHelper.isDev()) return;
        Map<String, String> missing = new LinkedHashMap<>();
        translations.forEach((key, name) -> {
            if (!alreadyTranslated.containsKey(key)) missing.put(key, name);
        });
        if (missing.isEmpty()) return;
        try {
            Path langFile = findLangFile(modId);
            if (langFile != null) addMissingEntries(langFile, missing);
        } catch (Exception e) {
            Moonlight.LOGGER.warn("Failed to export config lang keys for mod {}", modId, e);
        }
    }

    private static void addMissingEntries(Path langFile, Map<String, String> missing) throws Exception {
        JsonObject json = new JsonObject();
        if (Files.exists(langFile)) {
            try (BufferedReader reader = Files.newBufferedReader(langFile, StandardCharsets.UTF_8)) {
                JsonElement parsed = JsonParser.parseReader(reader);
                if (parsed.isJsonObject()) json = parsed.getAsJsonObject();
            }
        }
        int added = 0;
        for (var e : missing.entrySet()) {
            if (json.has(e.getKey())) continue;
            json.addProperty(e.getKey(), e.getValue());
            added++;
        }
        if (added == 0) return;
        Files.createDirectories(langFile.getParent());
        Files.writeString(langFile, GSON.toJson(json) + "\n", StandardCharsets.UTF_8);
        Moonlight.LOGGER.info("Added {} missing config lang entries to {}", added, langFile);
    }

    @Nullable
    private static Path findLangFile(String modId) {
        String langPath = "assets/" + modId + "/lang/en_us.json";
        Path root = classpathRoot(langPath);
        if (root == null) root = classpathRoot("assets/" + modId);
        if (root == null) return null;

        Path buildDir = root;
        while (buildDir != null && !isBuildDirName(buildDir.getFileName().toString())) {
            buildDir = buildDir.getParent();
        }
        if (buildDir == null || buildDir.getParent() == null) return null;
        Path module = buildDir.getParent();

        List<Path> candidates = new ArrayList<>();
        candidates.add(module.resolve("src/main/resources"));
        Path parent = module.getParent();
        if (parent != null && Files.isDirectory(parent)) {
            try (Stream<Path> siblings = Files.list(parent)) {
                siblings.filter(Files::isDirectory)
                        .map(s -> s.resolve("src/main/resources"))
                        .forEach(candidates::add);
            } catch (Exception ignored) {
            }
        }
        for (Path c : candidates) {
            if (Files.exists(c.resolve(langPath))) {
                return c.resolve(langPath);
            }
        }
        Path ownResources = candidates.getFirst();
        return Files.isDirectory(ownResources) ? ownResources.resolve(langPath) : null;
    }

    private static boolean isBuildDirName(String name) {
        return name.equals("build") || name.equals("out");
    }

    @Nullable
    private static Path classpathRoot(String resource) {
        URL url = ConfigLangExporter.class.getClassLoader().getResource(resource);
        if (url == null || !"file".equals(url.getProtocol())) return null;
        try {
            Path path = Paths.get(url.toURI());
            int segments = resource.split("/").length;
            for (int i = 0; i < segments && path != null; i++) {
                path = path.getParent();
            }
            return path;
        } catch (Exception e) {
            return null;
        }
    }
}
