package net.mehvahdjukaar.moonlight.core.client;

import com.mojang.blaze3d.platform.NativeImage;
import net.mehvahdjukaar.moonlight.api.client.gui.ModIcons;
import net.mehvahdjukaar.moonlight.api.resources.textures.SpriteUtils;
import net.mehvahdjukaar.moonlight.api.util.FileDownloadUtils;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

// icons fetched from a url for mods that aren't installed. Downloaded off thread, texture created on the render
// thread. Null until ready (or forever if it failed)
public final class RemoteIconCache {

    // Optional is present-and-empty for "gave up", absent-from-map for "not requested / still loading".
    private static final Map<String, Optional<ModIcons.Icon>> CACHE = new ConcurrentHashMap<>();

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
                NativeImage image = SpriteUtils.readImage(bytes);
                Minecraft mc = Minecraft.getInstance();
                mc.execute(() -> {
                    try {
                        Identifier id = Moonlight.res("remote_mod_icon/"
                                + key.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_.-]", "_"));
                        mc.getTextureManager().register(id, new DynamicTexture(id::toString, image));
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
