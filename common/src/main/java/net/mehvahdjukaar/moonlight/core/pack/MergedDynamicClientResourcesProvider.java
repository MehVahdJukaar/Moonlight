package net.mehvahdjukaar.moonlight.core.pack;

import com.mojang.blaze3d.platform.NativeImage;
import net.mehvahdjukaar.moonlight.api.resources.pack.DynamicResourcePack;
import net.mehvahdjukaar.moonlight.api.resources.pack.DynamicResourcesProvider;
import net.mehvahdjukaar.moonlight.api.resources.pack.SimplePackProvider;
import net.mehvahdjukaar.moonlight.core.Moonlight;
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
import net.minecraft.server.packs.resources.IoSupplier;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class MergedDynamicClientResourcesProvider implements PackResources, SimplePackProvider {

    private final Set<DynamicResourcesProvider> providers = new HashSet<>();
    private final List<PackResources> packResourcesStack = new ArrayList<>();
    private final PackLocationInfo locationInfo;
    private final Set<String> modNamespaces = new HashSet<>();
    private PackMetadataSection metadata;
    private byte[] packIcon;

    public MergedDynamicClientResourcesProvider(PackLocationInfo info) {
        this.locationInfo = info;
    }

    public void add(DynamicResourcesProvider provider) {
        if (provider.getPackType() != PackType.CLIENT_RESOURCES) {
            throw new IllegalArgumentException("Tried to merge a pack provider of type " + provider.getPackType() +
                    " to a merged provider of type " + PackType.CLIENT_RESOURCES);
        }
        if (this.providers.add(provider)) {
            this.packResourcesStack.add(provider.getPackResources());
            this.packResourcesStack.sort(Comparator.comparing(PackResources::packId));
            this.modNamespaces.add(provider.getName().getNamespace());
        }
    }

    public void addLegacy(DynamicResourcePack dynPack) {
        this.packResourcesStack.add(dynPack);
        this.packResourcesStack.sort(Comparator.comparing(PackResources::packId));
        this.modNamespaces.add(dynPack.mainNamespace);
    }

    @Override
    public @Nullable IoSupplier<InputStream> getRootResource(String... strings) {
        String fileName = String.join("/", strings);
        if (fileName.equals("pack.png") && packIcon != null) {
            return () -> new ByteArrayInputStream(packIcon);
        }
        for (PackResources packResources : this.packResourcesStack) {
            IoSupplier<InputStream> r = packResources.getRootResource(strings);
            if (r != null) return r;
        }
        return null;
    }

    @Override
    public @Nullable IoSupplier<InputStream> getResource(PackType packType, ResourceLocation location) {
        Iterator<PackResources> iterator = this.packResourcesStack.iterator();

        IoSupplier<InputStream> ioSupplier;
        do {
            if (!iterator.hasNext()) {
                return null;
            }

            PackResources packResources = iterator.next();
            ioSupplier = packResources.getResource(packType, location);
        } while (ioSupplier == null);

        return ioSupplier;
    }

    @Override
    public void listResources(PackType packType, String namespace, String path, ResourceOutput resourceOutput) {
        Map<ResourceLocation, IoSupplier<InputStream>> map = new HashMap<>();
        for (PackResources packResources : this.packResourcesStack) {
            Objects.requireNonNull(map);
            packResources.listResources(packType, namespace, path, map::putIfAbsent);
        }

        map.forEach(resourceOutput);
    }

    @Override
    public Set<String> getNamespaces(PackType type) {
        Set<String> set = new HashSet<>();
        for (PackResources packResources : this.packResourcesStack) {
            set.addAll(packResources.getNamespaces(type));
        }
        return set;
    }

    @Override
    public @Nullable <T> T getMetadataSection(MetadataSectionSerializer<T> serializer) throws IOException {
        if (metadata == null) {
            this.metadata = new PackMetadataSection(Component
                    .translatable("message.moonlight.merged_pack.description", modNamespaces.size()),
                    SharedConstants.getCurrentVersion().getPackVersion(PackType.CLIENT_RESOURCES),
                    Optional.empty());
        }
        return serializer == PackMetadataSection.TYPE ? (T) this.metadata : null;
    }

    @Override
    public PackLocationInfo location() {
        return locationInfo;
    }

    @Override
    public void close() {
        this.packResourcesStack.forEach(PackResources::close);
    }

    public int size() {
        return this.packResourcesStack.size();
    }

    private void checkInitialized() {
        if (this.packIcon == null) {
            this.packIcon = createIcon();
        }
    }

    private byte[] createIcon() {
        List<NativeImage> icons = new ArrayList<>();
        for (var p : packResourcesStack) {
            var icon = p.getRootResource("pack.png");
            if (icon != null) {
                try (var s = icon.get()) {
                    icons.add(NativeImage.read(s));
                } catch (Exception e) {
                    Moonlight.LOGGER.error("Failed to read pack icon from {}", p.packId(), e);
                }
            }else{
                Moonlight.LOGGER.warn("Pack {} has no icon", p.packId());
            }
        }
        try (NativeImage image = ImageMerger.mergeSquare(icons, ImageMerger.Mode.MIN_AREA_NO_UPSCALE, 0xFF000000)) {
            return image.asByteArray();
        } catch (Exception ignored) {
            Moonlight.LOGGER.error("Failed to merge pack icons");
        } finally {
            for (var i : icons) i.close();
        }
        return null;
    }


    @Override
    public Pack createPack() {

        PackResources resources = this;
        return Pack.readMetaAndCreate(
                this.locationInfo,
                new Pack.ResourcesSupplier() {
                    @Override
                    public PackResources openPrimary(PackLocationInfo location) {
                        checkInitialized();
                        return resources;
                    }

                    @Override
                    public PackResources openFull(PackLocationInfo location, Pack.Metadata metadata) {
                        return resources;
                    }
                },
                PackType.CLIENT_RESOURCES,
                new PackSelectionConfig(
                        true, Pack.Position.TOP, false
                )
        );
    }
}
