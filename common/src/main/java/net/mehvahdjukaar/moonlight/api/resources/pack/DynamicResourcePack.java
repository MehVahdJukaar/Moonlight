package net.mehvahdjukaar.moonlight.api.resources.pack;

import com.google.common.base.Suppliers;
import com.google.gson.JsonElement;
import dev.architectury.injectables.annotations.PlatformOnly;
import net.mehvahdjukaar.moonlight.api.integration.ModernFixCompat;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.resources.RPUtils;
import net.mehvahdjukaar.moonlight.api.resources.ResType;
import net.mehvahdjukaar.moonlight.api.resources.StaticResource;
import net.mehvahdjukaar.moonlight.api.resources.assets.LangBuilder;
import net.mehvahdjukaar.moonlight.core.CompatHandler;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.IoSupplier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;
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
import java.util.function.Supplier;

public abstract class DynamicResourcePack implements PackResources {
    private static final List<DynamicResourcePack> INSTANCES = new ArrayList<>();

    @ApiStatus.Internal
    public static void clearAfterReload(PackType targetType) {
        //this will be called multiple times. shunt be an issue I hope
        for (var p : DynamicResourcePack.INSTANCES) {
            if (p.packType == targetType) {
                p.clearNonStatic();
            }
        }
    }

    @ApiStatus.Internal
    public static void clearBeforeReload(PackType targetType) {
        for (var p : DynamicResourcePack.INSTANCES) {
            if (p.packType == targetType) {
                p.clearAllContent();
            }
        }
    }


    protected static final Logger LOGGER = LogManager.getLogger();

    protected final PackLocationInfo locationInfo;
    protected final boolean hidden;
    protected final boolean fixed;
    protected final Pack.Position position;
    protected final PackType packType;
    protected final Supplier<PackMetadataSection> metadata;
    protected final ResourceLocation resourcePackName;
    protected final Set<String> namespaces = new HashSet<>();
    protected final Map<ResourceLocation, byte[]> resources = new ConcurrentHashMap<>();
    protected final Map<String, byte[]> rootResources = new ConcurrentHashMap<>();
    protected final String mainNamespace;


    protected boolean clearOnReload = true;
    protected Set<ResourceLocation> staticResources = new HashSet<>();

    //for debug or to generate assets
    protected boolean generateDebugResources;

    boolean addToStatic = false;

    protected DynamicResourcePack(ResourceLocation name, PackType type) {
        this(name, type, Pack.Position.TOP, false, false);
    }

    protected DynamicResourcePack(ResourceLocation name, PackType type, Pack.Position position, boolean fixed, boolean hidden) {
        this.packType = type;
        this.resourcePackName = name;
        this.mainNamespace = name.getNamespace();
        this.namespaces.add(name.getNamespace());
        var title = Component.translatable(LangBuilder.getReadableName(name.toString()));

        this.position = position;
        this.fixed = fixed;
        this.hidden = hidden; //UNUSED. TODO: re add (forge)
        this.metadata = Suppliers.memoize(() -> new PackMetadataSection(this.makeDescription(),
                SharedConstants.getCurrentVersion().getPackVersion(type), Optional.empty()));
        this.generateDebugResources = PlatHelper.isDev();
    }

    @Override
    public PackLocationInfo location() {
        return locationInfo;
    }

    public Component makeDescription() {
        return Component.translatable(LangBuilder.getReadableName(mainNamespace + "_dynamic_resources"));
    }

    public void setClearOnReload(boolean canBeCleared) {
        this.clearOnReload = canBeCleared;
    }

    @Deprecated(forRemoval = true)
    public void clearOnReload(boolean canBeCleared) {
        this.clearOnReload = canBeCleared;
    }

    /**
     * Marks this texture as non-clearable.
     * By default, all textures will be cleared after texture atlases have been created
     * Call this for textures that are not on an atlas.
     */
    public void markNotClearable(ResourceLocation texturePath) {
        this.staticResources.add(texturePath);
    }

    public void unMarkNotClearable(ResourceLocation staticResources) {
        this.staticResources.remove(staticResources);
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

    public ResourceLocation id() {
        return resourcePackName;
    }

    @Override
    public String toString() {
        return packId();
    }

    /**
     * Registers this pack. Call on mod init
     */
    public void registerPack() {

        if (!INSTANCES.contains(this)) {
            PlatHelper.registerResourcePack(this.packType, () ->
                    Pack.readMetaAndCreate(
                            new PackLocationInfo(
                                    this.packId(),    // id
                                    this.getTitle(), // title
                                    PackSource.BUILT_IN,
                                    Optional.empty() //no clue what this is
                            ),
                            new Pack.ResourcesSupplier() {
                                @Override
                                public PackResources openPrimary(PackLocationInfo location) {
                                    return DynamicResourcePack.this;
                                }

                                @Override
                                public PackResources openFull(PackLocationInfo location, Pack.Metadata metadata) {
                                    return DynamicResourcePack.this;
                                }
                            },// pack supplier
                            this.packType,
                            new PackSelectionConfig(
                                    true,    // required -- this MAY need to be true for the pack to be enabled by default
                                    Pack.Position.TOP,
                                    false // fixed position
                            )
                    ));
            INSTANCES.add(this);
        }
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
        try {
            return serializer == PackMetadataSection.TYPE ? (T) this.metadata.get() : null;
        } catch (Exception exception) {
            return null;
        }
    }

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
    public void listResources(PackType packType, String namespace, String id, ResourceOutput output) {
        //why are we only using server resources here?
        if (packType == this.packType && this.namespaces.contains(namespace)) {
            //idk why but somebody had an issue with concurrency here during world load

            this.resources.entrySet().stream()
                    .filter(r -> (r.getKey().getNamespace().equals(namespace) && r.getKey().getPath().startsWith(id)))
                    .forEach(r -> output.accept(r.getKey(), () -> new ByteArrayInputStream(r.getValue())));
        }
    }

    @Override
    public IoSupplier<InputStream> getResource(PackType type, ResourceLocation id) {

        var res = this.resources.get(id);
        if (res != null) {
            return () -> {
                if (type != this.packType) {
                    throw new IOException(String.format("Tried to access wrong type of resource on %s.", this.resourcePackName));
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

    public FileNotFoundException makeFileNotFoundException(String path) {
        return new FileNotFoundException(String.format("'%s' in ResourcePack '%s'", path, this.resourcePackName));
    }

    protected void addBytes(ResourceLocation path, byte[] bytes) {
        this.namespaces.add(path.getNamespace());
        this.resources.put(path, bytes);
        if (addToStatic) markNotClearable(path);
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

    public void removeResource(ResourceLocation res) {
        this.resources.remove(res);
        this.staticResources.remove(res);
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

    // Called after texture have been stitched. Only keeps needed stuff
    @ApiStatus.Internal
    protected void clearNonStatic() {
        boolean mf = MODERN_FIX && getPackType() == PackType.CLIENT_RESOURCES;
        for (var r : this.resources.keySet()) {
            if (mf && modernFixHack(r)) continue;
            if (!this.staticResources.contains(r)) {
                this.resources.remove(r);
            }
        }
    }

    // Called after each reload
    @ApiStatus.Internal
    protected void clearAllContent() {
        if (this.clearOnReload) {
            for (var r : this.resources.keySet()) {
                this.resources.remove(r);
            }
        }
    }

    private static final boolean MODERN_FIX = CompatHandler.MODERNFIX && ModernFixCompat.areLazyResourcesOn();

    private boolean modernFixHack(ResourceLocation r) {
        String s = r.getPath();
        return s.startsWith("model") || s.startsWith("blockstate");
    }
}