package net.mehvahdjukaar.moonlight.core.client;

import com.mojang.blaze3d.platform.NativeImage;
import net.mehvahdjukaar.moonlight.api.client.gui.ModIcons;
import net.mehvahdjukaar.moonlight.api.util.FileDownloadUtils;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Loads and caches mod icons fetched from a URL (for mods that aren't installed, so their jar isn't around to pull an
 * icon from like {@link ModIcons} does). The bytes are downloaded off-thread; the {@link DynamicTexture} is created and
 * registered on the render thread. Callers get {@code null} until the icon is ready (or forever, if it failed) and
 * should draw a fallback in the meantime. Only PNGs decode - anything else just falls back.
 */
public final class RemoteIconCache {

    // Optional is present-and-empty for "gave up", absent-from-map for "not requested / still loading".
    private static final Map<String, Optional<ModIcons.Icon>> CACHE = new ConcurrentHashMap<>();

    /**
     * @param key a stable id for this icon (the mod id), used both for caching and the texture path
     * @param url where to fetch the PNG from
     * @return the loaded icon, or {@code null} while it's loading or if it failed
     */
    @Nullable
    public static ModIcons.Icon get(String key, String url) {
        Optional<ModIcons.Icon> cached = CACHE.get(key);
        if (cached != null) return cached.orElse(null);
        // reserve the slot so only the first caller starts a download
        if (CACHE.putIfAbsent(key, Optional.empty()) == null) {
            startLoad(key, url);
        }
        return null;
    }

    private static void startLoad(String key, String url) {
        Thread t = new Thread(() -> {
            try {
                byte[] bytes = FileDownloadUtils.readBytes(url);
                NativeImage image = NativeImage.read(new ByteArrayInputStream(bytes));
                Minecraft mc = Minecraft.getInstance();
                mc.execute(() -> {
                    try {
                        ResourceLocation id = Moonlight.res("remote_mod_icon/"
                                + key.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_.-]", "_"));
                        mc.getTextureManager().register(id, new DynamicTexture(image));
                        CACHE.put(key, Optional.of(new ModIcons.Icon(id, image.getWidth(), image.getHeight())));
                    } catch (Exception e) {
                        image.close();
                        Moonlight.LOGGER.warn("Failed to register remote icon for {}", key, e);
                    }
                });
            } catch (Exception e) {
                Moonlight.LOGGER.warn("Failed to fetch remote icon for {} from {}: {}", key, url, e.toString());
            }
        }, "Moonlight Icon Fetcher " + key);
        t.setDaemon(true);
        t.start();
    }
}
