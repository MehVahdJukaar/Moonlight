package net.mehvahdjukaar.moonlight.api.fluids;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.moonlight.api.misc.Triplet;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.moonlight.api.util.math.ColorUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * Never store an instance of this directly. Always use SoftFluidReference otherwise it will not work on reload
 */
@SuppressWarnings({"unused", "OptionalUsedAsFieldOrParameterType"})
public class SoftFluid {

    private final Component name;
    private final LazyFluidSet equivalentFluids;
    private final FluidContainerList containerList;
    private final FoodProvider food;
    private final List<String> NBTFromItem;

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

    private SoftFluid(Builder builder) {
        this.tintMethod = builder.tintMethod;
        this.equivalentFluids = new LazyFluidSet(builder.equivalentFluids);
        this.luminosity = builder.luminosity;
        this.emissivity = builder.emissivity;
        this.containerList = builder.containerList;
        this.food = builder.food;
        this.name = builder.name;
        this.NBTFromItem = builder.NBTFromItem;

        this.useTexturesFrom = builder.useTexturesFrom;

        ResourceLocation still = builder.stillTexture;
        ResourceLocation flowing = builder.flowingTexture;
        int tint = builder.tintColor;

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

        this.isGenerated = builder.isFromData;
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
        return !equivalentFluids.isEmpty() || !containerList.getPossibleFilled().isEmpty();
    }

    /**
     * gets equivalent forge fluid if present
     *
     * @return forge fluid
     */
    public Fluid getVanillaFluid() {
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
        return this.equivalentFluids.getFluids();
    }

    /**
     * is equivalent to forge fluid
     *
     * @param fluid forge fluid
     * @return equivalent
     */
    public boolean isEquivalent(Fluid fluid) {
        return this.equivalentFluids.getFluids().contains(fluid);
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


    //TODO: builder isn't needed anymore. maybe remove
    @SuppressWarnings("UnusedReturnValue")
    public static class Builder {
        private ResourceLocation stillTexture;
        private ResourceLocation flowingTexture;

        private Component name = Component.translatable("fluid.moonlight.generic_fluid");

        private int luminosity = 0;
        private int emissivity = 0;
        private int tintColor = -1;
        private TintMethod tintMethod = TintMethod.STILL_AND_FLOWING;

        private FoodProvider food = FoodProvider.EMPTY;
        private FluidContainerList containerList = new FluidContainerList();

        private final List<String> NBTFromItem = new ArrayList<>();
        private final List<String> equivalentFluids = new ArrayList<>();

        //used to indicate automatically generated fluids
        private boolean isFromData = true;
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
            this(ResourceLocation.parse("block/water_still"), ResourceLocation.parse("minecraft:block/water_flowing"));
            //these textures are later overwritten by copy textures from;
            this.copyTexturesFrom(Utils.getID(fluid));
            this.addEqFluid(fluid);
            this.isFromData = false;
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
                this.name = Component.translatable(translationKey);
            }
            return this;
        }

        public final Builder translation(Component component) {
            if (component != null) {
                this.name = component;
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
            this.emissivity = luminosity;
            return this;
        }

        public final Builder emissivity(int emissivity) {
            this.emissivity = emissivity;
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
                this.equivalentFluids.add(BuiltInRegistries.FLUID.getKey(fluid).toString());
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
            return copyTexturesFrom(ResourceLocation.parse(fluidRes));
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

        @Deprecated(forRemoval = true)
        public final Builder fromMod(String s) {
            return this;
        }
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

    public static final Codec<Component> COMPONENT_CODEC = Codec.either(ComponentSerialization.FLAT_CODEC, Codec.STRING).xmap(
            either -> either.map(c -> c, Component::translatable), Either::left);


    //Direct codec
    public static final Codec<SoftFluid> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
            ResourceLocation.CODEC.fieldOf("still_texture").forGetter(SoftFluid::getStillTexture),
            ResourceLocation.CODEC.fieldOf("flowing_texture").forGetter(SoftFluid::getFlowingTexture),
            COMPONENT_CODEC.optionalFieldOf( "translation_key").forGetter(getHackyOptional(SoftFluid::getTranslatedName)),
            Codec.intRange(0, 15).optionalFieldOf( "luminosity").forGetter(getHackyOptional(SoftFluid::getLuminosity)),
            Codec.intRange(0, 15).optionalFieldOf("emissivity").forGetter(getHackyOptional(SoftFluid::getEmissivity)),
            ColorUtils.CODEC.optionalFieldOf( "color").forGetter(getHackyOptional(SoftFluid::getTintColor)),
            TintMethod.CODEC.optionalFieldOf( "tint_method").forGetter(getHackyOptional(SoftFluid::getTintMethod)),
            FoodProvider.CODEC.optionalFieldOf("food").forGetter(getHackyOptional(SoftFluid::getFoodProvider)),
           Codec.STRING.listOf().optionalFieldOf( "preserved_tags_from_item").forGetter(getHackyOptional(SoftFluid::getNbtKeyFromItem)),
           FluidContainerList.Category.CODEC.listOf().optionalFieldOf( "containers").forGetter(f -> f.getContainerList().encodeList()),
           Codec.STRING.listOf().optionalFieldOf( "equivalent_fluids", new ArrayList<>()).forGetter(s -> s.equivalentFluids.keys),
           ResourceLocation.CODEC.optionalFieldOf( "use_texture_from").forGetter(s -> Optional.ofNullable(s.getTextureOverride()))
    ).apply(instance, SoftFluid::create));


