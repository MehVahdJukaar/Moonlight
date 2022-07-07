package net.mehvahdjukaar.moonlight.api.resources.recipe.forge;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.mehvahdjukaar.moonlight.api.resources.recipe.IRecipeTemplate;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;

import java.util.ArrayList;
import java.util.List;

public class TemplateRecipeManagerImpl {
    public static void addRecipeConditions(JsonObject recipe, IRecipeTemplate<?> template) {
        if (recipe.has("conditions")) {
            JsonArray cond = recipe.get("conditions").getAsJsonArray();
            List<ICondition> c = deserializeConditions(cond);
            c.forEach(template::addCondition);
        }
    }


    private static List<ICondition> deserializeConditions(JsonArray conditions) {
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
