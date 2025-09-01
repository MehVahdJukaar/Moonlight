package net.mehvahdjukaar.moonlight.api.resources.pack;

import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.Pack;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public interface PackCacheStrategy {

    boolean needsRegeneration(IEditablePackResources packResources, Collection<Pack> loadedPacks);

    void markRegenerated(IEditablePackResources packResources, Collection<Pack> loadedPacks);

    IEditablePackResources createPackResources(PackLocationInfo info, PackType type, PackMetadataSection metadata);


    PackCacheStrategy NO_CACHE = new PackCacheStrategy() {

        @Override
        public boolean needsRegeneration(IEditablePackResources packResources, Collection<Pack> loadedPacks) {
            return true;
        }

        @Override
        public void markRegenerated(IEditablePackResources pack, Collection<Pack> loadedPacks) {
            // no cache
        }

        @Override
        public IEditablePackResources createPackResources(PackLocationInfo info, PackType type, PackMetadataSection metadata) {
            return new InMemoryPackResources(info, type, metadata);
        }
    };

    PackCacheStrategy SIMPLE_CACHE = new PackCacheStrategy() {

        private static String computeCurrentFingerprint(Collection<Pack> packs) {
            List<String> tokens = new ArrayList<>();

            // Deterministic mod list: name@version
            var mods = new ArrayList<>(PlatHelper.getInstalledMods());
            mods.sort(String::compareTo);
            for (var mod : mods) {
                tokens.add(mod + "@" + PlatHelper.getModVersion(mod));
            }

            // Deterministic packs: id
            //TODO: consider pack ordering and exclude dynamic packs
            var sortedPacks = new ArrayList<>(packs);
            sortedPacks.sort(Comparator.comparing(Pack::getId));
            for (var p : sortedPacks) tokens.add("pack=" + p.getId());

            try {
                var md = MessageDigest.getInstance("SHA-256");
                for (String t : tokens) {
                    md.update(t.getBytes(StandardCharsets.UTF_8));
                    md.update((byte) 0x1F);
                }
                byte[] d = md.digest();
                StringBuilder sb = new StringBuilder(d.length * 2);
                for (byte b : d) sb.append(String.format("%02x", b));
                return sb.toString();
            } catch (NoSuchAlgorithmException e) {
                // Fallback: deterministic string hash
                return Integer.toHexString(tokens.toString().hashCode());
            }
        }


        private void writeFingerprint(IEditablePackResources pack, String fp) {
            Path dir = getCachePath(pack);
            Path file = getCacheHashPath(pack);
            try {
                Files.createDirectories(dir);
                Files.writeString(file, fp, StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            } catch (Exception e) {
                Moonlight.LOGGER.debug("Failed writing cache fingerprint for {}: {}", pack, e.toString());
            }
        }


        private String readFingerprint(IEditablePackResources pack) {
            Path file = getCacheHashPath(pack);
            if (!Files.exists(file)) return "";
            try {
                return Files.readString(file, StandardCharsets.UTF_8).trim();
            } catch (Exception e) {
                Moonlight.LOGGER.debug("Failed reading cache fingerprint for {}: {}", pack, e.toString());
                return "";
            }
        }

        private Path getCacheHashPath(IEditablePackResources pack) {
            return getCachePath(pack)
                    .resolve("hash.txt");
        }


        public Path getCachePath(IEditablePackResources pack) {
            return getCachePath(pack.location(), pack.getPackType());
        }

        public Path getCachePath(PackLocationInfo packInfo, PackType type) {
            return PlatHelper.getGamePath().resolve("dynamic-packs-cache")
                    .resolve(type.getDirectory())
                    .resolve(packInfo.id().replace(":", "/"));
        }

        @Override
        public boolean needsRegeneration(IEditablePackResources packResources, Collection<Pack> loadedPacks) {
            String oldHash = readFingerprint(packResources);
            String newHash = computeCurrentFingerprint(loadedPacks);
            return oldHash != newHash;
        }

        @Override
        public IEditablePackResources createPackResources(PackLocationInfo info, PackType type, PackMetadataSection metadata) {
            //this editable pack resources will save sutf to file whenver its added to it
            return new CacheBackedPackResources(info, type, metadata, getCachePath(info));
        }

        @Override
        public void markRegenerated(IEditablePackResources packResources, Collection<Pack> loadedPacks) {
            //write new hash
            String newHash = computeCurrentFingerprint(loadedPacks);
            writeCacheHash(packResources, newHash);
        }
    };
}
