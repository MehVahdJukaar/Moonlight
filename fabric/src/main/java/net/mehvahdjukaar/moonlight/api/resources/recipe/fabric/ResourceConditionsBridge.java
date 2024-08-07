package net.mehvahdjukaar.moonlight.api.resources.recipe.fabric;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.api.platform.fabric.RegHelperImpl;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ResourceConditionsBridge {

    //registers equivalent of fabric conditions
    public static void init() {
        try {
            ResourceConditions.register(ModLoadedCondition.TYPE);
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

}
