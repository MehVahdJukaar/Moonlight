package net.mehvahdjukaar.moonlight.api.resources;

import com.google.common.base.Preconditions;
import net.mehvahdjukaar.moonlight.api.misc.TriFunction;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.resources.recipe.BlockTypeSwapIngredient;
import net.mehvahdjukaar.moonlight.api.set.BlockType;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;

//TODO: change name
public class RecipeTemplate {

    private static final Map<Class<? extends Recipe<?>>, TriFunction<Recipe<?>, BlockType, BlockType, Recipe<?>>> REMAPPERS = new HashMap<>();

    public static <R extends Recipe<?>> void registerSimple(Class<R> type, RecipeFactory<R> factory) {
        register(type, (r, f, t) -> createSimple(r, factory, f, t));
    }

    /**
     * Register a recipe template for your recipe. Allows creating a copy of it with different ingredients
     */
    @Deprecated(forRemoval = true)
    public static <R extends Recipe<?>> void register(Class<R> type, BiFunction<R, UnaryOperator<ItemStack>, R> factory) {
        //  REMAPPERS.put(type, (r, t) -> factory.apply((R) r, t));
        if (PlatHelper.isDev()) {
            throw new UnsupportedOperationException("You must register this using RecipeTemplate.register()");
        }
    }

    public static <R extends Recipe<?>> void register(Class<R> type, TriFunction<R, BlockType, BlockType, R> factory) {
        REMAPPERS.put(type, (r, f, t) -> factory.apply((R) r, f, t));
    }

    public interface RecipeFactory<R extends Recipe<?>> {
        R create(String group, CraftingBookCategory category, ItemStack result, NonNullList<Ingredient> ingredients);
    }

    @Deprecated(forRemoval = true)
    public static <T extends BlockType, R extends Recipe<?>> RecipeHolder<?> makeSimilarRecipe(R original, T originalMat,
                                                                                               T destinationMat,
                                                                                               String baseID) {
        return makeSimilarRecipe(original, originalMat, destinationMat, ResourceLocation.parse(baseID));
    }

    public static <T extends BlockType, R extends Recipe<?>> RecipeHolder<?> makeSimilarRecipe(
            R original, @NotNull T originalMat, @NotNull T destinationMat,
            ResourceLocation baseID) {
        var clazz = original.getClass();
        var remapper = REMAPPERS.get(clazz);
        if (remapper == null) {
            throw new UnsupportedOperationException("Recipe class " + clazz + " not supported. You must register it using RecipeTemplate.register()");
        }
        ResourceLocation newId = baseID.withPath(p -> p + "/" + destinationMat.getAppendableId());

        Preconditions.checkNotNull(original, "Found null from block type for remapping for recipe " + originalMat + " with id " + newId);
        Preconditions.checkNotNull(originalMat, "Found null from block type for remapping for recipe " + originalMat + " with id " + newId);

        var remapped = remapper.apply(original, originalMat, destinationMat);

        return new RecipeHolder<>(newId, remapped);
    }

    static {
        register(ShapedRecipe.class, RecipeTemplate::createShaped);
        registerSimple(ShapelessRecipe.class, ShapelessRecipe::new);
        registerSimple(StonecutterRecipe.class, (group, category, result, ingredients) ->
                new StonecutterRecipe(group, ingredients.getFirst(), result));
        register(SmeltingRecipe.class, (recipe, oldType, newType) ->
                createSimple(recipe,
                        (group, category, result, ingredients) ->
                        new SmeltingRecipe(group, recipe.category(), ingredients.getFirst(), result, recipe.getExperience(), recipe.getCookingTime()),
                        oldType, newType
                )
        );
    }


    private static <R extends Recipe<?>> R createSimple(R or, RecipeFactory<R> factory,
                                                        @NotNull BlockType from, @NotNull BlockType to) {
        Preconditions.checkNotNull(from, "Found null from block type for recipe remapping on recipe " + or);
        Preconditions.checkNotNull(to, "Found null to block type for recipe remapping on recipe " + or);
        List<Ingredient> newList = convertIngredients(or.getIngredients(), from, to);
        ItemStack originalResult = or.getResultItem(RegistryAccess.EMPTY);
        ItemStack newResult = convertItemStack(originalResult, from, to);
        NonNullList<Ingredient> ingredients = NonNullList.of(Ingredient.EMPTY, newList.toArray(Ingredient[]::new));

        CraftingBookCategory cat = CraftingBookCategory.MISC;
        if (or instanceof CraftingRecipe cr) {
            cat = cr.category();
        }
        return factory.create(or.getGroup(), cat, newResult, ingredients);
    }

