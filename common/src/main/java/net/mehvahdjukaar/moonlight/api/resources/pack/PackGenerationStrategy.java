package net.mehvahdjukaar.moonlight.api.resources.pack;

import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.pack.DynamicResourcesInternals;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
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

    boolean needsRegeneration(IEditablePackResources packResources, Collection<PackResources> loadedPacks);

    void beforeRegenerate(IEditablePackResources packResources, Collection<PackResources> loadedPacks);

    IEditablePackResources createPackResources(PackLocationInfo info, PackType type);


    PackGenerationStrategy REGEN_ON_EVERY_RELOAD = new PackGenerationStrategy() {

        @Override
        public boolean needsRegeneration(IEditablePackResources packResources, Collection<PackResources> loadedPacks) {
            return true;
        }

        @Override
        public void beforeRegenerate(IEditablePackResources pack, Collection<PackResources> loadedPacks) {
            // no cache
        }

        @Override
        public IEditablePackResources createPackResources(PackLocationInfo info, PackType type) {
            return new InMemoryPackResources(info, type);
        }
    };

    PackGenerationStrategy RUN_ONCE = new PackGenerationStrategy() {
        @Override
        public boolean needsRegeneration(IEditablePackResources packResources, Collection<PackResources> loadedPacks) {
            return packResources.isEmpty();
        }

        @Override
        public void beforeRegenerate(IEditablePackResources pack, Collection<PackResources> loadedPacks) {
            // no cache
        }

        @Override
        public IEditablePackResources createPackResources(PackLocationInfo info, PackType type) {
            return new InMemoryPackResources(info, type);
        }
    };

    PackGenerationStrategy CACHED = new SimpleCached();


    class SimpleCached implements PackGenerationStrategy {

        private static String computeCurrentFingerprint(Collection<PackResources> packs) {
            List<String> tokens = computeTokens(packs);

            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
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

        private static @NotNull List<String> computeTokens(Collection<PackResources> packs) {
            List<String> tokens = new ArrayList<>();
            boolean fabric = PlatHelper.getPlatform().isFabric();

            // 1) Packs: keep the given order (order-sensitive)
            int i = 0;
            for (PackResources p : packs) {
                String id = p.packId();
                if (DynamicResourcesInternals.isKnownDynamicPack(p.location().id())) continue;
                if (id.startsWith("mod/")) continue;
                if (fabric && id.startsWith("fabric")) continue;
                if (id.startsWith("generated")) continue;
                String description = "";
                try {
                    PackMetadataSection metadataSection = p.getMetadataSection(PackMetadataSection.TYPE);
                    if (metadataSection != null) {
                        description = metadataSection.description().getString();
                    }
                } catch (Exception ignored) {
                }
                tokens.add("pack[" + (i++) + "]=" + id + "@" + description);
            }
            // 2) Mods: order-independent (sort deterministically)
            List<String> modTokens = new ArrayList<>();
            for (String mod : PlatHelper.getInstalledMods()) {
                if (fabric && mod.startsWith("fabric")) continue;
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
            return PlatHelper.getGamePath().resolve("dynamic-" +
                            (type == PackType.CLIENT_RESOURCES ? "resource" : "data")
                            + "-pack-cache")
                    .resolve(packInfo.id().replace(":", "-"));
        }

        @Override
        public boolean needsRegeneration(IEditablePackResources packResources, Collection<PackResources> loadedPacks) {
            String oldHash = readFingerprint(packResources);
            String newHash = computeCurrentFingerprint(loadedPacks);
            return !oldHash.equals(newHash);
        }

        @Override
        public IEditablePackResources createPackResources(PackLocationInfo info, PackType type) {
            //this editable pack resources will save sutf to file whenver its added to it
            return new CacheBackedPackResources(info, type, getCachePath(info, type));
        }

        @Override
        public void beforeRegenerate(IEditablePackResources packResources, Collection<PackResources> loadedPacks) {
            //write new hash
            String newHash = computeCurrentFingerprint(loadedPacks);
            writeFingerprint(packResources, newHash);
        }
    }


}
