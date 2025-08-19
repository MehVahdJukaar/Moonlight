package net.mehvahdjukaar.moonlight.core.set;

import net.mehvahdjukaar.moonlight.api.set.BlockSetAPI;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class DebugBlockTypes {

    private static final Path debugDir = Paths.get("debug", "dynamic_registry_dump");

    public static void writeToFile() {
        try {
            Files.createDirectories(debugDir);

            for (var reg : BlockSetAPI.getRegistries()) {
                String registryName = reg.typeName().replace(":", "_");
                Path filePath = debugDir.resolve(registryName + ".txt");

                StringBuilder builder = new StringBuilder();

                // Step 1: list all block types
                for (var entry : reg.getValues()) {
                    builder.append(entry.getId().toString())
                            .append(System.lineSeparator());
                }

                builder.append(System.lineSeparator()).append(System.lineSeparator());

                // Step 1.5: collect all possible child keys
                Set<String> allChildKeys = new TreeSet<>(); // TreeSet = alphabetical order
                for (var entry : reg.getValues()) {
                    allChildKeys.addAll(entry.getChildren().stream()
                            .map(Map.Entry::getKey)
                            .toList());
                }

                // Step 2: list children for each block type in deterministic order
                for (var entry : reg.getValues()) {
                    builder.append("[").append(entry.getId().toString()).append("]").append(System.lineSeparator());

                    if (allChildKeys.isEmpty()) {
                        builder.append("  (no children)").append(System.lineSeparator());
                    } else {
                        for (String key : allChildKeys) {
                            Object value = entry.getChild(key);
                            builder.append("  - ").append(key).append(" = ")
                                    .append(value != null ? formatValue(value) : "MISSING")
                                    .append(System.lineSeparator());
                        }
                    }

                    builder.append(System.lineSeparator()); // spacing between block types
                }

                Files.writeString(filePath, builder.toString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            }

        } catch (IOException e) {
            Moonlight.LOGGER.error("Failed to write registry debug dump: {}", e.toString());
        }
    }

    private static Object formatValue(Object child) {
        if (child instanceof Item i) {
            return "Item{" + BuiltInRegistries.ITEM.getKey(i) + "}";
        }
        return child.toString();
    }
}
