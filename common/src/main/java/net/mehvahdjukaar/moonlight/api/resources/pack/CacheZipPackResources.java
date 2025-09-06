package net.mehvahdjukaar.moonlight.api.resources.pack;

import com.google.common.base.Stopwatch;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.resources.IoSupplier;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class CacheZipPackResources implements PackResources, IEditablePackResources {

    private final Path path;
    private final PackMetadataSection metadata;
    private final PackType packType;
    private final Set<String> namespaces = new HashSet<>();
    private final Map<String, byte[]> rootResources = new ConcurrentHashMap<>();
    private final Map<ResourceLocation, byte[]> tempResources = new ConcurrentHashMap<>();
    private final PackLocationInfo locationInfo;

    @Nullable
    private PackResources zipResources;
    private boolean dirty = true;

    public CacheZipPackResources(PackLocationInfo location, PackType type, Path path) {
        if (!path.getFileName().toString().endsWith(".zip")) {
            path = path.resolveSibling(path.getFileName() + ".zip");
        }
        this.locationInfo = location;
        this.path = path;
        this.packType = type;
        this.metadata = new PackMetadataSection(Component.translatable("message.moonlight.cached_zipped"),
                SharedConstants.getCurrentVersion().getPackVersion(packType), Optional.empty());

        if (Files.exists(path)) {
            this.zipResources = new FilePackResources.FileResourcesSupplier(this.path.toFile())
                    .openPrimary(this.locationInfo);
        }

    }

    @Override
    public PackLocationInfo location() {
        return locationInfo;
    }

    @Override
    public Set<String> getNamespaces(PackType type) {
        if (type != this.packType) return Set.of();
        return namespaces;
    }

    @Override
    public void listResources(PackType packType, String namespace, String path, ResourceOutput resourceOutput) {
        if (packType != this.packType) return;
        if (zipResources == null) {
            return;
        }
        this.zipResources.listResources(packType, namespace, path, resourceOutput);
    }

    @Override
    public @Nullable IoSupplier<InputStream> getResource(PackType packType, ResourceLocation location) {
        if (packType != this.packType) return null;
        if (zipResources == null) {
            return null;
        }
        return this.zipResources.getResource(packType, location);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getMetadataSection(MetadataSectionSerializer<T> serializer) {
        try {
            return serializer == PackMetadataSection.TYPE ? (T) this.metadata : null;
        } catch (Exception exception) {
            return null;
        }
    }

    @Override
    public void addNamespaces(String... namespaces) {
        this.namespaces.addAll(Arrays.asList(namespaces));
    }

    @Override
    public void addRootResource(String name, byte[] resource) {
        this.rootResources.put(name, resource);
    }


    @Nullable
    @Override
    public IoSupplier<InputStream> getRootResource(String... strings) {
        String fileName = String.join("/", strings);
        byte[] resource = this.rootResources.get(fileName);
        return resource == null ? null : () -> new ByteArrayInputStream(resource);
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
    public void close() {
        if (zipResources != null) {
            this.zipResources.close();
        }
    }

    @Override
    public void removeRootResource(String name) {
        //no op
    }

    @Override
    public boolean clearAllResources() {
        //delete the whole folder
        try {
            if (zipResources != null) {
                this.zipResources.close();
            }
            this.zipResources = null;
            Files.deleteIfExists(path);
        } catch (Exception ignored) {
        }
        boolean doesntExist = !Files.exists(path);
        if (!doesntExist) {
            Moonlight.LOGGER.error("Failed to delete cached resource pack at {}", path);
        }
        return doesntExist;
    }

    @Override
    public boolean checkPathValidity() {
        return Files.exists(path);
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
            if (zipResources != null) {
                Moonlight.LOGGER.error("Zip fie resources was not cleared. How?");
                if (!clearAllResources()) {
                    throw new RuntimeException("Could not clear resources");
                }
            }
            try {
                Stopwatch stopwatch = Stopwatch.createStarted();
                writeZipNoCompressionDEFLATED(tempResources, path.toFile());
                this.tempResources.clear();
                this.zipResources = new FilePackResources.FileResourcesSupplier(path.toFile())
                        .openPrimary(this.locationInfo);
                Moonlight.LOGGER.info("Wrote cached resource pack to {} in {}", path, stopwatch);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }


    public void writeZipNoCompressionDEFLATED(Map<ResourceLocation, byte[]> files, File outputZip) throws IOException {
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
