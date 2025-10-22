package net.mehvahdjukaar.moonlight.api.resources.pack;

import com.google.common.base.Stopwatch;
import net.mehvahdjukaar.moonlight.core.CommonConfigs;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackType;
import org.apache.commons.io.FileUtils;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.*;
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

            if (Files.isDirectory(path)) {
                // heal accidental directory-at-zip-path case
                FileUtils.deleteDirectory(path.toFile());
            } else {
                Files.deleteIfExists(path);
            }
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
            try {
                FileUtils.deleteDirectory(path.toFile());
            } catch (IOException e) {
                Moonlight.LOGGER.warn("Could not remove directory at zip path {}: {}", path, e.toString());
                return false;
            }
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
        Path parent = outputZip.getParent();
        if (parent == null) {
            parent = Paths.get(System.getProperty("java.io.tmpdir"));
        } else {
            Files.createDirectories(parent);
        }

        Path tmp = Files.createTempFile(parent, "dynpack-", ".zip");
        boolean moved = false;
        try {
            // Try STORED first (requires size & CRC)
            try (OutputStream os = new BufferedOutputStream(Files.newOutputStream(tmp, StandardOpenOption.TRUNCATE_EXISTING));
                 ZipOutputStream zos = new ZipOutputStream(os)) {
                writeEntriesStored(zos, files);
            }
            moveIntoPlace(tmp, outputZip);
            moved = true;
        } catch (Exception storedEx) {
            Moonlight.LOGGER.warn("Could not write zip using STORED; falling back to DEFLATED: {}", String.valueOf(storedEx));
            try (OutputStream os = new BufferedOutputStream(Files.newOutputStream(tmp, StandardOpenOption.TRUNCATE_EXISTING));
                 ZipOutputStream zos = new ZipOutputStream(os)) {
                writeEntriesDeflated(zos, files, Deflater.NO_COMPRESSION);
            }
            moveIntoPlace(tmp, outputZip);
            moved = true;
        } finally {
            if (!moved) {
                try { Files.deleteIfExists(tmp); } catch (IOException ignored) {}
            }
        }
    }

    private void moveIntoPlace(Path tmp, Path target) throws IOException {
        try {
            Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException ex) {
            Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING);
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
