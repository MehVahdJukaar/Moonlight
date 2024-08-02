package net.mehvahdjukaar.moonlight.api.resources.recipe.neoforge;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Predicate;

/**
 * Simple recipe condition implementation for conditional recipes
 */
public record OptionalRecipeCondition(ResourceLocation id,
                                      Predicate<String> predicate,
                                      String conditionValue) implements ICondition {

    public static MapCodec<OptionalRecipeCondition> createCodec(ResourceLocation id, Predicate<String> predicate) {
        String name = id.getPath();
        return RecordCodecBuilder.mapCodec(builder -> builder.group(
                Codec.STRING.fieldOf(name).forGetter(o -> o.id().getPath())
        ).apply(builder, s -> new OptionalRecipeCondition(id, predicate, s)));
    }

    @Override
    public boolean test(IContext context) {
        return OptionalRecipeCondition.this.predicate.test(conditionValue);
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return NeoForgeRegistries.CONDITION_SERIALIZERS.get(id);
    }

}
