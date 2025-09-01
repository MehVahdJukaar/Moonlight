package net.mehvahdjukaar.moonlight.api.resources.pack;

import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.Pack;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

//very ugly and confused class
public interface PackGenerationStrategy {

    boolean needsRegeneration(IEditablePackResources packResources, Collection<Pack> loadedPacks);

    void afterRegenerate(IEditablePackResources packResources, Collection<Pack> loadedPacks);

    IEditablePackResources createPackResources(PackLocationInfo info, PackType type, PackMetadataSection metadata);


    PackGenerationStrategy REGEN_ON_EVERY_RELOAD = new PackGenerationStrategy() {

        @Override
        public boolean needsRegeneration(IEditablePackResources packResources, Collection<Pack> loadedPacks) {
            return true;
        }

        @Override
        public void afterRegenerate(IEditablePackResources pack, Collection<Pack> loadedPacks) {
            // no cache
        }

        @Override
        public IEditablePackResources createPackResources(PackLocationInfo info, PackType type, PackMetadataSection metadata) {
            return new InMemoryPackResources(info, type, metadata);
        }
    };

    PackGenerationStrategy RUN_ONCE = new PackGenerationStrategy() {
        @Override
        public boolean needsRegeneration(IEditablePackResources packResources, Collection<Pack> loadedPacks) {
            return packResources.isEmpty();
        }

        @Override
        public void afterRegenerate(IEditablePackResources pack, Collection<Pack> loadedPacks) {
            // no cache
        }

        @Override
        public IEditablePackResources createPackResources(PackLocationInfo info, PackType type, PackMetadataSection metadata) {
            return new InMemoryPackResources(info, type, metadata);
        }
    };

    PackGenerationStrategy CACHED = new SimpleCached();


    class SimpleCached implements PackGenerationStrategy {

        private static String computeCurrentFingerprint(Collection<Pack> packs) {
            List<String> tokens = computeTokens(packs);

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

        private static @NotNull List<String> computeTokens(Collection<Pack> packs) {
            List<String> tokens = new ArrayList<>();

            // 1) Packs: keep the given order (order-sensitive)
            int i = 0;
            for (Pack p : packs) {
                String id = p.getId();
                if (p.getPackSource() instanceof DynamicResourcesProvider) continue;
                if (id.startsWith("mod/")) continue;
                tokens.add("pack[" + (i++) + "]=" + id);
            }

            // 2) Mods: order-independent (sort deterministically)
            List<String> modTokens = new ArrayList<>();
            for (String mod : PlatHelper.getInstalledMods()) {
                modTokens.add(mod + "@" + PlatHelper.getModVersion(mod));
            }
            Collections.sort(modTokens); // normalize any iteration order
            tokens.addAll(modTokens);

            return tokens;
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
            return !oldHash.equals(newHash);
        }

        @Override
        public IEditablePackResources createPackResources(PackLocationInfo info, PackType type, PackMetadataSection metadata) {
            //this editable pack resources will save sutf to file whenver its added to it
            return new CacheBackedPackResources(info, type, metadata, getCachePath(info, type));
        }

        @Override
        public void afterRegenerate(IEditablePackResources packResources, Collection<Pack> loadedPacks) {
            //write new hash
            String newHash = computeCurrentFingerprint(loadedPacks);
            writeFingerprint(packResources, newHash);
        }
    }


}
