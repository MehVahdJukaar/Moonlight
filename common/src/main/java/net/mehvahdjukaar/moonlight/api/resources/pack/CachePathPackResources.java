package net.mehvahdjukaar.moonlight.api.resources.pack;

import com.google.common.base.Stopwatch;
import net.mehvahdjukaar.moonlight.api.resources.RPUtils;
import net.mehvahdjukaar.moonlight.core.CommonConfigs;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class CachePathPackResources extends AbstractCachedEditableResources {

    private final SafeWriter writer = new SafeWriter();

    public CachePathPackResources(PackLocationInfo location, PackType type, Path path) {
        super(path, location, type, Component.translatable("message.moonlight.cached"));
    }

    @Override
    public void addResource(ResourceLocation id, byte[] bytes) {
        try {
            // Write to the actual resource file path (not the root directory)
            Path resPath = RPUtils.getResourcePath(this.path, id, this.packType);
            writer.writeFast(resPath, bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removeResource(ResourceLocation id) {
        Path resPath = RPUtils.getResourcePath(this.path, id, this.packType);
        try {
            deleteRecursively(resPath);
        } catch (Exception e) {
            Moonlight.LOGGER.warn("Failed to delete resource {}", id, e);
        }
    }

    @Override
    public void removeRootResource(String name) {
        // no-op
    }

    @Override
    public boolean clearAllResources() {
        // delete the whole folder (or heal if a stray file exists at root)
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            writer.clear();
            if (Files.isDirectory(path)) {
                FileUtils.deleteDirectory(path.toFile());
            } else {
                // old/bad state: root path is a file; just delete it
                Files.deleteIfExists(path);
            }
        } catch (Exception e) {
            Moonlight.LOGGER.warn("Failed to clear cache pack resources at {}", path, e);
            return false;
        }
        Moonlight.LOGGER.info("Cleared cache pack resources at {} in {}", path, stopwatch);
        return true;
    }

    @Override
    public boolean initializeIfValid() {
        // Heal state where the root path was accidentally created as a file
        if (Files.exists(path) && !Files.isDirectory(path)) {
            try {
                Files.delete(path);
            } catch (IOException e) {
                Moonlight.LOGGER.warn("Failed to remove invalid cache file at {}", path, e);
                return false;
            }
        }

        boolean dirExists = Files.isDirectory(path);
        if (dirExists) {
            if (CommonConfigs.FASTER_CACHE_SEARCH.get()) {
                this.cachedResources = new FastSearchPathPackResources(locationInfo, path, packType);
            } else {
                this.cachedResources = new PathPackResources.PathResourcesSupplier(path)
                        .openPrimary(this.locationInfo);
            }
        }
        return dirExists;
    }

    @Override
    public void commitChanges() {
        initializeIfValid();
    }

    @Override
    public PackType getPackType() {
        return packType;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    private static void deleteRecursively(Path p) throws IOException {
        if (Files.isDirectory(p)) {
            FileUtils.deleteDirectory(p.toFile());
        } else {
            Files.deleteIfExists(p);
        }
    }

    private final class SafeWriter {
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
}
