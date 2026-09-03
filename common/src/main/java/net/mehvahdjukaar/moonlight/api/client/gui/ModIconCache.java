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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Locale;

public final class ModIconCache {

    private static final Map<String, Optional<Icon>> CACHE = new HashMap<>();

    // plenty of mods never declare a logo, but most of them still ship one of these somewhere in the jar
    private static final List<String> GUESSED_ICON_PATHS = List.of(
            "icon.png", "logo.png", "%s.png", "%s-icon.png", "%s_icon.png", "%s-logo.png", "%s_logo.png",
            "assets/%s/icon.png", "assets/%s/logo.png", "pack.png");

    @Nullable
    public static Icon get(String modId) {
        return CACHE.computeIfAbsent(modId, ModIconCache::load).orElse(null);
    }

    private static Optional<Icon> load(String modId) {
        try {
            Path path = PlatHelper.getModIcon(modId);
            if (path == null) path = guessIcon(modId);
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

    @Nullable
    private static Path guessIcon(String modId) {
        for (String candidate : GUESSED_ICON_PATHS) {
            Path path = PlatHelper.findModResource(modId, candidate.formatted(modId));
            if (path != null) return path;
        }
        return null;
    }
}
