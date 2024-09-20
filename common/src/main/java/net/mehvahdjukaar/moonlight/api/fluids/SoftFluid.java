package net.mehvahdjukaar.moonlight.api.fluids;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.moonlight.api.misc.Triplet;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.moonlight.api.util.math.ColorUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Optional;


/**
 * Never store an instance of this directly. Always use SoftFluidReference otherwise it will not work on reload
 */
@SuppressWarnings({"unused", "OptionalUsedAsFieldOrParameterType"})
public class SoftFluid {

    private final Component name;
    private final HolderSet<Fluid> equivalentFluids;
    private final FluidContainerList containerList;
    private final FoodProvider food;
    private final HolderSet<DataComponentType<?>> preservedComponentsFromItem;

    //used to indicate if it has been directly converted from a forge fluid
    public final boolean isGenerated;

    //client only

    private final ResourceLocation stillTexture;
    private final ResourceLocation flowingTexture;
    @Nullable
    private final ResourceLocation useTexturesFrom;
    private final int luminosity;
    private final int emissivity;
    private final int tintColor;
    //determines to which textures to apply tintColor
    private final TintMethod tintMethod;

    //populated with reload listener. Includes tintColor information
    protected int averageTextureTint = -1;

    protected SoftFluid(ResourceLocation still, ResourceLocation flowing,
                        Component name, int luminosity, int emissivity,
                        int color, TintMethod tintMethod,
                        FoodProvider food, HolderSet<DataComponentType<?>> components,
                        FluidContainerList containers, HolderSet<Fluid> equivalent,
                        Optional<ResourceLocation> textureFrom) {

        this.tintMethod = tintMethod;
        this.equivalentFluids = equivalent;
        this.luminosity = luminosity;
        this.emissivity = Math.max(emissivity, luminosity);
        this.containerList = containers;
        this.food = food;
        this.name = name;
        this.preservedComponentsFromItem = components;

        this.useTexturesFrom = textureFrom.orElse(null);

        int tint = color;

        Triplet<ResourceLocation, ResourceLocation, Integer> renderingData;
        if (this.useTexturesFrom != null && PlatHelper.getPhysicalSide().isClient()) {
            var data = getRenderingData(useTexturesFrom);
            if (data != null) {
                still = data.left();
                flowing = data.middle();
                tint = data.right();
            }
        }
        this.stillTexture = still;
        this.flowingTexture = flowing;
        this.tintColor = tint;

        this.isGenerated = false;
    }

    public SoftFluid(Holder<Fluid> fluid) {
        var still = ResourceLocation.parse("block/water_still");
        var flowing = ResourceLocation.parse("block/water_flowing");
        this.tintMethod = TintMethod.STILL_AND_FLOWING;
        this.containerList = new FluidContainerList();
        this.food = FoodProvider.EMPTY;
        this.preservedComponentsFromItem = HolderSet.empty();

        //these textures are later overwritten by copy textures from;
        this.useTexturesFrom = fluid.unwrapKey().get().location();
        this.equivalentFluids = HolderSet.direct(fluid);
        var pair = getFluidSpecificAttributes(fluid.value());
        this.name = pair.getSecond() == null ? Component.literal("generic fluid") : pair.getSecond();
        this.luminosity = pair.getFirst();
        this.emissivity = pair.getFirst();

        int tint = -1;

        Triplet<ResourceLocation, ResourceLocation, Integer> renderingData;
        if (this.useTexturesFrom != null && PlatHelper.getPhysicalSide().isClient()) {
            var data = getRenderingData(useTexturesFrom);
            if (data != null) {
                still = data.left();
                flowing = data.middle();
                tint = data.right();
            }
        }
        this.stillTexture = still;
        this.flowingTexture = flowing;
        this.tintColor = tint;

        this.isGenerated = true;
    }


    public void afterInit() {
        for (var f : equivalentFluids) {
            Item i = f.value().getBucket();
            if (i != Items.AIR && i != Items.BUCKET) {
                this.containerList.add(i, Items.BUCKET, BUCKET_COUNT, SoundEvents.BUCKET_FILL, SoundEvents.BUCKET_EMPTY);
            }
        }
    }