    private static ShapedRecipe createShaped(ShapedRecipe or, @NotNull BlockType from, @NotNull BlockType to) {
        Preconditions.checkNotNull(from, "Found null from block type for recipe remapping on recipe " + or);
        Preconditions.checkNotNull(to, "Found null to block type for recipe remapping on recipe " + or);
        List<Ingredient> newList = convertIngredients(or.getIngredients(), from, to);
        ItemStack originalResult = or.getResultItem(RegistryAccess.EMPTY);
        ItemStack newResult = convertItemStack(originalResult, from, to);
        NonNullList<Ingredient> ingredients = NonNullList.of(Ingredient.EMPTY, newList.toArray(Ingredient[]::new));

        ShapedRecipePattern pattern = new ShapedRecipePattern(or.getWidth(), or.getHeight(), ingredients,
                Optional.of(packRecipePattern(or.getWidth(), or.getHeight(), ingredients)));

        return new ShapedRecipe(or.getGroup(), or.category(), pattern, newResult);
    }

    private static ShapedRecipePattern.Data packRecipePattern(int width, int height, NonNullList<Ingredient> ingredients) {
        // Create a new map to hold the unique character keys and corresponding ingredients.
        Map<Character, Ingredient> key = new HashMap<>();
        List<String> pattern = new ArrayList<>();

        char nextSymbol = 'A';  // Start with 'A' as the symbol for mapping ingredients.

        // Iterate over each row in the grid based on the width and height of the pattern.
        for (int row = 0; row < height; row++) {
            StringBuilder rowPattern = new StringBuilder();
            for (int col = 0; col < width; col++) {
                Ingredient ingredient = ingredients.get(row * width + col);

                // Check if ingredient is empty, then use space.
                if (ingredient.isEmpty()) {
                    rowPattern.append(' ');
                } else {
                    // Check if ingredient already has an assigned symbol in the map.
                    Character symbol = null;
                    for (Map.Entry<Character, Ingredient> entry : key.entrySet()) {
                        if (entry.getValue() == ingredient) {
                            symbol = entry.getKey();
                            break;
                        }
                    }

                    // If no symbol is found, assign a new one.
                    if (symbol == null) {
                        symbol = nextSymbol++;
                        key.put(symbol, ingredient);
                    }

                    rowPattern.append(symbol);
                }
            }
            // Add the constructed row to the pattern.
            pattern.add(rowPattern.toString());
        }
        return new ShapedRecipePattern.Data(key, pattern);
    }

    public static <T extends BlockType> ItemStack convertItemStack(ItemStack original, T from, T to) {
        Item changed = BlockType.changeItemType(original.getItem(), from, to);
        if (changed == null) {
            throw new UnsupportedOperationException("Failed to convert item stack: could not change " +
                    original.getItem() + " from " + from.getId() + " to " + to.getId());
        }
        return original.transmuteCopy(changed);
    }

    @Deprecated(forRemoval = true)
    public static <R extends Recipe<?>> @NotNull List<Ingredient> convertIngredients(NonNullList<Ingredient> or,
                                                                                     UnaryOperator<ItemStack> typeChanger) {
        List<Ingredient> newList = new ArrayList<>(or);
        for (int i = 0; i < newList.size(); i++) {
            Ingredient ingredient = or.get(i);
            if (ingredient.isEmpty()) continue;
            ItemStack intItem = typeChanger.apply(ingredient.getItems()[0]);
            if (intItem != null) newList.set(i, Ingredient.of(intItem));
        }
        return newList;
    }

    public static <R extends Recipe<?>> @NotNull List<Ingredient> convertIngredients(NonNullList<Ingredient> or,
                                                                                     @NotNull BlockType from, @NotNull BlockType to) {
        List<Ingredient> newList = new ArrayList<>();
        Map<Ingredient, Ingredient> convertedMap = new HashMap<>();
        for (Ingredient ingredient : or) {
            if (ingredient.isEmpty()) {
                newList.add(ingredient);
            } else {
                newList.add(convertedMap.computeIfAbsent(ingredient, i -> BlockTypeSwapIngredient.create(i, from, to)));
            }
        }
        return newList;
    }

}
