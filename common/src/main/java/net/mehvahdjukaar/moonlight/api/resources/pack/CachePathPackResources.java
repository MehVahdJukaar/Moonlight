package net.mehvahdjukaar.moonlight.api.resources.pack;

import com.google.common.base.Stopwatch;
import net.mehvahdjukaar.moonlight.api.resources.RPUtils;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import org.apache.commons.io.FileUtils;

import java.nio.file.Files;
import java.nio.file.Path;

public class CachePathPackResources extends AbstractCachedEditableResources {

    public CachePathPackResources(PackLocationInfo location, PackType type, Path path) {
        super(path, location, type, Component.translatable("message.moonlight.cached"));
    }

    @Override
    public void addResource(ResourceLocation id, byte[] bytes) {
        RPUtils.writeResource(id, bytes, path, this.packType);
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
            FileUtils.deleteDirectory(path.toFile());
        } catch (Exception e) {
            Moonlight.LOGGER.warn("Failed to clear cache pack resources at {}", path, e);
        }
        Moonlight.LOGGER.info("Cleared cache pack resources at {} in {}", path, stopwatch);
        return true;
    }

    @Override
    public boolean checkValidityAndInitialize() {
        boolean dirExists = Files.isDirectory(path);
        if (dirExists) {
            this.cachedResources = new PathPackResources.PathResourcesSupplier(path)
                    .openPrimary(locationInfo);
        }
        return dirExists;
    }

    @Override
    public PackType getPackType() {
        return packType;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
}
