package net.mehvahdjukaar.moonlight.api.util;

import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FilesHelper {

    public static FastCachedWriter fastCacheWriter(){
        return new FastCachedWriter();
    }

    /**
     * Deletes or clears a file/directory at the given path.
     * If it's a directory, it is moved to a temp location for fast removal and deleted asynchronously.
     * If it's a file, it is deleted immediately.
     *
     * @param path the path to remove
     * @return true if the operation was initiated successfully, false on error
     */
    public static boolean fastRemove(Path path) {
        if (path == null || !Files.exists(path)) return true;

        try {
            if (Files.isDirectory(path)) {
                // Move to a temporary location for fast removal
                Path tempPath = path.resolveSibling(
                        path.getFileName() + "_temp_deleting_" + System.currentTimeMillis()
                );
                try {
                    Files.move(path, tempPath);
                } catch (IOException e) {
                    // fallback if move fails
                    Files.move(path, tempPath);
                }

                // Delete asynchronously
                new Thread(() -> {
                    try {
                        FileUtils.deleteDirectory(tempPath.toFile());
                    } catch (Exception ignored) {}
                }).start();

            } else {
                // Regular file deletion
                Files.deleteIfExists(path);
            }
        } catch (Exception e) {
            return false;
        }

        return true;
    }
}
