package net.mehvahdjukaar.moonlight.fabric;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.fabricmc.fabric.impl.resource.conditions.ResourceConditionsImpl;
import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Items;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public class ModResourceConditionsBridge {

    public static void init() {
        try {
            ResourceConditions.register(new ResourceLocation("forge:not"), ModResourceConditionsBridge::forgeNot);
        } catch (Exception ignored) {

        }
        try {
            ResourceConditions.register(new ResourceLocation("forge:and"), ModResourceConditionsBridge::forgeAnd);
        } catch (Exception ignored) {
        }

        try {
            ResourceConditions.register(new ResourceLocation("forge:mod_loaded"), ModResourceConditionsBridge::forgeModLoaded);
        } catch (Exception ignored) {
        }

        try {
            ResourceConditions.register(new ResourceLocation("forge:tag_empty"), ModResourceConditionsBridge::forgeTagEmpty);
        } catch (Exception ignored) {
        }
    }

    private static boolean forgeNot(JsonObject jsonObject) {
        JsonObject jo = GsonHelper.getAsJsonObject(jsonObject, "value");
        return !ResourceConditions.conditionMatches(jo);
    }

    private static boolean forgeAnd(JsonObject jsonObject) {
        JsonArray jo = GsonHelper.getAsJsonArray(jsonObject, "values");
        return ResourceConditions.conditionsMatch(jo, true);
    }

    private static boolean forgeModLoaded(JsonObject jsonObject) {
        return PlatformHelper.isModLoaded(GsonHelper.getAsJsonObject(jsonObject, "modid").getAsString());
    }

    private static Boolean forgeTagEmpty(JsonObject object) {
        var id = new ResourceLocation(GsonHelper.getAsString(object, "tag"));
        Map<ResourceKey<?>, Map<ResourceLocation, Collection<Holder<?>>>> allTags = ResourceConditionsImpl.LOADED_TAGS.get();
        if (allTags == null) {
            Moonlight.LOGGER.warn("Can't retrieve deserialized tags. Failing tag resource condition check.");
            return true;
        }
        Map<ResourceLocation, Collection<Holder<?>>> registryTags = allTags.get(Registry.ITEM_REGISTRY);
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
}
