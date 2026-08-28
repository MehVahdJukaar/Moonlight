package net.mehvahdjukaar.moonlight.api.resources.pack;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//


import com.google.common.base.Stopwatch;
import com.mojang.logging.LogUtils;
import net.mehvahdjukaar.moonlight.api.misc.ResourceLocationSearchTrie;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.IoSupplier;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

//clone
public class FastSearchFilePackResources extends AbstractPackResources {
    static final Logger LOGGER = LogUtils.getLogger();
    private final SharedZipFileAccess zipFileAccess;

    private final ResourceLocationSearchTrie searchTrie = new ResourceLocationSearchTrie();
    private final PackType packType;

    FastSearchFilePackResources(PackLocationInfo location, File file,
                                PackType packType) {
        super(location);
        this.zipFileAccess = new SharedZipFileAccess(file);
        this.packType = packType;
        buildIndex();
    }

    private void buildIndex() {
        Stopwatch watch = Stopwatch.createStarted();
        try {
            ZipFile zip = this.zipFileAccess.getOrCreateZipFile();
            String pathName = this.packType.getDirectory() + "/";
            assert zip != null;
            Enumeration<? extends ZipEntry> e = zip.entries();
            while (e.hasMoreElements()) {
                ZipEntry ze = e.nextElement();
                if (ze.isDirectory()) continue;
                String name = ze.getName();
                if (name.startsWith(pathName)) {
                    name = name.substring(pathName.length());
                } else {
                    continue;
                }
                searchTrie.insertPath(name); // assumes trie supports insert(String fullPath)
            }
        } catch (Exception e) {
            LOGGER.error("Failed to index zip file {}", this.zipFileAccess, e);
        } finally {
            Moonlight.LOGGER.info("Populated search tree for pack at {} in {}", this.zipFileAccess, watch);
        }
    }

    private static String getPathFromLocation(PackType packType, Identifier location) {
        return String.format(Locale.ROOT, "%s/%s/%s", packType.getDirectory(), location.getNamespace(), location.getPath());
    }

    @Nullable
    public IoSupplier<InputStream> getRootResource(String... elements) {
        return this.getResource(String.join("/", elements));
    }

    @Override
    public IoSupplier<InputStream> getResource(PackType packType, Identifier location) {
        if (packType != this.packType) return null;
        return this.getResource(getPathFromLocation(packType, location));
    }

    @Nullable
    private IoSupplier<InputStream> getResource(String resourcePath) {
        ZipFile zipFile = this.zipFileAccess.getOrCreateZipFile();
        if (zipFile == null) {
            return null;
        } else {
            ZipEntry zipEntry = zipFile.getEntry(resourcePath);
            return zipEntry == null ? null : IoSupplier.create(zipFile, zipEntry);
        }
    }

    @Override
    public Set<String> getNamespaces(PackType packType) {
        if (packType != this.packType) return Set.of();

        return new HashSet<>(this.searchTrie.listFolders(""));
    }

    @Override
    public void close() {
        this.zipFileAccess.close();
    }

    @Override
    public void listResources(PackType packType, String namespace, String path, PackResources.ResourceOutput output) {
        if (packType != this.packType) return;
        String prefix = packType.getDirectory() + "/";
        ZipFile zipFile = this.zipFileAccess.getOrCreateZipFile();
        if (zipFile != null) {
            this.searchTrie.search(namespace + "/" + path)
                    .forEach(r -> {
                        ZipEntry zipEntry = zipFile.getEntry(prefix + r.getNamespace()+"/"+r.getPath());
                        if (zipEntry == null) {
                            throw new RuntimeException("Zip file entry was null");
                        }
                        output.accept(r, IoSupplier.create(zipFile, zipEntry));
                    });
        }
    }

    static class SharedZipFileAccess implements AutoCloseable {
        final File file;
        @Nullable
        private ZipFile zipFile;
        private boolean failedToLoad;

        SharedZipFileAccess(File file) {
            this.file = file;
        }

        @Nullable
        ZipFile getOrCreateZipFile() {
            if (this.failedToLoad) {
                return null;
            } else {
                if (this.zipFile == null) {
                    try {
                        this.zipFile = new ZipFile(this.file);
                    } catch (IOException var2) {
                        LOGGER.error("Failed to open pack {}", this.file, var2);
                        this.failedToLoad = true;
                        return null;
                    }
                }

                return this.zipFile;
            }
        }

        @Override
        public String toString() {
            return file.toString();
        }

        public void close() {
            if (this.zipFile != null) {
                IOUtils.closeQuietly(this.zipFile);
                this.zipFile = null;
            }

        }
    }
}
