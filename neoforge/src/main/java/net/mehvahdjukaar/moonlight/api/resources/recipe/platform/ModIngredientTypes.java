package net.mehvahdjukaar.moonlight.api.resources.recipe.platform;

import com.mojang.serialization.MapCodec;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.api.resources.recipe.BlockTypeSwapIngredient;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;


public class ModIngredientTypes {

    public static final Supplier<IngredientType<?>> BLOCK_TYPE_SWAP = RegHelper.register(BlockTypeSwapIngredient.ID,
            () -> new IngredientType<>(
                    (MapCodec<ICustomIngredient>) (Object) BlockTypeSwapIngredient.CODEC,
                    (StreamCodec<? super RegistryFriendlyByteBuf, ICustomIngredient>) (Object) BlockTypeSwapIngredient.STREAM_CODEC
            ), NeoForgeRegistries.INGREDIENT_TYPES.key());

    public static void register() {
    }

}
