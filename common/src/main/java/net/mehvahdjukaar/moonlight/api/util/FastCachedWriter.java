package net.mehvahdjukaar.moonlight.api.util;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class FastCachedWriter {
    private final Set<Path> dirCache = ConcurrentHashMap.newKeySet();

    public void writeFast(Path filePath, byte[] bytes) throws IOException {
        final Path parent = filePath.getParent();
        final Path normParent = (parent == null) ? null : parent.toAbsolutePath().normalize();

        // Fast path: create the parent directory once per unique parent
        if (normParent != null && dirCache.add(normParent)) {
            Files.createDirectories(normParent);
        }

        int attempts = 0;
        while (true) {
            try {
                Files.write(filePath, bytes,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING);
                return; // success
            } catch (NoSuchFileException e) {
                // Parent likely deleted between calls → recreate and retry
                if (normParent == null || ++attempts > 2) throw e;
                Files.createDirectories(normParent);
                dirCache.add(normParent); // refresh cache after recreation
                // loop and retry
            }
        }
    }

    public void clear() {
        dirCache.clear();
    }
}
