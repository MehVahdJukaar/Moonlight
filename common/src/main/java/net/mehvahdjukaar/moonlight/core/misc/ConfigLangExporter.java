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

// Fills a mod's en_us.json with the config keys it doesn't have yet, so options and their comments are visible to
// translators without anyone typing them out. Dev only, and it never touches a key that is already there.
public class ConfigLangExporter {

    // names Moonlight makes up on a mod's behalf: a feature toggle, and the hidden parts of a range or a vec3. The
    // mod author never typed these, so Moonlight translates them and they stay out of the mod's lang file
    public static final Set<String> MOONLIGHT_NAMES = Set.of(
            ConfigBuilder.FEATURE_TOGGLE_NAME, "min", "max", "x", "y", "z");

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final boolean ENABLED = !"false".equals(System.getProperty("moonlight.langExport"));

    public static void exportInDev(String modId, Map<String, String> translations, Map<String, String> moonlightNames) {
        if (!ENABLED || !PlatHelper.isDev()) return;
        Map<String, String> wanted = new LinkedHashMap<>();
        translations.forEach((key, value) -> {
            if (!moonlightNames.containsKey(key)) wanted.put(key, value);
        });
        if (wanted.isEmpty()) return;
        try {
            Path file = findLangFile(modId);
            if (file != null) merge(file, wanted);
        } catch (Exception e) {
            Moonlight.LOGGER.warn("Failed to export config lang keys for mod {}", modId, e);
        }
    }

    private static void merge(Path file, Map<String, String> wanted) throws Exception {
        JsonObject json = new JsonObject();
        if (Files.exists(file)) {
            try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                JsonElement parsed = JsonParser.parseReader(reader);
                if (parsed.isJsonObject()) json = parsed.getAsJsonObject();
            }
        }
        int added = 0;
        for (var e : wanted.entrySet()) {
            if (json.has(e.getKey())) continue;
            json.addProperty(e.getKey(), e.getValue());
            added++;
        }
        if (added == 0) return;
        Files.createDirectories(file.getParent());
        Files.writeString(file, GSON.toJson(json) + "\n", StandardCharsets.UTF_8);
        Moonlight.LOGGER.info("Added {} missing config lang entries to {}", added, file);
    }

    // the compiled resources of a mod being developed live in a build folder next to its sources, so the source file
    // can be walked back to. A mod loaded from a jar has no sources here and is skipped
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

        // in a multi loader setup the lang file usually sits in the common module, not in the one that was loaded
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
            if (Files.exists(c.resolve(langPath))) return c.resolve(langPath);
        }
        Path own = candidates.getFirst();
        return Files.isDirectory(own) ? own.resolve(langPath) : null;
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
            for (int i = resource.split("/").length; i > 0 && path != null; i--) {
                path = path.getParent();
            }
            return path;
        } catch (Exception e) {
            return null;
        }
    }
}
