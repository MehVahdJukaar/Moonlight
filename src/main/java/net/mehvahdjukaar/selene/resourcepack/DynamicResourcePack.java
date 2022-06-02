package net.mehvahdjukaar.selene.resourcepack;

import com.google.gson.JsonElement;
import net.mehvahdjukaar.selene.client.asset_generators.LangBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.metadata.pack.PackMetadataSectionSerializer;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.loading.FMLLoader;
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
    private final Set<String> namespaces = new HashSet<>();
    private final Map<ResourceLocation, byte[]> resources = new ConcurrentHashMap<>();
    private final Map<String, byte[]> rootResources = new ConcurrentHashMap<>();
    protected final String mainNamespace;

    //for debug or to generate assets
    public boolean generateDebugResources = false;

    public DynamicResourcePack(ResourceLocation name, PackType type) {
        this(name, type, Pack.Position.TOP, false, false);
    }

    public DynamicResourcePack(ResourceLocation name, PackType type, Pack.Position position, boolean fixed, boolean hidden) {
        this.packType = type;
        var component = new TextComponent(LangBuilder.getReadableName(name.getNamespace() + "_dynamic_resources"));
        //new TranslatableComponent("%s.%s.description", name.getNamespace(), name.getPath());
        this.packInfo = new PackMetadataSection(component, 6);
        this.resourcePackName = name;
        this.mainNamespace = name.getNamespace();
        this.namespaces.add(name.getNamespace());
        this.title = new TextComponent(LangBuilder.getReadableName(name.toString()));
        ;//new TranslatableComponent("%s.%s.title", name.getNamespace(), name.getPath());

        this.position = position;
        this.fixed = fixed;
        this.hidden = hidden;

        this.generateDebugResources = !FMLLoader.isProduction();
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
    public String getName() {
        return title.getString();
    }

    @Override
    public String toString() {
        return getName();
    }

    /**
     * registers this pack. Intern calls AddPackFinderEvent
     *
     * @param bus MOD event bus
     */
    public void registerPack(IEventBus bus) {
        bus.addListener(this::register);
    }

    /**
     * same as register pack but takes the actual event. Use just one
     *
     * @param event AddPackFindersEvent
     */
    public void register(AddPackFindersEvent event) {
        if (event.getPackType() == this.packType) {
            event.addRepositorySource((infoConsumer, packFactory) ->
                    infoConsumer.accept(new Pack(
                            this.getName(),    // id
                            true,    // required -- this MAY need to be true for the pack to be enabled by default
                            () -> this, // pack supplier
                            this.getTitle(), // title
                            this.packInfo.getDescription(), // description
                            PackCompatibility.COMPATIBLE,
                            Pack.Position.TOP,
                            this.fixed, // fixed position? no
                            PackSource.BUILT_IN,
                            this.hidden // hidden? no
                    )));
        }
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
            PackType packType, String namespace, String id, int maxDepth, Predicate<String> filter) {

        //why are we only using server resources here?
        if (packType == this.packType && packType == PackType.SERVER_DATA && this.namespaces.contains(namespace)) {
            //idk why but somebody had an issue with concurrency here during world load
            return this.resources.keySet().stream()
                    .filter(r -> (r.getNamespace().equals(namespace) && r.getPath().startsWith(id)))
                    .filter(r -> filter.test(r.toString()))
                    .collect(Collectors.toList());
        }
        return NO_RESOURCES;
    }

    @Override
    public InputStream getResource(PackType type, ResourceLocation id) throws IOException {
        if (type != this.packType) {
            throw new IOException(String.format("Tried to access wrong type of resource on %s.", this.resourcePackName));
        }
        if (this.resources.containsKey(id)) {
            try {
                return new ByteArrayInputStream(this.resources.get(id));
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
            //json.toString().getBytes();
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

    //TODO: move to RP utils

    /**
     * This is a handy method for dynamic resource pack since it allows to specify the name of an existing resource
     * that will then be copied and modified replacing a certain keyword in it with another.
     * This is useful when adding new woodtypes as one can simply manually add a default wood json and provide the method with the
     * default woodtype name and the target name
     * The target location will the one of this pack while its path will be the original one modified following the same principle as the json itself
     *
     * @param resource    target resource that will be copied, modified and saved back
     * @param keyword     keyword to replace
     * @param replaceWith word to replace the keyword with
     */
    public void addSimilarJsonResource(StaticResource resource, String keyword, String replaceWith) throws NoSuchElementException {
        addSimilarJsonResource(resource, s -> s.replace(keyword, replaceWith));
    }

    public void addSimilarJsonResource(StaticResource resource, Function<String, String> textTransform) throws NoSuchElementException {
        addSimilarJsonResource(resource, textTransform, textTransform);
    }

    public void addSimilarJsonResource(StaticResource resource, Function<String, String> textTransform, Function<String, String> pathTransform) throws NoSuchElementException {
        ResourceLocation fullPath = resource.location;

        String fullText = new String(resource.data, StandardCharsets.UTF_8);


        fullText = textTransform.apply(fullText);
        //calculates new path
        StringBuilder builder = new StringBuilder();
        String[] partial = fullPath.getPath().split("/");
        for (int i = 0; i < partial.length; i++) {
            if (i != 0) builder.append("/");
            if (i == partial.length - 1) {
                builder.append(pathTransform.apply(partial[i]));
            } else builder.append(partial[i]);
        }
        //adds modified under my namespace
        ResourceLocation newRes = new ResourceLocation(resourcePackName.getNamespace(), builder.toString());
        this.addBytes(newRes, fullText.getBytes());
    }

}
