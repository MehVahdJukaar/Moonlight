package net.mehvahdjukaar.moonlight.api.resources.pack;

import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.resources.IoSupplier;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractCachedEditableResources implements PackResources, IEditablePackResources {

    protected final Path path;
    protected final PackLocationInfo locationInfo;
    protected final PackMetadataSection metadata;
    protected final PackType packType;
    protected final Set<String> namespaces = new HashSet<>();
    protected final Map<String, byte[]> rootResources = new ConcurrentHashMap<>();

    protected PackResources cachedResources = null;

    public AbstractCachedEditableResources(Path path, PackLocationInfo locationInfo, PackType packType, Component description) {
        this.path = path;
        this.locationInfo = locationInfo;
        this.packType = packType;
        this.metadata = new PackMetadataSection(description,
                SharedConstants.getCurrentVersion().packVersion(packType).minorRange());
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
        if (cachedResources == null) {
            return;
        }
        this.cachedResources.listResources(packType, namespace, path, resourceOutput);
    }

    @Override
    public @Nullable IoSupplier<InputStream> getResource(PackType packType, Identifier location) {
        if (packType != this.packType) return null;
        if (cachedResources == null) {
            return null;
        }
        return this.cachedResources.getResource(packType, location);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getMetadataSection(MetadataSectionType<T> type) {
        try {
            return type == PackMetadataSection.forPackType(this.packType) ? (T) this.metadata : null;
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
    public void close() {
        if (this.cachedResources != null) {
            this.cachedResources.close();
        }
    }

}
