package net.mehvahdjukaar.moonlight.api.resources.recipe.platform;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.mehvahdjukaar.moonlight.api.resources.recipe.BlockTypeSwapIngredient;
import net.mehvahdjukaar.moonlight.api.set.BlockType;
import net.mehvahdjukaar.moonlight.api.set.BlockTypeRegistry;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.stream.Stream;

public class BlockTypeSwapIngredientImpl<T extends BlockType> extends BlockTypeSwapIngredient<T> implements CustomIngredient {

    protected BlockTypeSwapIngredientImpl(Ingredient inner, T fromType, T toType, BlockTypeRegistry<T> reg) {
        super(inner, fromType, toType, reg);
    }

    @Override
    public Stream<Holder<Item>> items() {
        return getMatchingStacks().stream().map(ItemStack::typeHolder);
    }

    @Override
    public boolean requiresTesting() {
        return inner.requiresTesting();
    }

    @Override
    public CustomIngredientSerializer<?> getSerializer() {
        return SERIALIZER;
    }


    public static final CustomIngredientSerializer<BlockTypeSwapIngredientImpl<?>> SERIALIZER =
            new CustomIngredientSerializer<>() {

                @Override
                public Identifier getIdentifier() {
                    return ID;
                }

                @Override
                public MapCodec<BlockTypeSwapIngredientImpl<?>> getCodec() {
                    return (MapCodec<BlockTypeSwapIngredientImpl<?>>) (Object) CODEC;
                }

                @Override
                public StreamCodec<RegistryFriendlyByteBuf, BlockTypeSwapIngredientImpl<?>> getStreamCodec() {
                    return (StreamCodec<RegistryFriendlyByteBuf, BlockTypeSwapIngredientImpl<?>>) (Object) STREAM_CODEC;
                }

            };

    public static <T extends BlockType> BlockTypeSwapIngredient<T> create(Ingredient original, T from, T to, BlockTypeRegistry<T> reg) {
        return new BlockTypeSwapIngredientImpl<>(original, from, to, reg);
    }

    public static <T extends BlockType> Ingredient create(Ingredient original, T from, T to) {
        return new BlockTypeSwapIngredientImpl<>(original, from, to, from.getRegistry())
                .toVanilla();
    }

    public static void register() {
        CustomIngredientSerializer.register(SERIALIZER);
    }
}
