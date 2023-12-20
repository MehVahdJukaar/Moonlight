package net.mehvahdjukaar.moonlight.api.resources.recipe.forge;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.neoforged.neoforge.common.conditions.ICondition;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

/**
 * Simple recipe condition implementation for conditional recipes
 */
public record OptionalRecipeCondition(AtomicReference<Codec<OptionalRecipeCondition>> codecRef, String name, Predicate<String> predicate,
                                      String conditionValue) implements ICondition {

    public static Codec<OptionalRecipeCondition> createCodec(String name, Predicate<String> predicate) {
        AtomicReference<Codec<OptionalRecipeCondition>> ref= new AtomicReference<>();
        Codec<OptionalRecipeCondition> codec = RecordCodecBuilder.create(builder -> builder.group(
                Codec.STRING.fieldOf(name).forGetter(OptionalRecipeCondition::name)
        ).apply(builder, s -> new OptionalRecipeCondition(ref, name, predicate, s)));
        ref.set(codec);
        return codec;
    }

    @Override
    public boolean test(IContext context) {
        return OptionalRecipeCondition.this.predicate.test(conditionValue);
    }

    @Override
    public Codec<? extends ICondition> codec() {
        return codecRef.get();
    }

}
