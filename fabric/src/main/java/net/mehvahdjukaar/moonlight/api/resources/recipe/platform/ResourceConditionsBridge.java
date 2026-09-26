package net.mehvahdjukaar.moonlight.api.resources.recipe.platform;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

public class ResourceConditionsBridge {

    //registers equivalent of fabric conditions
    public static void init() {
        try {
            ResourceConditions.register(ModLoadedCondition.TYPE);
            ResourceConditions.register(TagEmptyCondition.TYPE);
        } catch (Exception e) {
            Moonlight.LOGGER.error("Failed to register fabric conditions", e);
        }
    }

    public record ModLoadedCondition(String modIds) implements ResourceCondition {
        public static final MapCodec<ModLoadedCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.fieldOf("modid").forGetter(ModLoadedCondition::modIds)
        ).apply(instance, ModLoadedCondition::new));


        public static final ResourceConditionType<ModLoadedCondition> TYPE = ResourceConditionType.create(
                Identifier.parse("fabric:mod_loaded"), ModLoadedCondition.CODEC);

        @Override
        public ResourceConditionType<?> getType() {
            return TYPE;
        }

        @Override
        public boolean test(RegistryOps.@Nullable RegistryInfoLookup registryLookup) {
            return PlatHelper.isModLoaded(modIds);
        }
    }

    public record TagEmptyCondition(TagKey<Item> tag) implements ResourceCondition {
        public static final MapCodec<TagEmptyCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                TagKey.codec(Registries.ITEM).fieldOf("tag").forGetter(TagEmptyCondition::tag)
        ).apply(instance, TagEmptyCondition::new));


        public static final ResourceConditionType<TagEmptyCondition> TYPE = ResourceConditionType.create(
                Identifier.parse("fabric:tag_empty"), TagEmptyCondition.CODEC);

        @Override
        public ResourceConditionType<?> getType() {
            return TYPE;
        }

        @Override
        public boolean test(RegistryOps.@Nullable RegistryInfoLookup registryLookup) {
            //tags arent bound yet during reload. this just checks that no pack defines it
            return !ResourceConditions.tagsPopulated(tag).test(registryLookup);
        }
    }

}
