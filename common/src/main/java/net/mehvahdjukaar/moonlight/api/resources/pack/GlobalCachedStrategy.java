package net.mehvahdjukaar.moonlight.api.resources.pack;

import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.util.math.MthUtils;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.misc.FilteredResManager;
import net.mehvahdjukaar.moonlight.core.pack.DynamicResourcesInternals;
import net.mehvahdjukaar.moonlight.core.pack.MergedDynamicClientResourcesProvider;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;

public abstract class GlobalCachedStrategy implements PackGenerationStrategy {

    private static final Map<PackType, Boolean> NEEDS_REGEN = new HashMap<>();

    private static final Map<PackType, String> LAST_KNOWN_HASH = new HashMap<>();

    public static void refreshState(PackType packType, Collection<PackResources> loadedPacks) {
        String oldHash = readFingerprint(packType);
        String newHash = computeCurrentFingerprint(loadedPacks);
        boolean shouldRegen = !oldHash.equals(newHash);
        Moonlight.LOGGER.info("Resource cache state for {}: {}", packType, shouldRegen ? "needs regeneration" : "up to date");
        NEEDS_REGEN.put(packType, shouldRegen);
        LAST_KNOWN_HASH.put(packType, newHash);

    }

    public static void writeNewState(PackType packType) {
        String newHash = LAST_KNOWN_HASH.get(packType);
        if (newHash != null) {
            writeFingerprint(packType, newHash);
            NEEDS_REGEN.put(packType, false);
        }
    }

    private static void writeFingerprint(PackType packType, String fp) {
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

    private static String readFingerprint(PackType packType) {
        Path file = getCacheHashPath(packType);
        if (!Files.exists(file)) return "";
        try {
            return Files.readString(file, StandardCharsets.UTF_8).trim();
        } catch (Exception e) {
            Moonlight.LOGGER.debug("Failed reading cache fingerprint for {}: {}", packType, e.toString());
            return "";
        }
    }

    private static Path getCacheHashPath(PackType packType) {
        return getCachePath(packType).resolve("hash.txt");
    }

    private static Path getCachePath(PackType type) {
        return PlatHelper.getGamePath().resolve("dynamic-" +
                (type == PackType.CLIENT_RESOURCES ? "resource" : "data")
                + "-pack-cache");
    }

    private static String computeCurrentFingerprint(Collection<PackResources> packs) {
        return MthUtils.sha256Digest(computeTokens(packs));
    }

    private static @NotNull List<String> computeTokens(Collection<PackResources> packs) {
        List<String> tokens = new ArrayList<>();
        boolean fabric = PlatHelper.getPlatform().isFabric();

        // 1) Packs: keep the given order (order-sensitive)
        int i = 0;
        for (PackResources p : packs) {
            String id = p.packId();
            if(FilteredResManager.isDynamicPackResource(p)) continue;
            if(FilteredResManager.isModResourcePack(p)) continue;

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

    @Override
    public boolean needsRegeneration(PackType packType) {
        return NEEDS_REGEN.get(packType);
    }

    protected Path getPath(PackType type) {
        return GlobalCachedStrategy.getCachePath(type);
    }

    @Override
    public IEditablePackResources createPackResources(PackLocationInfo info, PackType type) {
        //this editable pack resources will save sutf to file whenver its added to it
        return new CachePathPackResources(info, type, getPath(type)
                .resolve(info.id().replace(":", "-")));
    }

    @Override
    public String toString() {
        return "CACHED";
    }
}
