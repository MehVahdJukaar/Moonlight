package net.mehvahdjukaar.moonlight.api.resources.recipe.neoforge;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.conditions.AndCondition;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.List;
import java.util.function.Function;

public class ResourceConditionsBridge {

    private static final Codec<ICondition> REMAPPING_CODEC =
            NeoForgeRegistries.CONDITION_SERIALIZERS.byNameCodec()
                    .dispatch("type", ICondition::codec, Function.identity());
    //we must use "type" key instead of "condition" that fabric uses as compound conditions do expect that

    private static final Codec<List<ICondition>> LIST_CODEC = Utils.lenientListCodec(REMAPPING_CODEC);
    private static final Codec<ICondition> SINGLE_OR_LIST = Codec.withAlternative(REMAPPING_CODEC, LIST_CODEC,
            AndCondition::new);


    public static boolean matchesForgeConditions(JsonObject obj, ICondition.IContext context, String conditionKey) {
        JsonElement conditionElement = obj.get(conditionKey);
        if (conditionElement != null) {
            if (obj.has("neoforge:conditions")) {
                return true;
            }
            var newObj = replaceKeyInJsonRecursive(conditionElement,
                    "condition", "type",
                    "fabric", "neoforge");
            //very dumb incoming
            var c = SINGLE_OR_LIST.parse(JsonOps.INSTANCE, newObj);
            if (c.result().isPresent()) {
                return c.getOrThrow().test(context);
            }
        }
        return true;
    }

    //registers equivalent of fabric conditions
    public static void init() {
        try {
            RegHelper.register(ResourceLocation.parse("neoforge:all_mods_loaded"),
                    () -> AllModsLoadedResourceCondition.CODEC, NeoForgeRegistries.Keys.CONDITION_CODECS);
        } catch (Exception e) {
            Moonlight.LOGGER.error("Failed to register neoforge conditions", e);
        }
    }

    public record AllModsLoadedResourceCondition(List<String> modIds) implements ICondition {
        public static final MapCodec<AllModsLoadedResourceCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.listOf().fieldOf("values").forGetter(AllModsLoadedResourceCondition::modIds)
        ).apply(instance, AllModsLoadedResourceCondition::new));


        @Override
        public boolean test(IContext iContext) {
            for (String modId : modIds) {
                if (!PlatHelper.isModLoaded(modId)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public MapCodec<? extends ICondition> codec() {
            return CODEC;
        }
    }

    private static JsonElement replaceKeyInJsonRecursive(JsonElement json, String from, String to,
                                                         String valueFrom, String valueTo) {
        if (json.isJsonObject()) {
            JsonObject newObj = new JsonObject();

            for (var entry : json.getAsJsonObject().entrySet()) {
                String key = entry.getKey().equals(from) ? to : entry.getKey();
                newObj.add(key, replaceKeyInJsonRecursive(entry.getValue(), from, to, valueFrom, valueTo));
            }

            return newObj;
        } else if (json.isJsonArray()) {
            JsonArray newArray = new JsonArray();

            for (var element : json.getAsJsonArray()) {
                newArray.add(replaceKeyInJsonRecursive(element, from, to, valueFrom, valueTo));
            }

            return newArray;
        } else if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isString()) {
            String value = json.getAsString();
            if (value.contains(valueFrom)) {
                return new JsonPrimitive(value.replace(valueFrom, valueTo));
            }
        }

        return json; // Return the element itself if it's neither an object nor an array
    }

}
