package net.mehvahdjukaar.moonlight.api.resources.pack;

import com.google.gson.JsonElement;
import dev.architectury.injectables.annotations.PlatformOnly;
import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.mehvahdjukaar.moonlight.api.resources.RPUtils;
import net.mehvahdjukaar.moonlight.api.resources.ResType;
import net.mehvahdjukaar.moonlight.api.resources.StaticResource;
import net.mehvahdjukaar.moonlight.api.resources.assets.LangBuilder;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.metadata.pack.PackMetadataSectionSerializer;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.world.flag.FeatureFlagSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class DynamicResourcePack implements PackResources {
    protected static final Logger LOGGER = LogManager.getLogger();

    protected final boolean hidden;
    protected final boolean fixed;
    protected final Pack.Position position;
    protected final PackType packType;
    protected final Pack.Info info;
    protected final PackMetadataSection metadata;
    protected final Component title;
    protected final ResourceLocation resourcePackName;
    protected final Set<String> namespaces = new HashSet<>();
    protected final Map<ResourceLocation, byte[]> resources = new ConcurrentHashMap<>();
    protected final Map<String, byte[]> rootResources = new ConcurrentHashMap<>();
    protected final String mainNamespace;

    //for debug or to generate assets
    protected boolean generateDebugResources;

    protected DynamicResourcePack(ResourceLocation name, PackType type) {
        this(name, type, Pack.Position.TOP, false, false);
    }

    protected DynamicResourcePack(ResourceLocation name, PackType type, Pack.Position position, boolean fixed, boolean hidden) {
        this.packType = type;
        var component = Component.translatable(LangBuilder.getReadableName(name.getNamespace() + "_dynamic_resources"));
        this.resourcePackName = name;
        this.mainNamespace = name.getNamespace();
        this.namespaces.add(name.getNamespace());
        this.title = Component.translatable(LangBuilder.getReadableName(name.toString()));

        this.position = position;
        this.fixed = fixed;
        this.hidden = hidden; //UNUSED. TODO: re add (forge)
        this.metadata = new PackMetadataSection(component, type.getVersion(SharedConstants.getCurrentVersion()));
        this.info = new Pack.Info(metadata.getDescription(), metadata.getPackFormat(), FeatureFlagSet.of());

        this.generateDebugResources = PlatformHelper.isDev();
    }

    public void setGenerateDebugResources(boolean generateDebugResources) {
        this.generateDebugResources = generateDebugResources;
    }

    /**
     * Dynamic textures are loaded after getNamespaces is called, so unfortunately we need to know those in advance
     * Call this if you are adding stuff for another mod namespace
     **/
    public void addNamespaces(String... namespaces) {
        this.namespaces.addAll(Arrays.asList(namespaces));
    }

    public Component getTitle() {
        return this.title;
    }

    @Override
    public String packId() {
        return title.getString();
    }

    @Override
    public String toString() {
        return packId();
    }

    /**
     * registers this pack. Call on mod init
     */
    public void registerPack() {

        PlatformHelper.registerResourcePack(this.packType, () ->
                Pack.create(
                        this.packId(),    // id
                        this.getTitle(), // title
                        true,    // required -- this MAY need to be true for the pack to be enabled by default
                        (s) -> this, // pack supplier
                        this.info, // description
                        this.packType,
                        Pack.Position.TOP,
                        this.fixed, // fixed position? no
                        PackSource.BUILT_IN));
    }

    //@Override
    @PlatformOnly(PlatformOnly.FORGE)
    public boolean isHidden() {
        return this.hidden;
    }

    @Override
    public Set<String> getNamespaces(PackType packType) {
        return this.namespaces;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getMetadataSection(MetadataSectionSerializer<T> serializer) {
        return serializer instanceof PackMetadataSectionSerializer ? (T) this.metadata : null;
    }

    public void addRootResource(String name, byte[] resource) {
        this.rootResources.put(name, resource);
    }

    @Nullable
    @Override
    public IoSupplier<InputStream> getRootResource(String... strings) {
        String fileName = String.join("/", strings);
        return () -> new ByteArrayInputStream(this.rootResources.get(fileName));
    }


    @Override
    public void listResources(PackType packType, String namespace, String id, ResourceOutput output) {
        //why are we only using server resources here?
        if (packType == this.packType && packType == PackType.SERVER_DATA && this.namespaces.contains(namespace)) {
            //idk why but somebody had an issue with concurrency here during world load

            this.resources.entrySet().stream()
                    .filter(r -> (r.getKey().getNamespace().equals(namespace) && r.getKey().getPath().startsWith(id)))
                    .forEach(r -> output.accept(r.getKey(), () -> new ByteArrayInputStream(r.getValue())));
        }
    }

    @Override
    public IoSupplier<InputStream> getResource(PackType type, ResourceLocation id) {
        return () -> {
            if (type != this.packType) {
                throw new IOException(String.format("Tried to access wrong type of resource on %s.", this.resourcePackName));
            }
            var res = this.resources.get(id);
            if (res != null) {
                try {
                    return new ByteArrayInputStream(res);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            throw makeFileNotFoundException(String.format("%s/%s/%s", type.getDirectory(), id.getNamespace(), id.getPath()));
        };
    }

    @Override
    public void close() {
        // do not clear after reloading texture packs. should always be on
    }

    public FileNotFoundException makeFileNotFoundException(String path) {
        return new FileNotFoundException(String.format("'%s' in ResourcePack '%s'", path, this.resourcePackName));
    }

    protected void addBytes(ResourceLocation path, byte[] bytes) {
        this.namespaces.add(path.getNamespace());
        this.resources.put(path, bytes);

        //debug
        if (generateDebugResources) {
            try {
                Path p = Paths.get("debug", "generated_resource_pack").resolve(path.getNamespace() + "/" + path.getPath());
                Files.createDirectories(p.getParent());
                Files.write(p, bytes, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
            } catch (IOException ignored) {
            }
        }
    }

    public void addResource(StaticResource resource) {
        this.addBytes(resource.location, resource.data);
    }

    private void addJson(ResourceLocation path, JsonElement json) {
        try {
            this.addBytes(path, RPUtils.serializeJson(json).getBytes());
        } catch (IOException e) {
            LOGGER.error("Failed to write JSON {} to resource pack {}.", path, this.resourcePackName, e);
        }
    }

    public void addJson(ResourceLocation location, JsonElement json, ResType resType) {
        this.addJson(resType.getPath(location), json);
    }

    public void addBytes(ResourceLocation location, byte[] bytes, ResType resType) {
        this.addBytes(resType.getPath(location), bytes);
    }


    public PackType getPackType() {
        return packType;
    }

}
