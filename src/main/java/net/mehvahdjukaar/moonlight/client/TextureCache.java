package net.mehvahdjukaar.moonlight.client;

import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

//used for quick access when recoloring textures
public class TextureCache {

    private static final Map<ItemLike, Set<String>> CACHED_TEXTURES = new HashMap<>();

    public static void refresh() {
        CACHED_TEXTURES.clear();
    }

    @Nullable
    public static String getCached(ItemLike block, Predicate<String> texturePredicate) {
        var list = CACHED_TEXTURES.get(block);
        if (list != null) {
            for (var e : list) {
                if (texturePredicate.test(e)) return e;
            }
        }
        return null;
    }

    public static void add(ItemLike block, String t) {
        CACHED_TEXTURES.computeIfAbsent(block, b -> new HashSet<>()).add(t);
    }

}
