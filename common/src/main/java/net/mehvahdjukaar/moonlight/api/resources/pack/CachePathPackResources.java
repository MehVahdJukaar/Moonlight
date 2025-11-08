package net.mehvahdjukaar.moonlight.api.resources.pack;

import com.google.common.base.Stopwatch;
import net.mehvahdjukaar.moonlight.api.resources.RPUtils;
import net.mehvahdjukaar.moonlight.api.util.FastCachedWriter;
import net.mehvahdjukaar.moonlight.api.util.FilesHelper;
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
import java.nio.file.Path;
import java.util.concurrent.Executor;

public class CachePathPackResources extends AbstractCachedEditableResources {

    private final FastCachedWriter writer = new FastCachedWriter();

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
            throw new RuntimeException("An error occurred while adding a resource to the dynamic pack: ", e);
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
            FilesHelper.fastRemove(path);
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
            FilesHelper.fastRemove(path);
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

}
