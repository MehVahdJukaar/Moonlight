package net.mehvahdjukaar.moonlight.api.resources.textures;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.NativeImage;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.client.renderer.texture.atlas.sources.PalettedPermutations;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.IntUnaryOperator;

/**
 * Reconstructs textures that a mod (or vanilla) only exposes through the vanilla
 * {@code paletted_permutations} atlas sprite source. Those repaletted sprites are virtual: they have
 * no backing png and are baked straight into an atlas at stitch time, so {@link SpriteUtils#readImage}
 * cannot find them. Their three ingredients (grayscale base, palette key, permutation palette) are however
 * real resources, so we can re-run the exact vanilla recipe here and hand back a normal {@link TextureImage}
 * that the recoloring pipeline can consume like any other texture.
 * <p>
 * The atlas definitions are scanned once per {@link ResourceManager} (i.e. once per reload, and only the
 * first time a lookup actually misses) into a reverse index; every subsequent resolve is an O(1) map hit.
 */
public final class PalettedPermutationsHelper {

    private static final ResourceLocation PALETTED_PERMUTATIONS_TYPE =
            ResourceLocation.withDefaultNamespace("paletted_permutations");

    // The resource manager is a fresh instance every reload, so its identity doubles as a reload token:
    // when it rotates we rebuild and keep only the latest index. invalidate() also drops it eagerly on reload.
    private static volatile ResourceManager cachedManager;
    private static volatile Map<ResourceLocation, PalettedPermRecipe> cachedIndex;

    /**
     * The ingredients of a single virtual permutation, all of them concrete resources.
     *
     * @param base        grayscale base texture id (sprite id form, no {@code textures/} prefix / {@code .png})
     * @param paletteKey  the reference grayscale palette id
     * @param permutation the target color palette id
     */
    private record PalettedPermRecipe(ResourceLocation base, ResourceLocation paletteKey, ResourceLocation permutation) {
    }

    /**
     * Tries to reconstruct a virtual paletted-permutation sprite.
     *
     * @param spriteId the sprite id (relative texture path, no {@code textures/} prefix or {@code .png})
     * @return a freshly baked {@link TextureImage}, or {@code null} if this id is not a known permutation
     * (in which case the caller should treat the texture as genuinely missing)
     */
    @Nullable
    public static TextureImage tryResolve(ResourceManager manager, ResourceLocation spriteId) {
        PalettedPermRecipe recipe = getIndex(manager).get(spriteId);
        if (recipe == null) return null;
        try {
            return bake(manager, recipe);
        } catch (Exception e) {
            Moonlight.LOGGER.warn("Failed to reconstruct paletted-permutation texture {}: {}", spriteId, e.getMessage());
            return null;
        }
    }

    private static TextureImage bake(ResourceManager manager, PalettedPermRecipe recipe) throws IOException {
        int[] key = PalettedPermutations.loadPaletteEntryFromImage(manager, recipe.paletteKey());
        int[] perm = PalettedPermutations.loadPaletteEntryFromImage(manager, recipe.permutation());
        IntUnaryOperator mapping = PalettedPermutations.createPaletteMapping(key, perm);
        // read the base (real texture) through the normal path so we also inherit its animation metadata
        try (TextureImage base = TextureImage.open(manager, recipe.base())) {
            NativeImage mapped = base.getImage().mappedCopy(mapping);
            return TextureImage.of(mapped, base.getMcMeta());
        }
    }

    /**
     * Drops the cached index. Called on client resource reload (next to {@code TextureCache.clear()}) so nothing is
     * retained between reloads. Correctness across the reload itself is guaranteed independently by the
     * manager-identity check in {@link #getIndex}, since this cache is consumed at generation time (earlier).
     */
    public static void invalidate() {
        cachedManager = null;
        cachedIndex = null;
    }

    private static Map<ResourceLocation, PalettedPermRecipe> getIndex(ResourceManager manager) {
        Map<ResourceLocation, PalettedPermRecipe> index = cachedIndex;
        if (cachedManager == manager && index != null) return index; // fast path: same reload, no lock
        synchronized (PalettedPermutationsHelper.class) {
            if (cachedManager != manager || cachedIndex == null) {
                cachedIndex = buildIndex(manager); // scans all atlases exactly once per reload
                cachedManager = manager;
            }
            return cachedIndex;
        }
    }

    private static Map<ResourceLocation, PalettedPermRecipe> buildIndex(ResourceManager manager) {
        Map<ResourceLocation, PalettedPermRecipe> map = new HashMap<>();
        var atlases = manager.listResources("atlases", rl -> rl.getPath().endsWith(".json"));
        for (var entry : atlases.entrySet()) {
            try (var reader = entry.getValue().openAsReader()) {
                JsonObject json = GsonHelper.parse(reader);
                JsonArray sources = GsonHelper.getAsJsonArray(json, "sources", null);
                if (sources == null) continue;
                for (JsonElement el : sources) {
                    if (!el.isJsonObject()) continue;
                    JsonObject src = el.getAsJsonObject();
                    if (!PALETTED_PERMUTATIONS_TYPE.equals(ResourceLocation.tryParse(GsonHelper.getAsString(src, "type", "")))) {
                        continue;
                    }
                    parseSource(src, map);
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

    private static void parseSource(JsonObject src, Map<ResourceLocation, PalettedPermRecipe> map) {
        JsonArray textures = GsonHelper.getAsJsonArray(src, "textures");
        ResourceLocation paletteKey = ResourceLocation.parse(GsonHelper.getAsString(src, "palette_key"));
        JsonObject permutations = GsonHelper.getAsJsonObject(src, "permutations");

        for (Map.Entry<String, JsonElement> perm : permutations.entrySet()) {
            String suffix = "_" + perm.getKey();
            ResourceLocation permPalette = ResourceLocation.parse(perm.getValue().getAsString());
            for (JsonElement t : textures) {
                ResourceLocation base = ResourceLocation.parse(t.getAsString());
                // first definition wins, mirroring vanilla atlas source ordering
                map.putIfAbsent(base.withSuffix(suffix), new PalettedPermRecipe(base, paletteKey, permPalette));
            }
        }
    }
}
