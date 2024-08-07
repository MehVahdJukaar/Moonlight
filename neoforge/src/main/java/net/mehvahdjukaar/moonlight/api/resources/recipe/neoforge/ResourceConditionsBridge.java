package net.mehvahdjukaar.moonlight.api.resources.recipe.neoforge;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.api.resources.RPUtils;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.conditions.AndCondition;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Function;

public class ResourceConditionsBridge {

    private static final Codec<ICondition> CODEC = NeoForgeRegistries.CONDITION_SERIALIZERS.byNameCodec()
            .dispatch("condition", ICondition::codec, Function.identity());
    private static final Codec<List<ICondition>> LIST_CODEC = Utils.lenientListCodec(CODEC);
    private static final Codec<ICondition> SINGLE_OR_LIST = Codec.withAlternative(CODEC, LIST_CODEC,
            AndCondition::new);


    public static boolean matchesForgeConditions(JsonObject obj, ICondition.IContext context) {
        JsonElement je = obj.get("fabric:load_conditions");
        if (je != null) {
            je = RPUtils.deserializeJson(
                    new ByteArrayInputStream(
                            je.toString().replace("fabric:", "neoforge:")
                                    .getBytes(StandardCharsets.UTF_8)));

            var c = SINGLE_OR_LIST.parse(JsonOps.INSTANCE, je);
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
}
