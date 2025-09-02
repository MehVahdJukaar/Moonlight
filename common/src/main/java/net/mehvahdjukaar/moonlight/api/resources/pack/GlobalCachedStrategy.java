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

public class GlobalCachedStrategy implements PackGenerationStrategy {

    public static final GlobalCachedStrategy INSTANCE = new GlobalCachedStrategy();

    private final ThreadLocal<Boolean> needsRegeneration = ThreadLocal.withInitial(() -> true);

    public void refreshState(PackType packType, Collection<PackResources> loadedPacks) {
        String oldHash = readFingerprint(packType);
        String newHash = computeCurrentFingerprint(loadedPacks);
        needsRegeneration.set(!oldHash.equals(newHash));

        //write new state
        writeFingerprint(packType, newHash);
    }

    @Override
    public boolean needsRegeneration() {
        return needsRegeneration.get();
    }

    protected static String computeCurrentFingerprint(Collection<PackResources> packs) {
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

    protected static @NotNull List<String> computeTokens(Collection<PackResources> packs) {
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

    protected void writeFingerprint(PackType packType, String fp) {
        Path dir = getCachePath(packType);
        Path file = getCacheHashPath(packType);
        try {
            Files.createDirectories(dir);
            Files.writeString(file, fp, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (Exception e) {
            Moonlight.LOGGER.debug("Failed writing cache fingerprint for {}: {}", packType, e.toString());
        }
    }

    protected String readFingerprint(PackType packType) {
        Path file = getCacheHashPath(packType);
        if (!Files.exists(file)) return "";
        try {
            return Files.readString(file, StandardCharsets.UTF_8).trim();
        } catch (Exception e) {
            Moonlight.LOGGER.debug("Failed reading cache fingerprint for {}: {}", packType, e.toString());
            return "";
        }
    }

    protected Path getCacheHashPath(PackType packType) {
        return getCachePath(packType).resolve("hash.txt");
    }

    protected Path getCachePath(PackType type) {
        return PlatHelper.getGamePath().resolve("dynamic-" +
                (type == PackType.CLIENT_RESOURCES ? "resource" : "data")
                + "-pack-cache");
    }

    @Override
    public IEditablePackResources createPackResources(PackLocationInfo info, PackType type) {
        //this editable pack resources will save sutf to file whenver its added to it
        return new CacheBackedPackResources(info, type, getCachePath(type)
                .resolve(info.id().replace(":", "-")));
    }
}
