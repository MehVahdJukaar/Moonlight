package net.mehvahdjukaar.moonlight.api.client.gui;

import com.mojang.blaze3d.platform.NativeImage;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.resources.textures.SpriteUtils;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Locale;

/**
 * Loads mod icons out of each mod's jar (through PlatHelper.getModIcon) and keeps them around as GUI textures, so
 * screens can draw a mod's logo. Returns null for mods that don't declare one, and callers draw something else.
 * Each mod id is loaded once, on the render thread, and its texture stays until the game closes.
 */
public final class ModIcons {

    /** A loaded icon texture plus its pixel size (icons aren't always square). */
    public record Icon(ResourceLocation texture, int width, int height) {
    }

    private static final Map<String, Optional<Icon>> CACHE = new HashMap<>();

    @Nullable
    public static Icon get(String modId) {
        return CACHE.computeIfAbsent(modId, ModIcons::load).orElse(null);
    }

    private static Optional<Icon> load(String modId) {
        try {
            Path path = PlatHelper.getModIcon(modId);
            if (path == null) return Optional.empty();
            NativeImage image = SpriteUtils.readImage(Files.readAllBytes(path));
            ResourceLocation id = Moonlight.res("mod_icon/" + modId.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_.-]", "_"));
            Minecraft.getInstance().getTextureManager().register(id, new DynamicTexture(image));
            return Optional.of(new Icon(id, image.getWidth(), image.getHeight()));
        } catch (Exception e) {
            Moonlight.LOGGER.warn("Failed to load mod icon for {}", modId, e);
            return Optional.empty();
        }
    }
}
