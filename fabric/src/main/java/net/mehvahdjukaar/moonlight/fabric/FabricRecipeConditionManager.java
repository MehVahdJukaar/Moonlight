package net.mehvahdjukaar.moonlight.fabric;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class FabricRecipeConditionManager {

    private static final Map<ResourceLocation, Function<JsonObject, Boolean>> CONDITIONS = new HashMap<>();

    static {
        register(new ResourceLocation("forge:mod_loaded"), FabricRecipeConditionManager::forgeModLoaded);
        register(new ResourceLocation("forge:and"), FabricRecipeConditionManager::forgeAnd);
        register(new ResourceLocation("forge:not"), FabricRecipeConditionManager::forgeNot);
    }

    public static void register(ResourceLocation id, Function<JsonObject, Boolean> function) {
        CONDITIONS.put(id, function);
    }

    public static void registerSimple(ResourceLocation id, Function<String, Boolean> predicate) {
        register(id, j -> predicate.apply(j.get(id.getPath()).getAsString()));
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
            return c.apply(jo);
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

}
