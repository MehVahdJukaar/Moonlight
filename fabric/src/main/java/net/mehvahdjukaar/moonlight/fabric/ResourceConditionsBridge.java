package net.mehvahdjukaar.moonlight.fabric;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.fabricmc.fabric.impl.resource.conditions.ResourceConditionsImpl;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Items;

import java.util.Collection;
import java.util.Map;
import java.util.function.Predicate;

public class ResourceConditionsBridge {

    public static final String CONDITIONS_KEY = "conditions";
    public static final String NON_RECIPE_CONDITIONS_KEY = "global_conditions";
    public static final String CONDITION_ID = "type";

    public static void init() {
        try {
            ResourceConditions.register(new ResourceLocation("forge:not"), ResourceConditionsBridge::forgeNot);
        } catch (Exception ignored) {

        }
        try {
            ResourceConditions.register(new ResourceLocation("forge:and"), ResourceConditionsBridge::forgeAnd);
        } catch (Exception ignored) {
        }

        try {
            ResourceConditions.register(new ResourceLocation("forge:mod_loaded"), ResourceConditionsBridge::forgeModLoaded);
        } catch (Exception ignored) {
        }

        try {
            ResourceConditions.register(new ResourceLocation("forge:tag_empty"), ResourceConditionsBridge::forgeTagEmpty);
        } catch (Exception ignored) {
        }
    }

    private static boolean forgeNot(JsonObject jsonObject) {
        JsonObject jo = GsonHelper.getAsJsonObject(jsonObject, "value");
        return !conditionMatches(jo);
    }

    private static boolean forgeAnd(JsonObject jsonObject) {
        JsonArray jo = GsonHelper.getAsJsonArray(jsonObject, "values");
        return conditionsMatch(jo, true);
    }

    private static boolean forgeModLoaded(JsonObject jsonObject) {
        return PlatHelper.isModLoaded(GsonHelper.getAsString(jsonObject, "modid"));
    }

    private static Boolean forgeTagEmpty(JsonObject object) {
        var id = new ResourceLocation(GsonHelper.getAsString(object, "tag"));
        Map<ResourceKey<?>, Map<ResourceLocation, Collection<Holder<?>>>> allTags = ResourceConditionsImpl.LOADED_TAGS.get();
        if (allTags == null) {
            Moonlight.LOGGER.warn("Can't retrieve deserialized tags. Failing tag resource condition check.");
            return true;
        }
        Map<ResourceLocation, Collection<Holder<?>>> registryTags = allTags.get(Registries.ITEM);
        if (registryTags == null) {
            // No tag for this registry
            return true;
        }
        Collection<Holder<?>> tags = registryTags.get(id);
        if (tags == null) return true;
        if (tags.size() == 1 && tags.stream().findFirst().get().value() == Items.AIR) {
            Moonlight.LOGGER.warn("Found broken tag which just contained the empty item: " + id);
            return true;
        }
        return tags.isEmpty();
    }

    public static void registerSimple(ResourceLocation id, Predicate<String> predicate) {
        ResourceConditions.register(id, j -> predicate.test(j.get(id.getPath()).getAsString()));
    }

    public static boolean matchesForgeCondition(JsonObject obj) {
        try {
            JsonArray conditions = GsonHelper.getAsJsonArray(obj, NON_RECIPE_CONDITIONS_KEY, null);
            if(conditions == null){
                conditions = GsonHelper.getAsJsonArray(obj, CONDITIONS_KEY, null);
            }
            if (conditions != null) {
                return conditionsMatch(conditions, true);
            }
        } catch (RuntimeException exception) {
            //Moonlight.LOGGER.warn("Failed to parse resource conditions for %s".formatted(obj), exception);
        }
        return true;

    }

    /**
     * Check if the passed condition object matches.
     *
     * @throws RuntimeException If some condition failed to parse.
     */
    public static boolean conditionMatches(JsonObject condition) throws RuntimeException {
        if(condition.has(CONDITION_ID)){
            ResourceLocation conditionId = new ResourceLocation(GsonHelper.getAsString(condition, CONDITION_ID));
            Predicate<JsonObject> jrc = ResourceConditions.get(conditionId);

            if (jrc == null) {
                throw new JsonParseException("Unknown recipe condition: " + conditionId);
            } else {
                return jrc.test(condition);
            }
        }else return ResourceConditions.conditionMatches(condition);
    }

    public static boolean conditionsMatch(JsonArray conditions, boolean and) throws RuntimeException {
        for (JsonElement element : conditions) {
            if (element.isJsonObject()) {
                if (conditionMatches(element.getAsJsonObject()) != and) {
                    return !and;
                }
            } else {
                throw new JsonParseException("Invalid condition entry: " + element);
            }
        }
        return and;
    }
}
