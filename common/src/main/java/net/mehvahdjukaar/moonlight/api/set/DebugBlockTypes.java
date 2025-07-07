package net.mehvahdjukaar.moonlight.api.set;

import net.mehvahdjukaar.moonlight.core.Moonlight;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class DebugBlockTypes {

    static String filename = "blocktypes_debug.txt";
    static Path debugFilePath = Paths.get("logs", filename);
    static boolean isDeleted = false;

    public static void appendToDebugFile(String content) {
        // Path to the debug.txt file inside the logs directory

        try {
            Files.createDirectories(debugFilePath.getParent());

            // If file exists, delete it to ensure we start fresh
            if (Files.exists(debugFilePath) && !isDeleted) {
                Files.delete(debugFilePath);
                isDeleted = true;
            }

            // Append content to the file
            Files.write(
                    debugFilePath,
                    (content + System.lineSeparator()).getBytes(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
        } catch (IOException e) {
            Moonlight.LOGGER.error("Failed to write {}: {}", filename, e);
        }
    }

}
