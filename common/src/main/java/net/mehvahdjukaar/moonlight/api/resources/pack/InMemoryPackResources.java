package net.mehvahdjukaar.moonlight.api.resources.pack;

import dev.architectury.injectables.annotations.PlatformOnly;
import net.mehvahdjukaar.moonlight.api.misc.ResourceLocationSearchTrie;
import net.mehvahdjukaar.moonlight.api.resources.RPUtils;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.resources.IoSupplier;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryPackResources extends AbstractPackResources implements IEditablePackResources, IDebugDumpable {

    protected final boolean hidden;
    protected final PackType packType;
    protected final PackMetadataSection metadata;
    protected final Set<String> namespaces = new HashSet<>();
    protected final Map<ResourceLocation, byte[]> resources = new ConcurrentHashMap<>();
    protected final Map<String, byte[]> rootResources = new ConcurrentHashMap<>();
    protected final ResourceLocationSearchTrie searchTrie = new ResourceLocationSearchTrie();

    protected InMemoryPackResources(PackLocationInfo info, PackType type) {
        this(info, type, false);
    }

    protected InMemoryPackResources(PackLocationInfo info, PackType type, boolean hidden) {
        super(info);
        this.packType = type;
        this.hidden = hidden;
        this.metadata = new PackMetadataSection(Component.translatable("message.moonlight.runtime"),
                SharedConstants.getCurrentVersion().getPackVersion(packType), Optional.empty());

    }


    @PlatformOnly(PlatformOnly.FORGE)
    public boolean isHidden() {
        return this.hidden;
    }

    @Override
    public Set<String> getNamespaces(PackType packType) {
        if (packType != this.packType) return Set.of();
        return this.namespaces;
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

    @Nullable
    @Override
    public IoSupplier<InputStream> getRootResource(String... strings) {
        String fileName = String.join("/", strings);
        byte[] resource = this.rootResources.get(fileName);
        return resource == null ? null : () -> new ByteArrayInputStream(resource);
    }

    @Override
    public void listResources(PackType packType, String namespace, String id, ResourceOutput output) {
        //why are we only using server resources here?
        if (packType == this.packType) {
            //idk why but somebody had an issue with concurrency here during world load
            synchronized (this) {
                this.searchTrie .search(namespace + "/" + id)
                        .forEach(r -> {
                            byte[] buf = resources.get(r);
                            output.accept(r, () -> {
                                if (buf == null) {
                                    throw new IllegalStateException("Somehow search tree returned a resource not in resources " + r);
                                }
                                return new ByteArrayInputStream(buf);
                            });
                        });
            }
        }
    }

    @Override
    public IoSupplier<InputStream> getResource(PackType type, ResourceLocation id) {

        var res = this.resources.get(id);
        if (res != null) {
            return () -> {
                if (type != this.packType) {
                    throw new IOException(String.format("Tried to access wrong type of resource on %s.", this.packId()));
                }
                return new ByteArrayInputStream(res);
            };
        }
        return null;
    }

    @Override
    public void close() {
        // do not clear after reloading texture packs. should always be on
    }


    /**
     * Dynamic textures are loaded after getNamespaces is called, so unfortunately we need to know those in advance
     * Call this if you are adding stuff for another mod namespace
     **/
    @Override
    public void addNamespaces(String... namespaces) {
        this.namespaces.addAll(Arrays.asList(namespaces));
    }

    @Override
    public void addRootResource(String name, byte[] resource) {
        this.rootResources.put(name, resource);
    }

    @Override
    public void addResource(ResourceLocation id, byte[] bytes) {
        synchronized (this) {
            this.namespaces.add(id.getNamespace());
            this.resources.put(id, bytes);
            this.searchTrie.insert(id);
        }
    }

    @Override
    public void removeResource(ResourceLocation id) {
        synchronized (this) {
            this.resources.remove(id);
            this.searchTrie.remove(id);
        }
    }

    @Override
    public void removeRootResource(String name) {
        this.rootResources.remove(name);
    }

    @Override
    public boolean clearAllResources() {
        synchronized (this) {
            this.resources.clear();
            this.rootResources.clear();
            this.searchTrie.clear();
        }
        return true;
    }

    @Override
    public PackType getPackType() {
        return packType;
    }

    @Override
    public boolean isEmpty() {
        return this.resources.isEmpty();
    }

    @Override
    public void dumpToDisk(Path path) {
        this.resources.forEach((k, v) -> {
            try {
                Path p = RPUtils.getResourcePath(path, k, packType);
                Files.createDirectories(p.getParent());
                Files.write(p, v, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
            } catch (IOException ignored) {
            }
        });
    }


}
