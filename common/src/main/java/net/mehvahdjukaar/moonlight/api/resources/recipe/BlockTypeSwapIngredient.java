package net.mehvahdjukaar.moonlight.api.resources.recipe;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.moonlight.api.set.BlockType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;

import java.util.List;

public interface BlockTypeSwapIngredient {

    @Contract
    @ExpectPlatform
    static <T extends BlockType> Ingredient create(Ingredient original, T from, T to) {
        throw new AssertionError();
    }

    @ApiStatus.Internal
    @ExpectPlatform
    static void init() {
        throw new AssertionError();
    }

    Ingredient getInner();

    List<ItemStack> convertItems(List<ItemStack> items);
}
