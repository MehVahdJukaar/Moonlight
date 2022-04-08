package net.mehvahdjukaar.selene.fluids.serialization;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.PrimitiveCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.selene.fluids.FluidContainerList;
import net.mehvahdjukaar.selene.fluids.FoodProvider;
import net.mehvahdjukaar.selene.fluids.SoftFluid;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class SoftFluidCodec {

    private static final PrimitiveCodec<Integer> HEX = new PrimitiveCodec<>() {
        @Override
        public <T> DataResult<Integer> read(final DynamicOps<T> ops, final T input) {
            return ops.getStringValue(input)
                    .map(s -> {
                                if (s.contains("0x") || s.contains("#")) {
                                    return Integer.parseUnsignedInt(
                                            s.replace("0x", "").replace("#", ""), 16);
                                }
                                return Integer.parseUnsignedInt(s,10);
                            }
                    ).map(Number::intValue);
        }

        @Override
        public <T> T write(final DynamicOps<T> ops, final Integer value) {
            String hex = Integer.toHexString(value);
            return ops.createString("#" + hex);
        }

        @Override
        public String toString() {
            return "Int";
        }
    };

    public static final Codec<SoftFluid> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
            ResourceLocation.CODEC.fieldOf("id").forGetter(SoftFluid::getRegistryName),
            ResourceLocation.CODEC.fieldOf("still_texture").forGetter(SoftFluid::getStillTexture),
            ResourceLocation.CODEC.fieldOf("flowing_texture").forGetter(SoftFluid::getFlowingTexture),
            Codec.STRING.optionalFieldOf("translation_key").forGetter(getHackyOptional(SoftFluid::getTranslationKey)),
            Codec.INT.optionalFieldOf("luminosity").forGetter(getHackyOptional(SoftFluid::getLuminosity)),
            HEX.optionalFieldOf("color").forGetter(getHackyOptional(SoftFluid::getTintColor)),
            SoftFluid.TintMethod.CODEC.optionalFieldOf("tint_method").forGetter(getHackyOptional(SoftFluid::getTintMethod)),
            FoodProvider.CODEC.optionalFieldOf("food").forGetter(getHackyOptional(SoftFluid::getFoodProvider)),
            Codec.STRING.listOf().optionalFieldOf("preserved_tags_from_item").forGetter(getHackyOptional(SoftFluid::getNbtKeyFromItem)),
            FluidContainerList.Category.CODEC.listOf().optionalFieldOf("containers").forGetter(f -> f.getContainerList().encodeList()),
            Registry.FLUID.byNameCodec().listOf().optionalFieldOf("equivalent_fluids")
                    .forGetter(getHackyOptional(s -> s.getEquivalentFluids().stream().toList())),
            ResourceLocation.CODEC.optionalFieldOf("use_texture_from").forGetter(s -> Optional.ofNullable(s.getTextureOverride()))
    ).apply(instance, SoftFluidCodec::decode));


    private static SoftFluid decode(ResourceLocation id, ResourceLocation still, ResourceLocation flowing,
                                    Optional<String> translation, Optional<Integer> luminosity, Optional<Integer> color,
                                    Optional<SoftFluid.TintMethod> tint, Optional<FoodProvider> food, Optional<List<String>> nbtKeys,
                                    Optional<List<FluidContainerList.Category>> containers, Optional<List<Fluid>> equivalent,
                                    Optional<ResourceLocation> textureFrom) {
        SoftFluid.Builder builder = new SoftFluid.Builder(still, flowing, id);
        translation.ifPresent(builder::translationKey);
        luminosity.ifPresent(builder::luminosity);
        color.ifPresent(builder::color);
        tint.ifPresent(builder::tintMethod);
        food.ifPresent(builder::food);
        nbtKeys.ifPresent(k -> k.forEach(builder::keepNBTFromItem));
        containers.ifPresent(b -> builder.containers(new FluidContainerList(b)));
        equivalent.ifPresent(e -> e.forEach(builder::addEqFluid));
        textureFrom.ifPresent(builder::copyTexturesFrom);
        return new SoftFluid(builder);
    }

    private static final SoftFluid DEFAULT_DUMMY = new SoftFluid(new SoftFluid.Builder(new ResourceLocation(""), new ResourceLocation(""), new ResourceLocation("")));

    //hacky. gets an optional if the fluid value is its default one
    private static <T> Function<SoftFluid, Optional<T>> getHackyOptional(final Function<SoftFluid, T> getter) {
        return f -> {
            var value = getter.apply(f);
            var def = getter.apply(DEFAULT_DUMMY);
            return value == null || value.equals(def) ? Optional.empty() : Optional.of(value);
        };
    }
}
