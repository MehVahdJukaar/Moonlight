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
            writer.writeFast(path, bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removeResource(ResourceLocation id) {
        Path resPath = RPUtils.getResourcePath(path, id, this.packType);
        try {
            FileUtils.deleteDirectory(resPath.toFile());
        } catch (Exception e) {
            Moonlight.LOGGER.warn("Failed to delete resource {}", id, e);
        }
    }

    @Override
    public void removeRootResource(String name) {
        //no op
    }

    @Override
    public boolean clearAllResources() {
        //delete the whole folder
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            writer.clear();
            FileUtils.deleteDirectory(path.toFile());
        } catch (Exception e) {
            Moonlight.LOGGER.warn("Failed to clear cache pack resources at {}", path, e);
        }
        Moonlight.LOGGER.info("Cleared cache pack resources at {} in {}", path, stopwatch);
        return true;
    }

    @Override
    public boolean initializeIfValid() {
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


    private final class SafeWriter {
        private final Set<Path> dirCache = ConcurrentHashMap.newKeySet();

        public void writeFast(Path path, byte[] bytes) throws IOException {
            final Path parent = path.getParent();
            final Path normParent = parent == null ? null : parent.toAbsolutePath().normalize();

            // Fast path: create once per unique parent
            if (normParent != null && dirCache.add(normParent)) {
                Files.createDirectories(normParent);
            }

            int attempts = 0;
            while (true) {
                try {
                    Files.write(path, bytes,
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
