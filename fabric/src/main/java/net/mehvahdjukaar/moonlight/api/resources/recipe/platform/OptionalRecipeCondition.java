package net.mehvahdjukaar.moonlight.api.resources.recipe.platform;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

/**
 * Simple recipe condition implementation for conditional recipes
 */
public record OptionalRecipeCondition(Identifier id,
                                      Predicate<String> predicate,
                                      String conditionValue) implements ResourceCondition {

    public static MapCodec<OptionalRecipeCondition> createCodec(Identifier id, Predicate<String> predicate) {
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
    public boolean test(RegistryOps.@Nullable RegistryInfoLookup registryLookup) {
        return this.predicate.test(conditionValue);
    }
}
