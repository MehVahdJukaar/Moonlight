//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package net.mehvahdjukaar.moonlight.api.resources.pack;

import com.google.common.base.Joiner;
import com.google.common.base.Stopwatch;
import com.mojang.logging.LogUtils;
import net.mehvahdjukaar.moonlight.api.misc.ResourceLocationSearchTrie;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.FileUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.IoSupplier;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FastSearchPathPackResources extends AbstractPackResources {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Joiner PATH_JOINER = Joiner.on("/");
    private final Path root;

    private final ResourceLocationSearchTrie searchTrie = new ResourceLocationSearchTrie();
    private final PackType packType;

    public FastSearchPathPackResources(PackLocationInfo location, Path root, PackType packType) {
        super(location);
        this.root = root;
        this.packType = packType;
        buildIndex();
    }

    private void buildIndex() {
        Stopwatch watch = Stopwatch.createStarted();


        final Path base = this.root.resolve(this.packType.getDirectory()); // "assets" or "data"

        if (!Files.exists(base)) {
            return;
        }

        try {
            // Walk all regular files under <root>/<assets|data>
            try (java.util.stream.Stream<Path> stream =
                         java.nio.file.Files.find(base, Integer.MAX_VALUE,
                                 (p, attrs) -> attrs.isRegularFile())) {

                stream.forEach(file -> {
                    // Relativize to get "<namespace>/<rest/of/path>"
                    String rel = base.relativize(file).toString().replace('\\', '/');

                    int slash = rel.indexOf('/');
                    if (slash <= 0 || slash == rel.length() - 1) {
                        // No namespace or empty remainder -> skip
                        return;
                    }

                    String namespace = rel.substring(0, slash);
                    String pathWithinNs = rel.substring(slash + 1);

                    if (!ResourceLocation.isValidNamespace(namespace)) {
                        // Match vanilla behavior: warn and skip invalid namespaces
                        LOGGER.warn("Non [a-z0-9_.-] character in namespace {} in pack {}, ignoring",
                                namespace, this.root);
                        return;
                    }

                    ResourceLocation rl = ResourceLocation.tryBuild(namespace, pathWithinNs);
                    if (rl != null) {
                        this.searchTrie.insert(rl);
                    }
                });
            }

        } catch (IOException e) {
            LOGGER.error("Failed to build index for {}", base, e);
            // Mark built to avoid retry loops; remove this line if you prefer retry-on-next-call
        }


        Moonlight.LOGGER.info("generated search trie for fast zip file access in {}", watch);
    }

    @Nullable
    public IoSupplier<InputStream> getRootResource(String... elements) {
        FileUtil.validatePath(elements);
        Path path = FileUtil.resolvePath(this.root, List.of(elements));
        return Files.exists(path) ? IoSupplier.create(path) : null;
    }


    @Nullable
    public IoSupplier<InputStream> getResource(PackType packType, ResourceLocation location) {
        if (this.packType != packType) return null;
        Path path = this.root.resolve(packType.getDirectory()).resolve(location.getNamespace());
        return getResource(location, path);
    }

    @Nullable
    public static IoSupplier<InputStream> getResource(ResourceLocation location, Path path) {
        return FileUtil.decomposePath(location.getPath()).mapOrElse((list) -> {
            Path path2 = FileUtil.resolvePath(path, list);
            return returnFileIfExists(path2);
        }, (error) -> {
            LOGGER.error("Invalid path {}: {}", location, error.message());
            return null;
        });
    }

    @Nullable
    private static IoSupplier<InputStream> returnFileIfExists(Path path) {
        return Files.exists(path) ? IoSupplier.create(path) : null;
    }

    @Override
    public void listResources(PackType packType, String namespace, String path, PackResources.ResourceOutput output) {
        if (packType != this.packType) return; // Optional guard, if you're storing `this.packType`

        this.searchTrie.search( namespace + "/" + path).forEach(resourceLocation -> {
            Path file = this.root
                    .resolve(packType.getDirectory())   // e.g., "assets" or "data"
                    .resolve(resourceLocation.getNamespace())
                    .resolve(resourceLocation.getPath());

            if (Files.isRegularFile(file)) {
                output.accept(resourceLocation, IoSupplier.create(file));
            } else {
                LOGGER.warn("File not found or not regular: {} -> {}", resourceLocation, file);
            }
        });
    }

    @Override
    public Set<String> getNamespaces(PackType packType) {
        if (packType != this.packType) return Set.of();

        return new HashSet<>(this.searchTrie.listFolders(""));
    }

    public void close() {
    }
}
