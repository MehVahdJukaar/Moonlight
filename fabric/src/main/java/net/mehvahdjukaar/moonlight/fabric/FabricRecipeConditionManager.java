package net.mehvahdjukaar.moonlight.fabric;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

public class FabricRecipeConditionManager {

    private static final Map<ResourceLocation, Predicate<JsonObject>> CONDITIONS = new HashMap<>();

    static {
        register(new ResourceLocation("forge:mod_loaded"), FabricRecipeConditionManager::forgeModLoaded);
        register(new ResourceLocation("forge:and"), FabricRecipeConditionManager::forgeAnd);
        register(new ResourceLocation("forge:not"), FabricRecipeConditionManager::forgeNot);
        register(new ResourceLocation("forge:tag_empty"), FabricRecipeConditionManager::forgeTagEmpty);
    }


    public static void register(ResourceLocation id, Predicate<JsonObject> function) {
        CONDITIONS.put(id, function);
    }

    public static void registerSimple(ResourceLocation id, Predicate<String> predicate) {
        register(id, j -> predicate.test(j.get(id.getPath()).getAsString()));
    }

    public static boolean isRecipeDisabled(@Nullable JsonElement conditions, ResourceLocation recipeId) {
        try {
            if (!areConditionsMet(conditions)) return true;
        } catch (Exception e) {
            if (PlatformHelper.isDev()) {
                Moonlight.LOGGER.error("Failed to parse conditions for recipe {}", recipeId, e);
            }
            return false;
        }
        return false;
    }

    public static boolean areConditionsMet(@Nullable JsonElement conditions) {
        if (conditions instanceof JsonArray array) {
            for (var v : array) {
                if (v instanceof JsonObject jo) {
                    if (!isConditionSatisfied(jo)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static boolean isConditionSatisfied(JsonObject jo) {
        var type = jo.get("type").getAsString();
        ResourceLocation res = new ResourceLocation(type);
        var c = CONDITIONS.get(res);
        if (c != null) {
            return c.test(jo);
        }
        return true;
    }

    private static boolean forgeNot(JsonObject jsonObject) {
        JsonObject jo = jsonObject.getAsJsonObject("value");
        return !isConditionSatisfied(jo);
    }

    private static boolean forgeAnd(JsonObject jsonObject) {
        JsonArray jo = jsonObject.getAsJsonArray("values");
        return areConditionsMet(jo);
    }

    private static boolean forgeModLoaded(JsonObject jsonObject) {
        return PlatformHelper.isModLoaded(jsonObject.get("modid").getAsString());
    }

    private static Boolean forgeTagEmpty(JsonObject object) {
        var key = TagKey.create(Registries.ITEM, new ResourceLocation(GsonHelper.getAsString(object, "tag")));
        var tagContext = FabricHooks.getTagContext();
        if (tagContext != null) {
            return tagContext.getAllTags(key.registry()).getOrDefault(key.location(), Set.of()).isEmpty();
        }
        return true;
    }

}
