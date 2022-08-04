package net.mehvahdjukaar.moonlight.api.fluids;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.moonlight.api.misc.Triplet;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.moonlight.api.util.math.colors.BaseColor;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;


/**
 * Never store an instance of this directly. Always use SoftFluidReference otherwise it will not work on reload
 */
@SuppressWarnings({"unused", "OptionalUsedAsFieldOrParameterType"})
public class SoftFluid {


    private final ResourceLocation stillTexture;
    private final ResourceLocation flowingTexture;

    private final String fromMod;
    private final String translationKey;
    private final int luminosity;
    private final int tintColor;
    private final TintMethod tintMethod;
    private final List<Fluid> equivalentFluids;
    private final FluidContainerList containerList;
    private final FoodProvider food;
    private final List<String> NBTFromItem;

    @Nullable
    private final ResourceLocation useTexturesFrom;

    //used to indicate if it has been directly converted from a forge fluid
    public final boolean isGenerated;

    private SoftFluid(Builder builder) {
        this.tintMethod = builder.tintMethod;
        this.equivalentFluids = builder.equivalentFluids;
        this.luminosity = builder.luminosity;
        this.containerList = builder.containerList;
        this.food = builder.food;
        this.fromMod = builder.fromMod;
        this.translationKey = builder.translationKey;
        this.NBTFromItem = builder.NBTFromItem;

        this.useTexturesFrom = builder.useTexturesFrom;

        ResourceLocation still = builder.stillTexture;
        ResourceLocation flowing = builder.flowingTexture;
        int tint = builder.tintColor;

        Triplet<ResourceLocation, ResourceLocation, Integer> renderingData;
        if (this.useTexturesFrom != null) {
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


        //TODO: remove
        this.isGenerated = builder.custom;
    }

    //better to call registry directly. Here we cache the name
    private final Supplier<ResourceLocation> cachedID = Suppliers.memoize(() -> SoftFluidRegistry.getID(this));

    @Deprecated
    public ResourceLocation getRegistryName() {
        return cachedID.get();
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
        return Component.translatable(this.translationKey);
    }

    public String getTranslationKey() {
        return translationKey;
    }

    public String getFromMod() {
        return fromMod;
    }

    /**
     * gets equivalent forge fluid if present
     *
     * @return forge fluid
     */
    public Fluid getForgeFluid() {
        for (Fluid fluid : this.getEquivalentFluids()) {
            return fluid;
        }
        return Fluids.EMPTY;
    }

    /**
     * @return name of nbt tag that will be transferred from container item to fluid
     */
    public List<String> getNbtKeyFromItem() {
        return NBTFromItem;
    }

    /**
     * gets equivalent forge fluids if present
     *
     * @return forge fluid
     */
    public List<Fluid> getEquivalentFluids() {
        return this.equivalentFluids;
    }

    /**
     * is equivalent to forge fluid
     *
     * @param fluid forge fluid
     * @return equivalent
     */
    public boolean isEquivalent(Fluid fluid) {
        return this.equivalentFluids.contains(fluid);
    }

    public boolean isEmpty() {
        return this == SoftFluidRegistry.EMPTY;
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

    /**
     * @return tint color. default is -1 (white) so effectively no tint
     */
    public int getTintColor() {
        return tintColor;
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


    //TODO: builder isn't needed anymore. maybe remove
    @SuppressWarnings("UnusedReturnValue")
    public static class Builder {
        private ResourceLocation stillTexture;
        private ResourceLocation flowingTexture;

        private String fromMod = "minecraft";
        private String translationKey = "fluid.selene.generic_fluid";

        private int luminosity = 0;
        private int tintColor = -1;
        private TintMethod tintMethod = TintMethod.STILL_AND_FLOWING;

        private FoodProvider food = FoodProvider.EMPTY;
        private FluidContainerList containerList = new FluidContainerList();

        private final List<String> NBTFromItem = new ArrayList<>();
        private final List<Fluid> equivalentFluids = new ArrayList<>();

        //used to indicate automatically generated fluids
        public boolean custom = true;
        private ResourceLocation useTexturesFrom;

        /**
         * default builder. If provided id namespace mod is not loaded it will not be registered
         *
         * @param stillTexture   still fluid texture
         * @param flowingTexture flowing fluid texture
         */
        public Builder(ResourceLocation stillTexture, ResourceLocation flowingTexture) {
            this.stillTexture = stillTexture;
            this.flowingTexture = flowingTexture;
        }

        /**
         * builder from forge fluid
         *
         * @param fluid equivalent forge fluid
         */
        public Builder(Fluid fluid) {
            this(new ResourceLocation("block/water_still"), new ResourceLocation("minecraft:block/water_flowing"));
            //these textures are later overwritten by copy textures from;
            this.copyTexturesFrom(Utils.getID(fluid));
            this.addEqFluid(fluid);

            addFluidSpecificAttributes(this, fluid);
        }


        /**
         * adds textures
         *
         * @param still still fluid texture
         * @param flow  flowing fluid texture
         * @return builder
         */
        public final Builder textures(ResourceLocation still, ResourceLocation flow) {
            this.stillTexture = still;
            this.flowingTexture = flow;
            return this;
        }

        public final Builder translationKey(String translationKey) {
            if (translationKey != null) {
                this.translationKey = translationKey;
            }
            return this;
        }

        /**
         * @param NBTkey name of the nbt tag that, if present in a container item, will get assigned to the fluid. i.e. "Potion" for potions
         * @return builder
         */
        public final Builder keepNBTFromItem(String... NBTkey) {
            this.NBTFromItem.addAll(Arrays.asList(NBTkey));
            return this;
        }

        /**
         * @param tintColor fluid tint color.
         * @return builder
         */
        public final Builder color(int tintColor) {
            this.tintColor = tintColor;
            return this;
        }

        /**
         * used for fluids that only have a colored still and flowing texture and do not need tint (except for particles)
         *
         * @return builder
         */
        public final Builder noTint() {
            this.tintMethod = TintMethod.NO_TINT;
            return this;
        }

        public final Builder tinted() {
            this.tintMethod = TintMethod.STILL_AND_FLOWING;
            return this;
        }

        /**
         * only tint flowing texture. No need to give tint color since it will use particle color
         */
        public final Builder onlyFlowingTinted() {
            this.tintMethod = TintMethod.FLOWING;
            return this;
        }

        public final Builder tintMethod(TintMethod tint) {
            this.tintMethod = tint;
            return this;
        }

        public final Builder luminosity(int luminosity) {
            this.luminosity = luminosity;
            return this;
        }

        /**
         * adds equivalent forge fluid
         *
         * @param fluid equivalent forge fluid
         * @return builder
         */
        public final Builder addEqFluid(Fluid fluid) {
            if (fluid != null && fluid != Fluids.EMPTY) {
                this.equivalentFluids.add(fluid);
                Item i = fluid.getBucket();
                if (i != Items.AIR && i != Items.BUCKET) this.bucket(i);
            }
            return this;
        }

        /**
         * gives this soft fluid a flowing & still texture from a forge fluid that might not be installed. Also copies color
         *
         * @param fluidRes modded optional forge fluid from which the texture will be taken
         * @return builder
         */
        public final Builder copyTexturesFrom(ResourceLocation fluidRes) {
            this.useTexturesFrom = fluidRes;
            return this;
        }

        public final Builder copyTexturesFrom(String fluidRes) {
            return copyTexturesFrom(new ResourceLocation(fluidRes));
        }

        /**
         * adds an item containing this fluid
         *
         * @param filledItem   filled item
         * @param emptyItem    filled item
         * @param itemCapacity bottle equivalent of fluid contained in this filled item
         * @return builder
         */
        public final Builder containerItem(Item filledItem, Item emptyItem, int itemCapacity) {
            if (filledItem != Items.AIR) {
                this.containerList.add(emptyItem, filledItem, itemCapacity);
            }
            return this;
        }

        public final Builder containerItem(Item filledItem, Item emptyItem, int itemCapacity, SoundEvent fillSound, SoundEvent emptySound) {
            if (filledItem != Items.AIR) {
                this.containerList.add(emptyItem, filledItem, itemCapacity, fillSound, emptySound);
            }
            return this;
        }

        public final Builder containers(FluidContainerList containerList) {
            this.containerList = containerList;
            return this;
        }

        /**
         * adds an item containing this fluid that does not have an empty container. Returns empty hand when placed in the tank
         *
         * @param filledItem   filled item
         * @param itemCapacity bottle equivalent of fluid contained in this filled item
         * @return builder
         */
        public final Builder emptyHandContainerItem(Item filledItem, int itemCapacity) {
            if (filledItem != Items.AIR) {
                return containerItem(filledItem, Items.AIR, itemCapacity);
            }
            return this;
        }

        /**
         * adds a bottle containing this fluid
         *
         * @param item filled bottle
         * @return builder
         */
        public final Builder bottle(Item item) {
            this.containerItem(item, Items.GLASS_BOTTLE, BOTTLE_COUNT);
            return this;
        }

        /**
         * adds a bottle containing this fluid & sets it as food
         *
         * @param item filled bottle
         * @return builder
         */
        public final Builder drink(Item item) {
            return this.bottle(item).food(item, BOTTLE_COUNT);
        }

        /**
         * adds a bucket containing this fluid
         *
         * @param item filled bucket
         * @return builder
         */
        public final Builder bucket(Item item) {
            this.containerItem(item, Items.BUCKET, BUCKET_COUNT, SoundEvents.BUCKET_FILL, SoundEvents.BUCKET_EMPTY);
            return this;
        }

        /**
         * adds a bowl containing this fluid
         *
         * @param item filled bowl
         * @return builder
         */
        public final Builder bowl(Item item) {
            this.containerItem(item, Items.BOWL, BOWL_COUNT);
            return this;
        }

        /**
         * adds a bowl containing this fluid & sets it as food
         *
         * @param item filled bowl
         * @return builder
         */
        public final Builder stew(Item item) {
            return this.bowl(item).food(item, BOWL_COUNT);
        }

        /**
         * adds associated food
         *
         * @param item food item
         * @return builder
         */
        public final Builder food(Item item) {
            return this.food(item, 1);
        }

        /**
         * adds associated food
         *
         * @param item        food item
         * @param foodDivider divider for the food effects. i.e: 2 for bowls. Same as bottles of fluid contained in item
         * @return builder
         */
        public final Builder food(Item item, int foodDivider) {
            if (item != null) this.food(FoodProvider.create(item, foodDivider));
            return this;
        }

        public final Builder food(FoodProvider foodProvider) {
            this.food = foodProvider;
            return this;
        }

        public SoftFluid build() {
            return new SoftFluid(this);
        }

        public final Builder fromMod(String s) {
            this.fromMod = s;
            return this;
        }
    }


    public static final int BOTTLE_COUNT = 1;
    public static final int BOWL_COUNT = 2;
    public static final int BUCKET_COUNT = 4;

    /**
     * NO_TINT for both colored textures
     * FLOWING for when only flowing texture is grayscaled
     * STILL_AND_FLOWING for when both are grayscaled
     */
    public enum TintMethod implements StringRepresentable {
        NO_TINT, //allows special color
        FLOWING, //use particle for flowing
        STILL_AND_FLOWING; //both gray-scaled

        public static final Codec<TintMethod> CODEC = StringRepresentable.fromEnum(TintMethod::values);

        @Override
        public String getSerializedName() {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }

    public static final Codec<SoftFluid> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
            ResourceLocation.CODEC.fieldOf("still_texture").forGetter(SoftFluid::getStillTexture),
            ResourceLocation.CODEC.fieldOf("flowing_texture").forGetter(SoftFluid::getFlowingTexture),
            Codec.STRING.optionalFieldOf("from_mod").forGetter(getHackyOptional(SoftFluid::getFromMod)),
            Codec.STRING.optionalFieldOf("translation_key").forGetter(getHackyOptional(SoftFluid::getTranslationKey)),
            Codec.INT.optionalFieldOf("luminosity").forGetter(getHackyOptional(SoftFluid::getLuminosity)),
            BaseColor.CODEC.optionalFieldOf("color").forGetter(getHackyOptional(SoftFluid::getTintColor)),
            TintMethod.CODEC.optionalFieldOf("tint_method").forGetter(getHackyOptional(SoftFluid::getTintMethod)),
            FoodProvider.CODEC.optionalFieldOf("food").forGetter(getHackyOptional(SoftFluid::getFoodProvider)),
            Codec.STRING.listOf().optionalFieldOf("preserved_tags_from_item").forGetter(getHackyOptional(SoftFluid::getNbtKeyFromItem)),
            FluidContainerList.Category.CODEC.listOf().optionalFieldOf("containers").forGetter(f -> f.getContainerList().encodeList()),
            Registry.FLUID.byNameCodec().listOf().optionalFieldOf("equivalent_fluids")
                    .forGetter(getHackyOptional(s -> s.getEquivalentFluids().stream().toList())),
            ResourceLocation.CODEC.optionalFieldOf("use_texture_from").forGetter(s -> Optional.ofNullable(s.getTextureOverride()))
    ).apply(instance, SoftFluid::create));


    protected static SoftFluid create(ResourceLocation still, ResourceLocation flowing, Optional<String> fromMod,
                                      Optional<String> translation, Optional<Integer> luminosity, Optional<Integer> color,
                                      Optional<TintMethod> tint, Optional<FoodProvider> food, Optional<List<String>> nbtKeys,
                                      Optional<List<FluidContainerList.Category>> containers, Optional<List<Fluid>> equivalent,
                                      Optional<ResourceLocation> textureFrom) {

        Builder builder = new Builder(still, flowing);
        fromMod.ifPresent(builder::fromMod);
        translation.ifPresent(builder::translationKey);
        luminosity.ifPresent(builder::luminosity);
        color.ifPresent(builder::color);
        tint.ifPresent(builder::tintMethod);
        food.ifPresent(builder::food);
        nbtKeys.ifPresent(k -> k.forEach(builder::keepNBTFromItem));
        containers.ifPresent(b -> builder.containers(new FluidContainerList(b)));
        equivalent.ifPresent(e -> e.forEach(builder::addEqFluid));
        textureFrom.ifPresent(builder::copyTexturesFrom);
        return builder.build();
    }

    //merge 2 fluids together. TODO: remove?
    protected static SoftFluid merge(SoftFluid originalFluid, SoftFluid newFluid) {
        var builder = new Builder(newFluid.stillTexture, newFluid.flowingTexture);
        builder.translationKey(newFluid.getTranslationKey());
        builder.luminosity(newFluid.getLuminosity());
        builder.color(newFluid.getTintColor());
        builder.tintMethod(newFluid.getTintMethod());
        newFluid.getNbtKeyFromItem().forEach(builder::keepNBTFromItem);
        originalFluid.getNbtKeyFromItem().forEach(builder::keepNBTFromItem);
        FluidContainerList containerList = newFluid.getContainerList();
        containerList.merge(originalFluid.getContainerList());
        builder.containers(containerList);
        newFluid.getEquivalentFluids().forEach(builder::addEqFluid);
        originalFluid.getEquivalentFluids().forEach(builder::addEqFluid);
        if (originalFluid.useTexturesFrom != null) builder.copyTexturesFrom(originalFluid.useTexturesFrom);
        if (newFluid.useTexturesFrom != null) builder.copyTexturesFrom(newFluid.useTexturesFrom);
        return builder.build();
    }

    private static final SoftFluid DEFAULT_DUMMY = new SoftFluid(new Builder(new ResourceLocation(""), new ResourceLocation("")));

    //hacky. gets an optional if the fluid value is its default one
    private static <T> Function<SoftFluid, Optional<T>> getHackyOptional(final Function<SoftFluid, T> getter) {
        return f -> {
            var value = getter.apply(f);
            var def = getter.apply(DEFAULT_DUMMY);
            return value == null || value.equals(def) ? Optional.empty() : Optional.of(value);
        };
    }

    @ExpectPlatform
    public static void addFluidSpecificAttributes(SoftFluid.Builder builder, Fluid fluid) {
        throw new AssertionError(); //fabric gets nothing here :/
    }

    @Nullable
    @ExpectPlatform
    public static Triplet<ResourceLocation, ResourceLocation, Integer> getRenderingData(ResourceLocation useTexturesFrom) {
        throw new AssertionError();
    }
}
