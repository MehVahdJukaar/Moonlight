package net.mehvahdjukaar.moonlight.api.util;

import net.mehvahdjukaar.moonlight.core.Moonlight;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

public class FilesHelper {

    public static FastCachedWriter fastCacheWriter() {
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
                Moonlight.LOGGER.info("Deleting directory: {}", path);
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
                    } catch (Exception ignored) {
                    }
                }).start();

            } else if (Files.exists(path)) {
                Moonlight.LOGGER.info("Deleting file: {}", path);
                // Regular file deletion
                Files.deleteIfExists(path);
            }
        } catch (Exception e) {
            return false;
        }

        return true;
    }


    @FunctionalInterface
    public interface IOConsumer<T> {
        void accept(T t) throws IOException;
    }


    public static void writeAtomically(Path target, IOConsumer<OutputStream> writeLogic) throws IOException {
        Path dir = target.getParent();
        if (dir == null) {
            dir = Paths.get(System.getProperty("java.io.tmpdir"));
        } else {
            Files.createDirectories(dir);
        }

        Path temp = Files.createTempFile(dir, target.getFileName().toString(), ".tmp");

        try {
            // Write data
            try (OutputStream out = Files.newOutputStream(temp, StandardOpenOption.WRITE)) {
                writeLogic.accept(out);
                out.flush();
            }

            // fsync file contents
            try (FileChannel fc = FileChannel.open(temp, StandardOpenOption.WRITE)) {
                fc.force(true);
            }

            // Atomic replace
            Files.move(temp, target,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);

            // fsync directory (best effort)
            try (FileChannel dirFc = FileChannel.open(dir, StandardOpenOption.READ)) {
                dirFc.force(true);
            } catch (Exception ignored) {
            }

        } catch (Exception e) {
            Files.deleteIfExists(temp);
            throw e;
        }
    }

    public static void writeTextAtomically(Path target,
                                 IOConsumer<Writer> writeLogic) throws IOException {
        writeAtomically(target, out -> {
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8))) {
                writeLogic.accept(writer);
                writer.flush();
            }
        });
    }
}
