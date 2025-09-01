package net.mehvahdjukaar.moonlight.api.resources.pack;

import net.mehvahdjukaar.moonlight.api.resources.RPUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.resources.IoSupplier;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class CacheBackedPackResources extends PathPackResources implements IEditablePackResources {

    private final Path path;
    private final PackMetadataSection metadata;
    private final PackType packType;
    private final Set<String> namespaces = new HashSet<>();

    public CacheBackedPackResources(PackLocationInfo location, PackType type, PackMetadataSection metadata, Path path) {
        super(location, path);
        this.metadata = metadata;
        this.path = path;
        this.packType = type;
    }

    @Override
    public Set<String> getNamespaces(PackType type) {
        if (type != this.packType) return Set.of();
        return namespaces;
    }

    @Override
    public void listResources(PackType packType, String namespace, String path, ResourceOutput resourceOutput) {
        if (packType != this.packType) return;
        super.listResources(packType, namespace, path, resourceOutput);
    }

    @Override
    public @Nullable IoSupplier<InputStream> getResource(PackType packType, ResourceLocation location) {
        if (packType != this.packType) return null;
        return super.getResource(packType, location);
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
        //no op
    }

    @Override
    public void addResource(ResourceLocation id, byte[] bytes) {
        RPUtils.writeResource(id, bytes, path, this.packType);
    }

    @Override
    public void removeResource(ResourceLocation id) {
        Path resPath = RPUtils.getResourcePath(path, id, this.packType);
        try {
            Files.deleteIfExists(resPath);
        } catch (Exception ignored) {
        }
    }

    @Override
    public void removeRootResource(String name) {
        //no op
    }

    @Override
    public void clearAllResources() {
        //delete the whole folder
        try {
            Files.deleteIfExists(path);
        } catch (Exception ignored) {
        }
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
