package net.mehvahdjukaar.moonlight.api.resources.textures;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.serialization.JsonOps;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.client.renderer.texture.atlas.SpriteSources;
import net.minecraft.client.renderer.texture.atlas.sources.PalettedPermutations;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.IntUnaryOperator;

//Hack that is able to patch together a paletted permutation
public final class PalettedPermutationsHelper {

    private static volatile ResourceManager managerThatBuiltThisCache;
    private static volatile Map<Identifier, PalettedSprite> allPalettedSprites;

    private record PalettedSprite(Identifier base, Identifier paletteKey,
                                  Identifier permutationPalette) {
    }

    @Nullable
    public static TextureImage tryResolveImage(ResourceManager manager, Identifier spriteId) {
        PalettedSprite sprite = getPalettedSprite(manager, spriteId);
        if (sprite == null) return null;
        try {
            return applyPalette(manager, sprite);
        } catch (Exception e) {
            Moonlight.LOGGER.warn("Failed to reconstruct paletted-permutation texture {}: {}", spriteId, e.getMessage());
            return null;
        }
    }

    private static TextureImage applyPalette(ResourceManager manager, PalettedSprite sprite) throws IOException {
        int[] key = PalettedPermutations.loadPaletteEntryFromImage(manager, sprite.paletteKey());
        int[] perm = PalettedPermutations.loadPaletteEntryFromImage(manager, sprite.permutationPalette());
        IntUnaryOperator mapping = PalettedPermutations.createPaletteMapping(key, perm);
        try (TextureImage base = TextureImage.open(manager, sprite.base())) {
            NativeImage mapped = base.getImage().mappedCopy(mapping);
            return TextureImage.of(mapped, base.getMcMeta());
        }
    }

    public static void invalidate() {
        managerThatBuiltThisCache = null;
        allPalettedSprites = null;
    }

    private static @Nullable PalettedSprite getPalettedSprite(ResourceManager manager, Identifier spriteId) {
        return getAllPalettedSprites(manager).get(spriteId);
    }

    private static Map<Identifier, PalettedSprite> getAllPalettedSprites(ResourceManager manager) {
        Map<Identifier, PalettedSprite> index = allPalettedSprites;
        if (managerThatBuiltThisCache == manager && index != null) return index;
        synchronized (PalettedPermutationsHelper.class) {
            if (managerThatBuiltThisCache != manager || allPalettedSprites == null) {
                allPalettedSprites = scanPalettedSprites(manager);
                managerThatBuiltThisCache = manager;
            }
            return allPalettedSprites;
        }
    }

    //scan atlas and finx paletted perms
    private static Map<Identifier, PalettedSprite> scanPalettedSprites(ResourceManager manager) {
        Map<Identifier, PalettedSprite> map = new HashMap<>();
        var atlases = manager.listResources("atlases", rl -> rl.getPath().endsWith(".json"));
        for (var entry : atlases.entrySet()) {
            try (var reader = entry.getValue().openAsReader()) {
                JsonObject json = GsonHelper.parse(reader);
                JsonArray sources = GsonHelper.getAsJsonArray(json, "sources", null);
                if (sources == null) continue;
                for (JsonElement el : sources) {
                    var source = SpriteSources.CODEC.parse(JsonOps.INSTANCE, el).result().orElse(null);
                    if (source instanceof PalettedPermutations pp) addPermutations(pp, map);
                }
            } catch (Exception e) {
                Moonlight.LOGGER.warn("Failed to scan atlas {} for paletted permutations: {}", entry.getKey(), e.getMessage());
            }
        }
        if (!map.isEmpty()) {
            Moonlight.LOGGER.debug("Indexed {} virtual paletted-permutation sprites for recoloring", map.size());
        }
        return map;
    }

    private static void addPermutations(PalettedPermutations source, Map<Identifier, PalettedSprite> map) {
        for (var perm : source.permutations.entrySet()) {
            String suffix = "_" + perm.getKey();
            for (Identifier base : source.textures) {
                // last wins, same as vanilla Output.add
                map.put(base.withSuffix(suffix), new PalettedSprite(base, source.paletteKey, perm.getValue()));
            }
        }
    }
}
