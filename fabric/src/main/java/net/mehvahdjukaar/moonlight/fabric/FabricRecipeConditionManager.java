package net.mehvahdjukaar.moonlight.fabric;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.fabricmc.fabric.impl.resource.conditions.ResourceConditionsImpl;
import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

public class FabricRecipeConditionManager {

    private static final Map<ResourceLocation, Function<JsonObject, Boolean>> CONDITIONS = new HashMap<>();

    static {
        register(new ResourceLocation("forge:mod_loaded"), FabricRecipeConditionManager::forgeModLoaded);
        register(new ResourceLocation("forge:and"), FabricRecipeConditionManager::forgeAnd);
        register(new ResourceLocation("forge:not"), FabricRecipeConditionManager::forgeNot);
        register(new ResourceLocation("forge:tag_empty"), FabricRecipeConditionManager::forgeTagEmpty);
    }


    public static void register(ResourceLocation id, Function<JsonObject, Boolean> function) {
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

    private static Boolean forgeTagEmpty(JsonObject object) {
        var id= new ResourceLocation(GsonHelper.getAsString(object, "tag"));
        Map<ResourceKey<?>, Map<ResourceLocation, Collection<Holder<?>>>> allTags = ResourceConditionsImpl.LOADED_TAGS.get();

        if (allTags == null) {
            Moonlight.LOGGER.warn("Can't retrieve deserialized tags. Failing tags_populated resource condition check.");
            return true;
        }
        Map<ResourceLocation, Collection<Holder<?>>> registryTags = allTags.get(Registry.ITEM_REGISTRY);
        if (registryTags == null) {
            // No tag for this registry
            return false;
        }
        Collection<Holder<?>> tags = registryTags.get(id);

        return tags != null && !tags.isEmpty();
    }

}
