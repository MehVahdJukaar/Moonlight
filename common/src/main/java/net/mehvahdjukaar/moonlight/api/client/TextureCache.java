package net.mehvahdjukaar.moonlight.api.client;

import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.moonlight.api.resources.ResType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

//used for quick access when recoloring textures
public class TextureCache {

    /**
     * Hacky method to add weird textures for blocks that have ones with odd names that might not get picked up.
     * Useful for block sets to specify exactly which texture to use for their needs
     *
     * @param block       target block
     * @param id          id that will be used to identify this texture. needs to match the criteria that are used to normally identify textures inside a model
     * @param texturePath actual texture location. It is not its absolute path so no :textures/
     */
    public static void registerSpecialTextureForBlock(ItemLike block, String id, ResourceLocation texturePath) {
        SPECIAL_TEXTURES.computeIfAbsent(block, b -> new HashSet<>()).add(new Pair<>(id, texturePath.toString()));
    }

    private static final Map<ItemLike, Set<Pair<String, String>>> SPECIAL_TEXTURES = new IdentityHashMap<>();

    private static final Map<ItemLike, Set<String>> CACHED_TEXTURES = new IdentityHashMap<>();

    public static void clear() {
        CACHED_TEXTURES.clear();
    }

    @Nullable
    public static String getCached(ItemLike block, Predicate<String> texturePredicate) {
        var special = SPECIAL_TEXTURES.get(block);
        if (special != null) {
            for (var e : special) {
                if (texturePredicate.test(e.getFirst())) return e.getSecond();
            }
        }
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
