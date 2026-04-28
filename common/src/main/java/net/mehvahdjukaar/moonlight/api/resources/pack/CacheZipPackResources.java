package net.mehvahdjukaar.moonlight.api.resources.pack;

import com.google.common.base.Stopwatch;
import net.mehvahdjukaar.moonlight.api.util.FilesHelper;
import net.mehvahdjukaar.moonlight.core.CommonConfigs;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackType;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class CacheZipPackResources extends AbstractCachedEditableResources {

    private final Map<ResourceLocation, byte[]> tempResources = new ConcurrentHashMap<>();
    private volatile boolean dirty = false;

    public CacheZipPackResources(PackLocationInfo location, PackType type, Path path) {
        super((!path.getFileName().toString().endsWith(".zip"))
                        ? path.resolveSibling(path.getFileName() + ".zip")
                        : path,
                location, type, Component.translatable("message.moonlight.cached_zipped"));
    }

    @Override
    public void addResource(ResourceLocation id, byte[] bytes) {
        this.tempResources.put(id, bytes);
        this.dirty = true;
    }

    @Override
    public void removeResource(ResourceLocation id) {
        this.tempResources.remove(id);
        this.dirty = true;
    }

    @Override
    public void removeRootResource(String name) {
        // no-op for zipped cache
    }

    @Override
    public boolean clearAllResources() {
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            if (this.cachedResources != null) {
                this.cachedResources.close();
            }
            this.cachedResources = null;
            FilesHelper.fastRemove(path);
        } catch (Exception e) {
            Moonlight.LOGGER.warn("Failed to clear zipped cached resource pack at {}", path, e);
        }
        boolean gone = !Files.exists(path);
        if (!gone) {
            Moonlight.LOGGER.error("Failed to delete cached resource pack at {}", path);
        }
        Moonlight.LOGGER.info("Cleared zipped cached resource pack at {} in {}", path, stopwatch);
        return gone;
    }

    @Override
    public boolean initializeIfValid() {
        // Heal: if a directory exists where the zip should be, remove it so we can write/open the zip
        if (Files.exists(path) && Files.isDirectory(path)) {
            FilesHelper.fastRemove(path);
        }

        boolean cacheExists = Files.isRegularFile(path);
        if (cacheExists) {
            if (CommonConfigs.FASTER_CACHE_SEARCH.get()) {
                this.cachedResources = new FastSearchFilePackResources(locationInfo, this.path.toFile(), packType);
            } else {
                this.cachedResources = new FilePackResources.FileResourcesSupplier(path.toFile())
                        .openPrimary(this.locationInfo);
            }
        }
        return cacheExists;
    }

    @Override
    public PackType getPackType() {
        return packType;
    }

    @Override
    public boolean isEmpty() {
        // Keep existing behavior (always false), or implement a real check if desired:
        // return !Files.exists(path) && tempResources.isEmpty();
        return false;
    }

    @Override
    public void commitChanges() {
        if (!dirty) return;

        dirty = false;

        // If somehow still open, force clear before rewriting the zip
        if (cachedResources != null) {
            Moonlight.LOGGER.error("Zip file resources was not cleared before commit. Clearing now.");
            if (!clearAllResources()) {
                throw new RuntimeException("Could not clear resources before writing zip");
            }
        }

        try {
            Stopwatch stopwatch = Stopwatch.createStarted();
            writeZipPreferStored(tempResources, path);
            this.tempResources.clear();
            this.initializeIfValid();
            Moonlight.LOGGER.info("Wrote cached resource pack to {} in {}", path, stopwatch);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ---- Zip writing helpers ----

    public void writeZipPreferStored(Map<ResourceLocation, byte[]> files, Path outputZip) throws IOException {

        try {
            // Try STORED first
            FilesHelper.writeAtomically(outputZip, out -> {
                try (ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(out))) {
                    writeEntriesStored(zos, files);
                }
            });

        } catch (Exception storedEx) {
            Moonlight.LOGGER.warn("Could not write zip using STORED; falling back to DEFLATED: {}", String.valueOf(storedEx));

            // Fallback to DEFLATED (no compression)
            FilesHelper.writeAtomically(outputZip, out -> {
                try (ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(out))) {
                    writeEntriesDeflated(zos, files, Deflater.NO_COMPRESSION);
                }
            });
        }
    }

    private void writeEntriesStored(ZipOutputStream zos, Map<ResourceLocation, byte[]> files) throws IOException {
        for (var e : files.entrySet()) {
            String name = packType.getDirectory() + "/" +
                    e.getKey().toString().replace(':', '/').replace('\\', '/');

            byte[] data = e.getValue();
            CRC32 crc = new CRC32();
            crc.update(data);

            ZipEntry ze = new ZipEntry(name);
            ze.setMethod(ZipEntry.STORED);
            ze.setSize(data.length);
            ze.setCompressedSize(data.length);
            ze.setCrc(crc.getValue());
            // ze.setTime(0L); // optional: reproducible builds

            zos.putNextEntry(ze);
            zos.write(data);
            zos.closeEntry();
        }
    }

    private void writeEntriesDeflated(ZipOutputStream zos, Map<ResourceLocation, byte[]> files, int level) throws IOException {
        zos.setLevel(level);
        for (var e : files.entrySet()) {
            String name = packType.getDirectory() + "/" +
                    e.getKey().toString().replace(':', '/').replace('\\', '/');

            ZipEntry ze = new ZipEntry(name);
            // ze.setTime(0L); // optional: reproducible builds
            zos.putNextEntry(ze);
            zos.write(e.getValue());
            zos.closeEntry();
        }
    }
}
