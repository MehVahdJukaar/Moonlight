package net.mehvahdjukaar.moonlight.api.resources.pack;

import com.google.common.base.Stopwatch;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackType;
import org.apache.commons.compress.archivers.zip.ParallelScatterZipCreator;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipMethod;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class CacheZipPackResources extends AbstractCachedEditableResources {

    private final Map<ResourceLocation, byte[]> tempResources = new ConcurrentHashMap<>();

    private boolean dirty = false;

    public CacheZipPackResources(PackLocationInfo location, PackType type, Path path) {
        super((!path.getFileName().toString().endsWith(".zip")) ?
                        path.resolveSibling(path.getFileName() + ".zip") : path,
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
        //no op
    }

    @Override
    public boolean clearAllResources() {
        //delete the whole folder
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            if (cachedResources != null) {
                this.cachedResources.close();
            }
            this.cachedResources = null;
            Files.deleteIfExists(path);
        } catch (Exception ignored) {
        }
        boolean doesntExist = !Files.exists(path);
        if (!doesntExist) {
            Moonlight.LOGGER.error("Failed to delete cached resource pack at {}", path);
        }
        Moonlight.LOGGER.info("Cleared zipped cached resource pack at {} in {}", path, stopwatch);
        return doesntExist;
    }

    @Override
    public boolean checkValidityAndInitialize() {
        //initialize if not valid
        boolean cacheExists = Files.exists(path);
        if (cacheExists) {
            this.cachedResources = new FastSearchFilePackResources(locationInfo, this.path.toFile(), packType);
        }
        return cacheExists;
    }

    @Override
    public PackType getPackType() {
        return packType;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public void commitChanges(Executor executor) {
        if (dirty) {
            dirty = false;
            //idk how this could happen but just in case
            if (cachedResources != null) {
                Moonlight.LOGGER.error("Zip fie resources was not cleared. How?");
                if (!clearAllResources()) {
                    throw new RuntimeException("Could not clear resources");
                }
            }
            try {
                Stopwatch stopwatch = Stopwatch.createStarted();
                writeZipStoredCommons(tempResources, path.toFile());
                this.tempResources.clear();
                this.cachedResources = new FilePackResources.FileResourcesSupplier(path.toFile())
                        .openPrimary(this.locationInfo);
                Moonlight.LOGGER.info("Wrote cached resource pack to {} in {}", path, stopwatch);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }


    public void writeZipStoredCommons(Map<ResourceLocation, byte[]> files, File outputZip) throws IOException, ExecutionException, InterruptedException {
        try (var out = new java.io.FileOutputStream(outputZip)) {
            var scatter = new ParallelScatterZipCreator(); // computes CRC/size for you
            for (var e : files.entrySet()) {
                String name = packType.getDirectory() + "/" +
                        e.getKey().toString().replace(':', '/').replace('\\', '/');

                var zae = new ZipArchiveEntry(name);
                zae.setMethod(ZipMethod.STORED.getCode()); // no compression
                scatter.addArchiveEntry(zae, () -> new ByteArrayInputStream(e.getValue()));
            }

            try (var zipOut = new ZipArchiveOutputStream(out)) {
                scatter.writeTo(zipOut); // entries are emitted with correct size+CRC
            }
        }
    }

    public void writeZipDeflated(Map<ResourceLocation, byte[]> files, File outputZip) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(outputZip);
             BufferedOutputStream bos = new BufferedOutputStream(fos);
             ZipOutputStream zos = new ZipOutputStream(bos)) {

            // No compression, but still uses DEFLATED method (no manual CRC/size needed)
            zos.setLevel(Deflater.NO_COMPRESSION);

            for (var entry : files.entrySet()) {
                String name = packType.getDirectory() + "/" +
                        entry.getKey().toString().replace(':', '/').replace('\\', '/');

                ZipEntry ze = new ZipEntry(name);
                // Optional: make builds reproducible
                // ze.setTime(0L);

                zos.putNextEntry(ze);
                zos.write(entry.getValue());
                zos.closeEntry();
            }
        }
    }

    public void writeUncompressedZipSTORED(Map<ResourceLocation, byte[]> files, File outputZip) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(outputZip);
             BufferedOutputStream bos = new BufferedOutputStream(fos);
             ZipOutputStream zos = new ZipOutputStream(bos)) {

            for (Map.Entry<ResourceLocation, byte[]> entry : files.entrySet()) {

                String path = this.packType.getDirectory() + "/" +
                        entry.getKey().toString().replace(":", "/")
                                .replace("\\", "/"); // Normalize path
                byte[] data = entry.getValue();

                ZipEntry zipEntry = new ZipEntry(path);
                zipEntry.setMethod(ZipEntry.STORED);
                zipEntry.setSize(data.length);
                zipEntry.setCompressedSize(data.length);
                zipEntry.setCrc(computeCRC32(data));

                zos.putNextEntry(zipEntry);
                zos.write(data);
                zos.closeEntry();
            }
        }
    }


    private static long computeCRC32(byte[] data) {
        CRC32 crc = new CRC32();
        crc.update(data);
        return crc.getValue();
    }
}
