package net.mehvahdjukaar.moonlight.api.resources.textures;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.NativeImage;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.client.renderer.texture.atlas.sources.PalettedPermutations;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.IntUnaryOperator;

/** Rebuilds sprites that only exist through the paletted_permutations atlas source and have no png of their own. */
public final class PalettedPermutationsHelper {

    private static final Identifier PALETTED_PERMUTATIONS_TYPE =
            Identifier.withDefaultNamespace("paletted_permutations");

    // The resource manager is a fresh instance every reload, so its identity doubles as a reload token:
    // when it rotates we rebuild and keep only the latest index. invalidate() also drops it eagerly on reload.
    private static volatile ResourceManager cachedManager;
    private static volatile Map<Identifier, PalettedPermRecipe> cachedIndex;

    private record PalettedPermRecipe(Identifier base, Identifier paletteKey, Identifier permutation) {
    }

    /** Null if the sprite id is not a known permutation. */
    @Nullable
    public static TextureImage tryResolve(ResourceManager manager, Identifier spriteId) {
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

    public static void invalidate() {
        cachedManager = null;
        cachedIndex = null;
    }

    private static Map<Identifier, PalettedPermRecipe> getIndex(ResourceManager manager) {
        Map<Identifier, PalettedPermRecipe> index = cachedIndex;
        if (cachedManager == manager && index != null) return index; // fast path: same reload, no lock
        synchronized (PalettedPermutationsHelper.class) {
            if (cachedManager != manager || cachedIndex == null) {
                cachedIndex = buildIndex(manager); // scans all atlases exactly once per reload
                cachedManager = manager;
            }
            return cachedIndex;
        }
    }

    private static Map<Identifier, PalettedPermRecipe> buildIndex(ResourceManager manager) {
        Map<Identifier, PalettedPermRecipe> map = new HashMap<>();
        var atlases = manager.listResources("atlases", rl -> rl.getPath().endsWith(".json"));
        for (var entry : atlases.entrySet()) {
            try (var reader = entry.getValue().openAsReader()) {
                JsonObject json = GsonHelper.parse(reader);
                JsonArray sources = GsonHelper.getAsJsonArray(json, "sources", null);
                if (sources == null) continue;
                for (JsonElement el : sources) {
                    if (!el.isJsonObject()) continue;
                    JsonObject src = el.getAsJsonObject();
                    if (!PALETTED_PERMUTATIONS_TYPE.equals(Identifier.tryParse(GsonHelper.getAsString(src, "type", "")))) {
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

    private static void parseSource(JsonObject src, Map<Identifier, PalettedPermRecipe> map) {
        JsonArray textures = GsonHelper.getAsJsonArray(src, "textures");
        Identifier paletteKey = Identifier.parse(GsonHelper.getAsString(src, "palette_key"));
        JsonObject permutations = GsonHelper.getAsJsonObject(src, "permutations");

        for (Map.Entry<String, JsonElement> perm : permutations.entrySet()) {
            String suffix = "_" + perm.getKey();
            Identifier permPalette = Identifier.parse(perm.getValue().getAsString());
            for (JsonElement t : textures) {
                Identifier base = Identifier.parse(t.getAsString());
                // first definition wins, mirroring vanilla atlas source ordering
                map.putIfAbsent(base.withSuffix(suffix), new PalettedPermRecipe(base, paletteKey, permPalette));
            }
        }
    }
}
