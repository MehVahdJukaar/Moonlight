package net.mehvahdjukaar.moonlight.api.resources.recipe.fabric;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

/**
 * Simple recipe condition implementation for conditional recipes
 */
public record OptionalRecipeCondition(ResourceLocation id,
                                      Predicate<String> predicate,
                                      String conditionValue) implements ResourceCondition {

    public static MapCodec<OptionalRecipeCondition> createCodec(ResourceLocation id, Predicate<String> predicate) {
        String name = id.getPath();
        return RecordCodecBuilder.mapCodec(builder -> builder.group(
                Codec.STRING.fieldOf(name).forGetter(o -> o.id().getPath())
        ).apply(builder, s -> new OptionalRecipeCondition(id, predicate, s)));
    }

    @Override
    public ResourceConditionType<?> getType() {
        return ResourceConditions.getConditionType(id);
    }

    @Override
    public boolean test(@Nullable HolderLookup.Provider registryLookup) {
        return this.predicate.test(conditionValue);
    }
}
