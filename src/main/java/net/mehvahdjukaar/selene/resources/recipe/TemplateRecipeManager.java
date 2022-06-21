package net.mehvahdjukaar.selene.resources.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class TemplateRecipeManager {

    private static final Map<ResourceLocation, Function<JsonObject, ? extends IRecipeTemplate<?>>> DESERIALIZERS = new HashMap<>();

    /**
     * Registers a recipe template deserializer. Will be used to parse existing recipes and be able to create new ones
     *
     * @param deserializer usually IRecipeTemplate::new
     * @param serializer   recipe serializer type
     */
    public static <T extends IRecipeTemplate<?>> void registerTemplate(
            RecipeSerializer<?> serializer, Function<JsonObject, T> deserializer) {
        registerTemplate(serializer.getRegistryName(), deserializer);
    }

    public static <T extends IRecipeTemplate<?>> void registerTemplate(
            ResourceLocation serializerId, Function<JsonObject, T> deserializer) {
        DESERIALIZERS.put(serializerId, deserializer);
    }

    public static IRecipeTemplate<?> read(JsonObject recipe) throws UnsupportedOperationException {
        String type = GsonHelper.getAsString(recipe, "type");
        //RecipeSerializer<?> s = ForgeRegistries.RECIPE_SERIALIZERS.getValue(new ResourceLocation(type));

        var templateFactory = DESERIALIZERS.get(new ResourceLocation(type));

        if (templateFactory != null) {
            var template = templateFactory.apply(recipe);
            //special case for shaped with a single item...
            if(template instanceof ShapedRecipeTemplate st && st.shouldBeShapeless()){
                template = st.toShapeless();
            }
            if (recipe.has("conditions")) {
                JsonArray cond = recipe.get("conditions").getAsJsonArray();
                List<ICondition> c = deserializeConditions(cond);
                c.forEach(template::addCondition);
            }
            return template;
        } else {
            throw new UnsupportedOperationException(String.format("Invalid recipe serializer: %s. Must be either shaped, shapeless or stonecutting", type));
        }
    }

    static {
        registerTemplate(RecipeSerializer.SHAPED_RECIPE, ShapedRecipeTemplate::new);
        registerTemplate(RecipeSerializer.SHAPELESS_RECIPE, ShapelessRecipeTemplate::new);
        registerTemplate(RecipeSerializer.STONECUTTER, StoneCutterRecipeTemplate::new);
    }

    public static List<ICondition> deserializeConditions(JsonArray conditions) {
        List<ICondition> list = new ArrayList<>();
        for (int x = 0; x < conditions.size(); x++) {
            if (!conditions.get(x).isJsonObject())
                throw new JsonSyntaxException("Conditions must be an array of JsonObjects");

            JsonObject json = conditions.get(x).getAsJsonObject();
            list.add(CraftingHelper.getCondition(json));
        }
        return list;
    }
}
