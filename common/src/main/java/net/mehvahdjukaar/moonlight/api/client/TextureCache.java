package net.mehvahdjukaar.moonlight.api.client;

import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.moonlight.api.misc.TriResult;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

//used for quick access when recoloring textures
public class TextureCache {

    /**
     * Hacky method to add weird textures for blocks that have ones with odd names that might not get picked up.
     * Useful for block sets to specify exactly which texture to use for their needs
     *
     * Takes the block id so it can be called before the block is registered, or for blocks of mods that aren't loaded.
     *
     * @param blockId     target block id
     * @param id          id that will be used to identify this texture. needs to match the criteria that are used to normally identify textures inside a model
     * @param texturePath actual texture location. It is not its absolute path so no :textures/
     */
    public static void registerSpecialTextureForBlock(ResourceLocation blockId, String id, ResourceLocation texturePath) {
        SPECIAL_BLOCK_TEXTURES.computeIfAbsent(blockId, b -> ConcurrentHashMap.newKeySet()).add(new Pair<>(id, texturePath.toString()));
    }

    @Deprecated(forRemoval = true)
    public static void registerSpecialTextureForBlock(ItemLike block, String id, ResourceLocation texturePath) {
        if (block instanceof Block b) {
            registerSpecialTextureForBlock(BuiltInRegistries.BLOCK.getKey(b), id, texturePath);
        } else {
            SPECIAL_TEXTURES.computeIfAbsent(block, b -> ConcurrentHashMap.newKeySet()).add(new Pair<>(id, texturePath.toString()));
        }
    }
    //lazy reg map
    private static final Map<ResourceLocation, Set<Pair<String, String>>> SPECIAL_BLOCK_TEXTURES = new ConcurrentHashMap<>();

    //live ones
    private static final Map<ItemLike, Set<Pair<String, String>>> SPECIAL_TEXTURES = new ConcurrentHashMap<>();
    private static final Map<ItemLike, Set<String>> CACHED_TEXTURES = new ConcurrentHashMap<>();

    public static void clear() {
        CACHED_TEXTURES.clear();
    }

    @Deprecated(forRemoval = true)
    @Nullable
    public static String getCached(ItemLike block, Predicate<String> texturePredicate) {
        return getCachedTexture(block, texturePredicate).getObject();
    }

    public static TriResult<String> getCachedTexture(ItemLike block, Predicate<String> texturePredicate) {
        Set<Pair<String, String>> special;
        if (block instanceof Block b) {
            special = SPECIAL_BLOCK_TEXTURES.isEmpty() ? null : SPECIAL_BLOCK_TEXTURES.get(BuiltInRegistries.BLOCK.getKey(b));
        } else special = SPECIAL_TEXTURES.get(block);
        if (special != null) {
            for (var e : special) {
                if (texturePredicate.test(e.getFirst())){
                    return TriResult.success(e.getSecond());
                }
            }
        }
        var list = CACHED_TEXTURES.get(block);
        if (list != null) {
            for (var e : list) {
                if (texturePredicate.test(e)) return TriResult.success(e);
            }
            return TriResult.fail();
        }
        return TriResult.pass();
    }

    public static void add(ItemLike block, String t) {
        CACHED_TEXTURES.computeIfAbsent(block, b -> ConcurrentHashMap.newKeySet()).add(t);
    }

    public static void addAll(ItemLike block, Collection<String> textures) {
        Set<String> filled = ConcurrentHashMap.newKeySet();
        filled.addAll(textures);
        CACHED_TEXTURES.merge(block, filled, (old, added) -> {
            old.addAll(added);
            return old;
        });
    }

}
