package net.mehvahdjukaar.selene.resourcepack;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import javax.annotation.Nullable;
import java.io.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

//resource manager that only contains vanilla stuff
public class VanillaResourceManager implements ResourceManager, Closeable {

    private final PackType TYPE = PackType.CLIENT_RESOURCES;
    private final Map<String, PackResources> packs = new HashMap<>();

    public VanillaResourceManager() {
        PackRepository repository = Minecraft.getInstance().getResourcePackRepository();
        var v = repository.getPack("vanilla");
        if (v != null) packs.put("vanilla", v.open());
        var m = repository.getPack("mod_resources");
        if (m != null) packs.put("mod_resources", m.open());
    }

    @Override
    public Set<String> getNamespaces() {
        return packs.keySet();
    }

    @Override
    public boolean hasResource(ResourceLocation location) {
        return packs.values().stream().anyMatch(p -> p.hasResource(TYPE, location));
    }

    @Override
    public List<Resource> getResources(ResourceLocation p_10730_) throws IOException {
        throw new IOException("Operation not supported");
    }

    @Override
    public Collection<ResourceLocation> listResources(String p_10726_, Predicate<String> p_10727_) {
        return Collections.emptySet();
    }

    @Override
    public Stream<PackResources> listPacks() {
        return this.packs.values().stream();
    }

    @Override
    public Resource getResource(ResourceLocation location) throws IOException {
        for (var p : packs.values()) {
            if (p.hasResource(TYPE, location)) {
                //code from VanillaPackResources
                return new Resource() {
                    @Nullable
                    InputStream inputStream;

                    public void close() throws IOException {
                        if (this.inputStream != null) {
                            this.inputStream.close();
                        }

                    }

                    public ResourceLocation getLocation() {
                        return location;
                    }

                    public InputStream getInputStream() {
                        try {
                            this.inputStream = p.getResource(TYPE, location);
                        } catch (IOException ioexception) {
                            throw new UncheckedIOException("Could not get client resource from vanilla pack", ioexception);
                        }

                        return this.inputStream;
                    }

                    public boolean hasMetadata() {
                        return false;
                    }

                    @Nullable
                    public <T> T getMetadata(MetadataSectionSerializer<T> p_143773_) {
                        return (T) null;
                    }

                    public String getSourceName() {
                        return location.toString();
                    }
                };
            }
        }
        throw new FileNotFoundException(location.toString());
    }

    @Override
    public void close(){
        this.packs.values().forEach(PackResources::close);
    }
}
