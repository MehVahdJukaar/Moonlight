package net.mehvahdjukaar.moonlight.api.resources;

import com.google.common.base.Preconditions;
import net.mehvahdjukaar.moonlight.api.misc.TriFunction;
import net.mehvahdjukaar.moonlight.api.resources.recipe.BlockTypeSwapIngredient;
import net.mehvahdjukaar.moonlight.api.set.BlockType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;

//TODO: change name
public class RecipeTemplate {

    private static final Map<Class<? extends Recipe<?>>, TriFunction<Recipe<?>, BlockType, BlockType, Recipe<?>>> REMAPPERS = new HashMap<>();

    /** Registers a recipe type remapped through its ingredients and result. */
    public static <R extends Recipe<?>> void registerSimple(Class<R> type,
                                                            Function<R, List<Ingredient>> ingredientGetter,
                                                            Function<R, ItemStackTemplate> resultGetter,
                                                            RecipeFactory<R> factory) {
        register(type, (r, f, t) -> createSimple(r, ingredientGetter, resultGetter, factory, f, t));
    }

    public static <R extends Recipe<?>> void register(Class<R> type, TriFunction<R, BlockType, BlockType, R> factory) {
        REMAPPERS.put(type, (r, f, t) -> factory.apply((R) r, f, t));
    }

    public interface RecipeFactory<R extends Recipe<?>> {
        R create(R original, ItemStackTemplate result, List<Ingredient> ingredients);
    }

    public static <T extends BlockType, R extends Recipe<?>> RecipeHolder<?> makeSimilarRecipe(
            R original, @NotNull T originalMat, @NotNull T destinationMat,
            Identifier newId) {
        var clazz = original.getClass();
        var remapper = REMAPPERS.get(clazz);
        if (remapper == null) {
            throw new UnsupportedOperationException("Recipe class " + clazz + " not supported. You must register it using RecipeTemplate.register()");
        }

        //backward compat hack
        if (newId.getPath().endsWith("_oak")) {
            newId = newId.withPath(p -> p + "/" + destinationMat.getAppendableId());
        }

        Preconditions.checkNotNull(original, "Found null from block type for remapping for recipe " + originalMat + " with id " + newId);
        Preconditions.checkNotNull(originalMat, "Found null from block type for remapping for recipe " + originalMat + " with id " + newId);

        var remapped = remapper.apply(original, originalMat, destinationMat);

        return new RecipeHolder<>(ResourceKey.create(Registries.RECIPE, newId), remapped);
    }

    static {
        register(ShapedRecipe.class, RecipeTemplate::createShaped);
        registerSimple(ShapelessRecipe.class,
                r -> r.ingredients,
                r -> r.result,
                (or, result, ingredients) -> new ShapelessRecipe(commonInfoOf(or),
                        new CraftingRecipe.CraftingBookInfo(or.category(), or.group()), result, ingredients));
        registerSimple(StonecutterRecipe.class,
                r -> List.of(r.input()),
                SingleItemRecipe::result,
                (or, result, ingredients) -> new StonecutterRecipe(commonInfoOf(or), ingredients.getFirst(), result));
        registerSimple(SmeltingRecipe.class,
                r -> List.of(r.input()),
                SingleItemRecipe::result,
                (or, result, ingredients) -> new SmeltingRecipe(commonInfoOf(or),
                        new AbstractCookingRecipe.CookingBookInfo(or.category(), or.group()),
                        ingredients.getFirst(), result, or.experience(), or.cookingTime()));
    }

    private static Recipe.CommonInfo commonInfoOf(Recipe<?> recipe) {
        return new Recipe.CommonInfo(recipe.showNotification());
    }