    protected static SoftFluid create(ResourceLocation still, ResourceLocation flowing,
                                      Optional<Component> translation, Optional<Integer> luminosity, Optional<Integer> emissivity,
                                      Optional<Integer> color, Optional<TintMethod> tint,
                                      Optional<FoodProvider> food, Optional<List<String>> nbtKeys,
                                      Optional<List<FluidContainerList.Category>> containers,
                                      List<String> equivalent,
                                      Optional<ResourceLocation> textureFrom) {

        Builder builder = new Builder(still, flowing);
        translation.ifPresent(builder::translation);
        luminosity.ifPresent(builder::luminosity);
        emissivity.ifPresent(builder::emissivity);
        color.ifPresent(builder::color);
        tint.ifPresent(builder::tintMethod);
        food.ifPresent(builder::food);
        nbtKeys.ifPresent(k -> k.forEach(builder::keepNBTFromItem));
        containers.ifPresent(b -> builder.containers(new FluidContainerList(b)));
        builder.equivalentFluids.addAll(equivalent);
        textureFrom.ifPresent(builder::copyTexturesFrom);
        return builder.build();
    }

    private static final SoftFluid DEFAULT_DUMMY = new SoftFluid(new Builder(ResourceLocation.parse(""), ResourceLocation.parse("")));

    //hacky. gets an optional if the fluid value is its default one
    private static <T> Function<SoftFluid, Optional<T>> getHackyOptional(final Function<SoftFluid, T> getter) {
        return f -> {
            var value = getter.apply(f);
            var def = getter.apply(DEFAULT_DUMMY);
            return value == null || value.equals(def) ? Optional.empty() : Optional.of(value);
        };
    }

    @ApiStatus.Internal
    @ExpectPlatform
    public static void addFluidSpecificAttributes(SoftFluid.Builder builder, Fluid fluid) {
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

    //can use tag. Ugly. We cant use HolderSet because tags are loaded after registry entries obviouslu
    private static class LazyFluidSet {
        protected static final LazyFluidSet EMPTY = new LazyFluidSet(Collections.emptyList());

        private final List<String> keys;
        private final List<Fluid> fluids; //respects insertion order
        private final List<TagKey<Fluid>> tags = new ArrayList<>();

        private LazyFluidSet(List<String> keys) {
            this.keys = keys;
            var set = new LinkedHashSet<Fluid>();

            for (String key : keys) {
                if (key.startsWith("#")) {
                    //actually this wont work because we need these before tags are loaded...
                    tags.add(TagKey.create(Registries.FLUID,
                                    ResourceLocation.parse(key.substring(1))));
                }
                else BuiltInRegistries.FLUID.getOptional(ResourceLocation.parse(key)).ifPresent(set::add);
            }
            fluids = List.of(set.toArray(new Fluid[0]));
        }

        public static LazyFluidSet merge(LazyFluidSet first, LazyFluidSet second) {
            if (first.isEmpty()) return second;
            if (second.isEmpty()) return first;
            List<String> keys = new ArrayList<>(first.keys);
            keys.addAll(second.keys);
            return new LazyFluidSet(keys);
        }

        public List<Fluid> getFluids() {
            if(tags.isEmpty()) return fluids;
            var list = new ArrayList<>(fluids);
            for (TagKey<Fluid> tag : tags) {
                BuiltInRegistries.FLUID.getTagOrEmpty(tag).forEach(e->list.add(e.value()));
            }
            return list;
        }

        public boolean isEmpty() {
            return getFluids().isEmpty();
        }
    }
}