    @Nullable
    public ResourceLocation getTextureOverride() {
        return useTexturesFrom;
    }

    /**
     * @return associated food
     */
    public FoodProvider getFoodProvider() {
        return food;
    }

    public Component getTranslatedName() {
        return name;
    }

    public boolean isEnabled() {
        return equivalentFluids.size() != 0 || !containerList.getPossibleFilled().isEmpty();
    }

    /**
     * gets equivalent forge fluid if present
     *
     * @return forge fluid
     */
    public Holder<Fluid> getVanillaFluid() {
        for (var fluid : this.getEquivalentFluids()) {
            return fluid;
        }
        return Fluids.EMPTY.builtInRegistryHolder();
    }

    /**
     * @return name of nbt tag that will be transferred from container item to fluid
     */
    public HolderSet<DataComponentType<?>> getPreservedComponents() {
        return preservedComponentsFromItem;
    }

    /**
     * gets equivalent forge fluids if present
     *
     * @return forge fluid
     */
    public HolderSet<Fluid> getEquivalentFluids() {
        return this.equivalentFluids;
    }

    /**
     * is equivalent to forge fluid
     *
     * @param fluid forge fluid
     * @return equivalent
     */
    public boolean isEquivalent(Holder<Fluid> fluid) {
        return this.equivalentFluids.contains(fluid);
    }

    public boolean isEmptyFluid() {
        return this == SoftFluidRegistry.empty();
    }

    /**
     * gets filled item category if container can be emptied
     *
     * @param emptyContainer empty item container
     * @return empty item.
     */
    public Optional<Item> getFilledContainer(Item emptyContainer) {
        return this.containerList.getFilled(emptyContainer);
    }

    /**
     * gets empty item if container can be emptied
     *
     * @param filledContainer empty item container
     * @return empty item.
     */
    public Optional<Item> getEmptyContainer(Item filledContainer) {
        return this.containerList.getEmpty(filledContainer);
    }

    /**
     * use this to access containers and possibly add new container items
     *
     * @return container map
     */
    public FluidContainerList getContainerList() {
        return containerList;
    }

    public int getLuminosity() {
        return luminosity;
    }

    public int getEmissivity() {
        return emissivity;
    }

    /**
     * @return tint color. default is -1 (white) so effectively no tint
     */
    public int getTintColor() {
        return tintColor;
    }

    public int getAverageTextureTintColor() {
        return averageTextureTint;
    }

    /**
     * @return used for fluids that only have a colored still texture and a grayscaled flowing one
     */
    public TintMethod getTintMethod() {
        return tintMethod;
    }

    /**
     * @return tint color to be used on flowing fluid texture. -1 for no tint
     */
    public boolean isColored() {
        return this.tintColor != -1;
    }

    public ResourceLocation getFlowingTexture() {
        return flowingTexture;
    }

    public ResourceLocation getStillTexture() {
        return stillTexture;
    }

    public boolean isFood() {
        return !this.food.isEmpty();
    }

    public static final int BOTTLE_COUNT = Capacity.BOTTLE.getValue();
    public static final int BOWL_COUNT = Capacity.BOWL.getValue();
    public static final int BUCKET_COUNT = Capacity.BUCKET.getValue();
    public static final int WATER_BUCKET_COUNT = 3;

    /**
     * NO_TINT for both colored textures
     * FLOWING for when only flowing texture is grayscaled
     * STILL_AND_FLOWING for when both are grayscaled
     */
    public enum TintMethod implements StringRepresentable {
        NO_TINT, //allows special color
        FLOWING, //use particle for flowing. Still texture wont have any color
        STILL_AND_FLOWING; //both texture needs to be gray-scaled and will be colored

        public static final Codec<TintMethod> CODEC = StringRepresentable.fromEnum(TintMethod::values);

