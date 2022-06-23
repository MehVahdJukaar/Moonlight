package net.mehvahdjukaar.moonlight.resources;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

//resource manager that only contains vanilla stuff
public class VanillaResourceManager implements ResourceManager, Closeable {

    private final PackType TYPE = PackType.CLIENT_RESOURCES;
    private final Map<String, PackResources> packs = new HashMap<>();

    public VanillaResourceManager(PackRepository repository) {
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
    public Stream<PackResources> listPacks() {
        return this.packs.values().stream();
    }

    @Override
    public Optional<Resource> getResource(ResourceLocation location) {
        for (var p : packs.values()) {
            if (p.hasResource(TYPE, location)) {
                return Optional.of(new Resource(p.getName(), () -> p.getResource(TYPE, location)));
            }
        }
        return Optional.empty();
    }
    @Override
    public void close(){
        this.packs.values().forEach(PackResources::close);
    }


    //old getResources
    @Override
    public List<Resource> getResourceStack(ResourceLocation p_215562_) {
        return List.of();
    }

    @Override
    public Map<ResourceLocation, Resource> listResources(String p_215563_, Predicate<ResourceLocation> p_215564_) {
        return Collections.emptyMap();
    }

    //old listResources
    @Override
    public Map<ResourceLocation, List<Resource>> listResourceStacks(String p_215565_, Predicate<ResourceLocation> p_215566_) {
        return Collections.emptyMap();
    }
}
