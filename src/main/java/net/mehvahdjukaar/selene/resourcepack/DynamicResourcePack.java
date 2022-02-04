package net.mehvahdjukaar.selene.resourcepack;

import com.google.gson.JsonElement;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.metadata.pack.PackMetadataSectionSerializer;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class DynamicResourcePack implements PackResources {
    protected static final Logger LOGGER = LogManager.getLogger();

    public static final List<ResourceLocation> NO_RESOURCES = Collections.emptyList();

    protected final PackType packType;
    protected final PackMetadataSection packInfo;
    protected final TranslatableComponent title;
    protected final ResourceLocation resourcePackName;
    private final Set<String> namespaces = new HashSet<>();
    private final Map<ResourceLocation, byte[]> resources = new HashMap<>();

    public DynamicResourcePack(ResourceLocation name, PackType type) {
        this.packType = type;
        this.packInfo = new PackMetadataSection(
                new TranslatableComponent("%s.%s_description", name.getNamespace(), name.getPath()), 6);
        this.resourcePackName = name;
        this.namespaces.add(name.getNamespace());
        this.title = new TranslatableComponent("%s.%s_title", name.getNamespace(), name.getPath());
    }

    /**
     * Dynamic textures are loaded after getNamespaces is called so unfortunately we need to know those in advance
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

    public void addPackToRepository(PackRepository packRepository) {
        packRepository.addPackFinder((infoConsumer, packFactory) ->
                infoConsumer.accept(new Pack(
                        this.getName(),    // id
                        true,    // required -- this MAY need to be true for the pack to be enabled by default
                        () -> this, // pack supplier
                        this.getTitle(), // title
                        this.packInfo.getDescription(), // description
                        PackCompatibility.COMPATIBLE,
                        Pack.Position.TOP,
                        false, // fixed position? no
                        PackSource.DEFAULT,
                        false // hidden? no
                )));
        packRepository.reload();
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

    @Nullable
    @Override
    public InputStream getRootResource(String p_10294_) {
        return null;
    }

    @Override
    public Collection<ResourceLocation> getResources(
            PackType packType, String namespace, String id, int maxDepth, Predicate<String> filter) {

        if (packType == this.packType && packType == PackType.SERVER_DATA && this.namespaces.contains(namespace)) {
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
            throw new IOException(String.format("tried to access wrong type of resource on %s.", this.resourcePackName));
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
            this.resources.clear();
            this.namespaces.clear();
        }
    }

    public FileNotFoundException makeFileNotFoundException(String path) {
        return new FileNotFoundException(String.format("'%s' in ResourcePack '%s'", path, this.resourcePackName));
    }

    protected void addBytes(ResourceLocation path, byte[] bytes) {
        this.namespaces.add(path.getNamespace());
        this.resources.put(path, bytes);

        //debug
        if (true) {
            try {
                var p = Paths.get("debug", "generated_pack").resolve(path.getNamespace() + "/" + path.getPath());
                Files.createDirectories(p.getParent());
                Files.write(p, bytes, StandardOpenOption.CREATE, StandardOpenOption.WRITE,
                        StandardOpenOption.TRUNCATE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected void addImage(ResourceLocation path, NativeImage image, RPUtils.ResType resType) {
        try {
            this.addBytes(path, image.asByteArray(), resType);
        } catch (Exception e) {
            LOGGER.warn("Failed to add image {} to resource pack {}.", path, this.resourcePackName, e);
        }
    }

    private void addJson(ResourceLocation path, JsonElement json) {
        try {
            //json.toString().getBytes();
            this.addBytes(path, RPUtils.serializeJson(json).getBytes());
        } catch (IOException e) {
            LOGGER.error("Failed to write JSON {} to resource pack {}.", path, this.resourcePackName, e);
        }
    }

    public void addJson(ResourceLocation location, JsonElement json, RPUtils.ResType resType){
        this.addJson(RPUtils.resPath(location, resType), json);
    }

    public void addBytes(ResourceLocation location, byte[] bytes, RPUtils.ResType resType){
        this.addBytes(RPUtils.resPath(location, resType), bytes);
    }


}
