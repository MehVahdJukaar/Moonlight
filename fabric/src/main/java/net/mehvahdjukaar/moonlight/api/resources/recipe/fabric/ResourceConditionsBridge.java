package net.mehvahdjukaar.moonlight.api.resources.recipe.fabric;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.fabricmc.fabric.impl.resource.conditions.ResourceConditionsImpl;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ResourceConditionsBridge {

    //registers equivalent of fabric conditions
    public static void init() {
        try {
            ResourceConditions.register(ModLoadedCondition.TYPE);
            ResourceConditions.register(TagEmptyCondition.TYPE);
            ResourceConditions.register(FALSE_TYPE);
        } catch (Exception e) {
            Moonlight.LOGGER.error("Failed to register fabric conditions", e);
        }
    }

    public record ModLoadedCondition(String modIds) implements ResourceCondition {
        public static final MapCodec<ModLoadedCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.fieldOf("modid").forGetter(ModLoadedCondition::modIds)
        ).apply(instance, ModLoadedCondition::new));


        public static final ResourceConditionType<ModLoadedCondition> TYPE = ResourceConditionType.create(
                ResourceLocation.parse("fabric:mod_loaded"), ModLoadedCondition.CODEC);

        @Override
        public ResourceConditionType<?> getType() {
            return TYPE;
        }

        @Override
        public boolean test(@Nullable HolderLookup.Provider registryLookup) {
            return PlatHelper.isModLoaded(modIds);
        }
    }

    public record TagEmptyCondition(TagKey<Item> tag) implements ResourceCondition {
        public static final MapCodec<TagEmptyCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                TagKey.codec(Registries.ITEM).fieldOf("tag").forGetter(TagEmptyCondition::tag)
        ).apply(instance, TagEmptyCondition::new));


        public static final ResourceConditionType<TagEmptyCondition> TYPE = ResourceConditionType.create(
                ResourceLocation.parse("fabric:tag_empty"), TagEmptyCondition.CODEC);

        @Override
        public ResourceConditionType<?> getType() {
            return TYPE;
        }

        @Override
        public boolean test(@Nullable HolderLookup.Provider registryLookup) {
            if (registryLookup == null) {
                //not ideal... idk why registryLookup would be null... dub fabric as usual
                Moonlight.LOGGER.error("Registry Lookup was null, failing tag_empty resource condition check");
                return !ResourceConditionsImpl.tagsPopulated(tag.registry().location(), List.of(tag.location()));
            }
            var opt = registryLookup.lookupOrThrow(Registries.ITEM).get(tag);
            return opt.isEmpty() || opt.get().stream().findAny().isEmpty();
        }
    }


    public static final ResourceCondition FALSE = new ResourceCondition() {

        @Override
        public ResourceConditionType<?> getType() {
            return FALSE_TYPE;
        }

        @Override
        public boolean test(HolderLookup.@Nullable Provider registryLookup) {
            return false;
        }
    };

    public static final ResourceConditionType<ResourceCondition> FALSE_TYPE = ResourceConditionType.create(
            ResourceLocation.parse("fabric:false"), MapCodec.unit(FALSE));

}
