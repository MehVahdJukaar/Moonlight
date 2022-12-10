package net.mehvahdjukaar.moonlight.api.resources.pack;

import com.google.gson.JsonElement;
import dev.architectury.injectables.annotations.PlatformOnly;
import net.mehvahdjukaar.moonlight.api.resources.assets.LangBuilder;
import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.mehvahdjukaar.moonlight.api.resources.RPUtils;
import net.mehvahdjukaar.moonlight.api.resources.ResType;
import net.mehvahdjukaar.moonlight.api.resources.StaticResource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.metadata.pack.PackMetadataSectionSerializer;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.world.level.block.WoolCarpetBlock;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorPreset;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorPresets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class DynamicResourcePack implements PackResources {
    protected static final Logger LOGGER = LogManager.getLogger();

    public static final List<ResourceLocation> NO_RESOURCES = Collections.emptyList();

    protected final boolean hidden;
    protected final boolean fixed;
    protected final Pack.Position position;
    protected final PackType packType;
    protected final PackMetadataSection packInfo;
    protected final Component title;
    protected final ResourceLocation resourcePackName;
    protected final Set<String> namespaces = new HashSet<>();
    protected final Map<ResourceLocation, byte[]> resources = new ConcurrentHashMap<>();
    protected final Map<String, byte[]> rootResources = new ConcurrentHashMap<>();
    protected final String mainNamespace;

    //for debug or to generate assets
    public boolean generateDebugResources = false;

    protected DynamicResourcePack(ResourceLocation name, PackType type) {
        this(name, type, Pack.Position.TOP, false, false);
    }

    protected DynamicResourcePack(ResourceLocation name, PackType type, Pack.Position position, boolean fixed, boolean hidden) {
        this.packType = type;
        var component = Component.translatable(LangBuilder.getReadableName(name.getNamespace() + "_dynamic_resources"));
        this.packInfo = new PackMetadataSection(component, 6);
        this.resourcePackName = name;
        this.mainNamespace = name.getNamespace();
        this.namespaces.add(name.getNamespace());
        this.title = Component.translatable(LangBuilder.getReadableName(name.toString()));

        this.position = position;
        this.fixed = fixed;
        this.hidden = hidden; //UNUSED. TODO: re add (forge)

        this.generateDebugResources = PlatformHelper.isDev();
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
                new Pack(
                        this.packId(),    // id
                        true,    // required -- this MAY need to be true for the pack to be enabled by default
                        () -> this, // pack supplier
                        this.getTitle(), // title
                        this.packInfo.getDescription(), // description
                        PackCompatibility.COMPATIBLE,
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
        return serializer instanceof PackMetadataSectionSerializer ? (T) this.packInfo : null;
    }

    public void addRootResource(String name, byte[] resource) {
        this.rootResources.put(name, resource);
    }

    @Nullable
    @Override
    public InputStream getRootResource(String pFileName) {
        return new ByteArrayInputStream(this.rootResources.get(pFileName));
    }

    @Override
    public Collection<ResourceLocation> getResources(
            PackType packType, String namespace, String id, Predicate<ResourceLocation> filter) {

        //why are we only using server resources here?
        if (packType == this.packType && packType == PackType.SERVER_DATA && this.namespaces.contains(namespace)) {
            //idk why but somebody had an issue with concurrency here during world load
            return this.resources.keySet().stream()
                    .filter(r -> (r.getNamespace().equals(namespace) && r.getPath().startsWith(id)))
                    .filter(filter)
                    .toList();
        }
        return NO_RESOURCES;
    }

    @Override
    public IoSupplier<InputStream> getResource(PackType type, ResourceLocation id) {
        if (type != this.packType) {
            throw new IOException(String.format("Tried to access wrong type of resource on %s.", this.resourcePackName));
        }
        if (this.resources.containsKey(id)) {
            try {
                return ()->new ByteArrayInputStream(this.resources.get(id));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        throw makeFileNotFoundException(String.format("%s/%s/%s", type.getDirectory(), id.getNamespace(), id.getPath()));
    }

    @Override
    public boolean hasResource(PackType type, ResourceLocation id) {
        return type == this.packType && (this.resources.containsKey(id));
    }

    @Override
    public void close() {
        if (this.packType == PackType.CLIENT_RESOURCES) {
            // do not clear after reloading texture packs. should always be on
            // this.resources.clear();
            // this.namespaces.clear();
        }
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