    private static <R extends Recipe<?>> R createSimple(R or,
                                                        Function<R, List<Ingredient>> ingredientGetter,
                                                        Function<R, ItemStackTemplate> resultGetter,
                                                        RecipeFactory<R> factory,
                                                        @NotNull BlockType from, @NotNull BlockType to) {
        Preconditions.checkNotNull(from, "Found null from block type for recipe remapping on recipe " + or);
        Preconditions.checkNotNull(to, "Found null to block type for recipe remapping on recipe " + or);
        List<Ingredient> newIngredients = convertIngredients(ingredientGetter.apply(or), from, to);
        ItemStackTemplate newResult = convertResult(resultGetter.apply(or), from, to);
        return factory.create(or, newResult, newIngredients);
    }

    private static ShapedRecipe createShaped(ShapedRecipe or, @NotNull BlockType from, @NotNull BlockType to) {
        Preconditions.checkNotNull(from, "Found null from block type for recipe remapping on recipe " + or);
        Preconditions.checkNotNull(to, "Found null to block type for recipe remapping on recipe " + or);
        List<Optional<Ingredient>> newList = convertOptionalIngredients(or.getIngredients(), from, to);
        ItemStackTemplate newResult = convertResult(or.result, from, to);

        int width = or.getWidth();
        int height = or.getHeight();
        ShapedRecipePattern pattern = new ShapedRecipePattern(width, height, newList,
                Optional.of(packRecipePattern(width, height, newList)));

        return new ShapedRecipe(commonInfoOf(or),
                new CraftingRecipe.CraftingBookInfo(or.category(), or.group()), pattern, newResult);
    }

    private static ShapedRecipePattern.Data packRecipePattern(int width, int height, List<Optional<Ingredient>> ingredients) {
        // Create a new map to hold the unique character keys and corresponding ingredients.
        Map<Character, Ingredient> key = new HashMap<>();
        List<String> pattern = new ArrayList<>();

        char nextSymbol = 'A';  // Start with 'A' as the symbol for mapping ingredients.

        // Iterate over each row in the grid based on the width and height of the pattern.
        for (int row = 0; row < height; row++) {
            StringBuilder rowPattern = new StringBuilder();
            for (int col = 0; col < width; col++) {
                Optional<Ingredient> slot = ingredients.get(row * width + col);

                // Check if ingredient is empty, then use space.
                if (slot.isEmpty() || slot.get().isEmpty()) {
                    rowPattern.append(' ');
                } else {
                    Ingredient ingredient = slot.get();
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
            pattern.add(rowPattern.toString());
        }
        return new ShapedRecipePattern.Data(key, pattern);
    }

    public static <T extends BlockType> ItemStack convertItemStack(ItemStack original, T from, T to) {
        return original.transmuteCopy(changeItem(original.getItem(), from, to));
    }

    public static <T extends BlockType> ItemStackTemplate convertResult(ItemStackTemplate original, T from, T to) {
        Item changed = changeItem(original.item().value(), from, to);
        return new ItemStackTemplate(changed.builtInRegistryHolder(), original.count(), original.components());
    }

    private static <T extends BlockType> Item changeItem(Item original, T from, T to) {
        Item changed = BlockType.changeItemType(original, from, to);
        if (changed == null) {
            throw new UnsupportedOperationException("Failed to convert item stack: could not change " +
                    original + " from " + from.getId() + " to " + to.getId());
        }
        return changed;
    }

    public static @NotNull List<Ingredient> convertIngredients(List<Ingredient> or,
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

    public static @NotNull List<Optional<Ingredient>> convertOptionalIngredients(List<Optional<Ingredient>> or,
                                                                                 @NotNull BlockType from, @NotNull BlockType to) {
        Map<Ingredient, Ingredient> convertedMap = new HashMap<>();
        List<Optional<Ingredient>> newList = new ArrayList<>();
        for (Optional<Ingredient> slot : or) {
            if (slot.isEmpty() || slot.get().isEmpty()) {
                newList.add(slot);
            } else {
                newList.add(Optional.of(convertedMap.computeIfAbsent(slot.get(),
                        i -> BlockTypeSwapIngredient.create(i, from, to))));
            }
        }
        return newList;
    }

}