        @Override
        public String getSerializedName() {
            return this.name().toLowerCase(Locale.ROOT);
        }

        public boolean appliesToFlowing() {
            return this == FLOWING || this == STILL_AND_FLOWING;
        }

        public boolean appliesToStill() {
            return this == STILL_AND_FLOWING;
        }
    }

    public static final Codec<Holder<SoftFluid>> HOLDER_CODEC = RegistryFileCodec.create(SoftFluidRegistry.KEY, SoftFluid.CODEC);
    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<SoftFluid>> STREAM_CODEC = ByteBufCodecs.holderRegistry(SoftFluidRegistry.KEY);

    public static final Codec<Component> COMPONENT_CODEC = Codec.either(ComponentSerialization.FLAT_CODEC, Codec.STRING).xmap(
            either -> either.map(c -> c, Component::translatable), Either::left);


    //Direct codec
    public static final Codec<SoftFluid> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
            ResourceLocation.CODEC.fieldOf("still_texture").forGetter(SoftFluid::getStillTexture),
            ResourceLocation.CODEC.fieldOf("flowing_texture").forGetter(SoftFluid::getFlowingTexture),
            COMPONENT_CODEC.optionalFieldOf("translation_key", Component.translatable("fluid.moonlight.generic_fluid")).forGetter(SoftFluid::getTranslatedName),
            Codec.intRange(0, 15).optionalFieldOf("luminosity", 0).forGetter(SoftFluid::getLuminosity),
            Codec.intRange(0, 15).optionalFieldOf("emissivity", 0).forGetter(SoftFluid::getEmissivity),
            ColorUtils.CODEC.optionalFieldOf("color", -1).forGetter(SoftFluid::getTintColor),
            TintMethod.CODEC.optionalFieldOf("tint_method", TintMethod.STILL_AND_FLOWING).forGetter(SoftFluid::getTintMethod),
            FoodProvider.CODEC.optionalFieldOf("food", FoodProvider.EMPTY).forGetter(SoftFluid::getFoodProvider),
            Utils.lenientHomogeneousList(Registries.DATA_COMPONENT_TYPE)
                    .optionalFieldOf("preserved_components_from_item", HolderSet.empty())
                    .forGetter(SoftFluid::getPreservedComponents),
            FluidContainerList.CODEC.optionalFieldOf("containers", new FluidContainerList()).forGetter(SoftFluid::getContainerList),
            Utils.lenientHomogeneousList(Registries.FLUID).optionalFieldOf("equivalent_fluids",HolderSet.empty())
                    .forGetter(s -> s.equivalentFluids),
            ResourceLocation.CODEC.optionalFieldOf("use_texture_from").forGetter(s -> Optional.ofNullable(s.getTextureOverride()))
    ).apply(instance, SoftFluid::new));


    @ApiStatus.Internal
    @ExpectPlatform
    public static Pair<Integer, Component> getFluidSpecificAttributes(Fluid fluid) {
        throw new AssertionError(); //fabric gets nothing here :/
    }

    //this is client only!
    @ApiStatus.Internal
    @Nullable
    @ExpectPlatform
    public static Triplet<ResourceLocation, ResourceLocation, Integer> getRenderingData(ResourceLocation useTexturesFrom) {
        throw new AssertionError();
    }

    public enum Capacity implements StringRepresentable {
        BOTTLE(1, 1), BOWL(2, 1), BUCKET(4, 3), BLOCK(4, 4);
        public final int value;

        Capacity(int forge, int fabric) {
            value = PlatHelper.getPlatform().isForge() ? forge : fabric;
        }

        public static final Codec<Capacity> CODEC = StringRepresentable.fromEnum(Capacity::values);
        public static final Codec<Integer> INT_CODEC = Codec.either(Codec.INT, Capacity.CODEC).xmap(
                either -> either.map(i -> i, Capacity::getValue), Either::left);


        @Override
        public String getSerializedName() {
            return this.name().toUpperCase(Locale.ROOT);
        }

        public int getValue() {
            return value;
        }
    }
}
