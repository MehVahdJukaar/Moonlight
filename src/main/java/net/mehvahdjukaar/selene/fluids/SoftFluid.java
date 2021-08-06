package net.mehvahdjukaar.selene.fluids;

import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SoftFluid {

    private final ResourceLocation stillTexture;
    private final ResourceLocation flowingTexture;
    private final int tintColor;
    private final TintMethod tintMethod;
    private final List<Fluid> equivalentFluids;
    private final int luminosity;
    private final Map<Item, FilledContainerCategory> filledContainersMap;
    private final Item foodItem;
    private final int foodDivider;
    private final ResourceLocation id;
    private final String[] NBTFromItem;
    private final String translationKey;
    //used to indicate if it has been directly converted from a forge fluid
    public final boolean isCustom;
    //flag will be raised when required dependencies are not met. will not be registered
    private final boolean disabled;

    public SoftFluid(Builder builder) {
        this.stillTexture = builder.stillTexture;
        this.flowingTexture = builder.flowingTexture;
        this.tintColor = builder.tintColor;
        this.tintMethod = builder.tintMethod;
        this.equivalentFluids = builder.equivalentFluids;
        this.luminosity = builder.luminosity;
        this.filledContainersMap = builder.filledContainers;
        this.foodItem = builder.foodItem;
        this.foodDivider = Math.max(1, builder.foodDivider);
        this.id = builder.id;
        this.translationKey = builder.translationKey;
        this.NBTFromItem = builder.NBTFromItem;
        this.isCustom = builder.custom;
        this.disabled = builder.isDisabled;
    }

    /**
     * @return true if dependsncy mods are not present and fluid should not be registered
     */
    public boolean isDisabled(){
        return this.disabled;
    }

    /**
     * food divider: 1 for bottles, 2 for bowls, 3 for bucket foods
     *
     * @return divider
     */
    public int getFoodDivider() {
        return foodDivider;
    }

    /**
     * @return associated food
     */
    public Item getFoodItem() {
        return foodItem;
    }

    public TranslationTextComponent getTranslatedName() {
        return new TranslationTextComponent(this.translationKey);
    }

    public String getTranslationKey() {
        return translationKey;
    }

    public String getID() {
        return id.toString();
    }
    public ResourceLocation getRegistryName() {
        return id;
    }

    /**
     * gets equivalent forge fluid if present
     *
     * @return forge fluid
     */
    public Fluid getForgeFluid() {
        for (Fluid fluid : this.equivalentFluids) {
            return fluid;
        }
        return Fluids.EMPTY;
    }

    /**
     * @return name of nbt tag that will be transferred from container item to fluid
     */
    public String[] getNbtKeyFromItem() {
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
     * gets filled items category if container can be filled
     * @param emptyContainer empty item container
     * @return filled items category. nulls it can't be filled
     */
    @Nullable
    public FilledContainerCategory tryGettingFilledItems(Item emptyContainer) {
        return this.filledContainersMap.get(emptyContainer);
    }

    /**
     * gets empty item category if container can be emptied
     * @param filledContainer empty item container
     * @return empty item.
     */
    @Nullable
    public Item tryGettingEmptyItem(Item filledContainer) {
        for(Item k : this.filledContainersMap.keySet()){
            for(Item i : this.filledContainersMap.get(k).getItems()){
                if(i == filledContainer) return k;
            }
        }
        return null;
    }


//    /**
//     * does provided bucket contains this fluid
//     *
//     * @param item bucket
//     * @return is same fluid
//     */
//    public boolean isContainedInBottle(Item item) {
//        return this.filledBottles.contains(item);
//    }
//
//    public boolean hasBucket() {
//        return !this.filledBuckets.isEmpty();
//    }

//
//    public Collection<Item> getBowls() {
//        return this.filledBowls;
//    }
//

//    /**
//     * @return filled bottle, null if not present
//     */
//    @Nullable
//    public Item getBottle() {
//        for (Item item : this.filledBottles) {
//            return item;
//        }
//        return null;
//    }
//


    public List<Item> getAllFilledContainers() {
        List<Item> list = new ArrayList<>();
        this.getFilledContainersMap().values().forEach(c->list.addAll(c.filled));
        return list;
    }

    /**
     * use this to access containers and possibly add new container items
     * @return container map
     */
    public Map<Item, FilledContainerCategory> getFilledContainersMap() {
        return filledContainersMap;
    }

    /**
     * @return filled item, null if not present
     */
    @Nullable
    public Item getFilledContainer(Item emptyContainer) {
        FilledContainerCategory c = this.filledContainersMap.get(emptyContainer);
        if(c!=null && !c.isEmpty())return c.getFirstFilled();
        return null;
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
        return this.foodItem != Items.AIR;
    }

    public static class Builder {
        private ResourceLocation stillTexture;
        private ResourceLocation flowingTexture;
        private String translationKey = "fluid.selene.generic_fluid";
        private String[] NBTFromItem = null;
        private int tintColor = -1;
        private TintMethod tintMethod = TintMethod.STILL_AND_FLOWING;
        private int luminosity = 0;
        private Item foodItem = Items.AIR;
        private int foodDivider = 1;
        private ResourceLocation id;
        public boolean isDisabled = false;
        //used to indicate automatically generated fluids
        public boolean custom = true;

        private final List<Fluid> equivalentFluids = new ArrayList<>();

        private final Map<Item, FilledContainerCategory> filledContainers = new LinkedHashMap<>();

        /**
         * default builder. If provided id namespace mod is not loaded it will not be registered
         *
         * @param stillTexture   still fluid texture
         * @param flowingTexture flowing fluid texture
         * @param id             registry id
         */
        public Builder(ResourceLocation stillTexture, ResourceLocation flowingTexture, ResourceLocation id) {
            this.stillTexture = stillTexture;
            this.flowingTexture = flowingTexture;
            this.id = id;
            this.isDisabled = !ModList.get().isLoaded(id.getNamespace());
        }
        public Builder(String stillTexture, String flowingTexture, String id) {
            this(new ResourceLocation(stillTexture), new ResourceLocation(flowingTexture), new ResourceLocation(id));
        }
        public Builder(ResourceLocation stillTexture, ResourceLocation flowingTexture, String id) {
            this(stillTexture, flowingTexture, new ResourceLocation(id));
        }

        /**
         * builder from forge fluid
         *
         * @param fluid equivalent forge fluid
         */
        public Builder(Fluid fluid) {
            FluidAttributes att = fluid.getAttributes();
            this.stillTexture = att.getStillTexture();
            this.flowingTexture = att.getFlowingTexture();
            //TODO: fluid colors can depend on fluid stack
            this.color(att.getColor());
            this.luminosity = att.getLuminosity();
            this.translationKey = att.getTranslationKey();
            this.addEqFluid(fluid);
            this.id = fluid.getRegistryName();
            this.isDisabled = false;
        }

        /**
         * builder for modded forge fluid accessed with registry id
         *
         * @param fluidRes equivalent fluid id
         */
        public Builder(String fluidRes) {
            if (ForgeRegistries.FLUIDS.containsKey(new ResourceLocation(fluidRes))) {
                Fluid fluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(fluidRes));
                if (fluid != null && fluid != Fluids.EMPTY) {
                    FluidAttributes att = fluid.getAttributes();
                    this.stillTexture = att.getStillTexture(new FluidStack(fluid,1));
                    this.flowingTexture = att.getFlowingTexture(new FluidStack(fluid,1));
                    int color = att.getColor();
                    if(color==-1)this.tintMethod = TintMethod.NO_TINT;
                    this.color(color);
                    this.bucket(fluid.getBucket());
                    this.luminosity = att.getLuminosity();
                    this.translationKey = att.getTranslationKey();
                    this.addEqFluid(fluid);
                    this.id = fluid.getRegistryName();
                } else this.isDisabled = true;
            } else this.isDisabled = true;
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
            if (translationKey != null){
                this.translationKey = translationKey;
            }
            return this;
        }

        /**
         * @param NBTkey name of the nbt tag that, if present in a container item, will get assigned to the fluid. i.e. "Potion" for potions
         * @return builder
         */
        public final Builder keepNBTFromItem(String ...NBTkey) {
            this.NBTFromItem = NBTkey;
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
         * @return
         */
        public final Builder onlyFlowingTinted() {
            this.tintMethod = TintMethod.FLOWING;
            return this;
        }

        public final Builder luminosity(int luminosity) {
            this.luminosity = luminosity;
            return this;
        }

        /**
         * adds equivalent forge fluid also adding its bucket
         *
         * @param fluidRes equivalent forge fluid id
         * @return builder
         */
        public final Builder addEqFluid(ResourceLocation fluidRes) {
            if (ForgeRegistries.FLUIDS.containsKey(fluidRes)) {
                Fluid f = ForgeRegistries.FLUIDS.getValue(fluidRes);
                if (f != null && f != Fluids.EMPTY) {
                    this.equivalentFluids.add(f);
                    Item i = f.getBucket();
                    if (i != Items.AIR && i != Items.BUCKET) this.bucket(i);
                }
            }
            return this;
        }

        /**
         * adds equivalent forge fluid
         *
         * @param res equivalent forge fluid id
         * @return builder
         */
        public final Builder addEqFluid(String res) {
            return this.addEqFluid(new ResourceLocation(res));
        }

        /**
         * adds equivalent forge fluid
         *
         * @param fluid equivalent forge fluid
         * @return builder
         */
        public final Builder addEqFluid(Fluid fluid) {
            return this.addEqFluid(fluid.getRegistryName());
        }

        /**
         * gives this soft fluid a flowing texture from a forge fluid that might not be installed
         *
         * @param fluidRes modded optional forge fluid from which the texture will be taken
         * @return builder
         */
        public final Builder copyFlowingTextureFrom(String fluidRes) {
            if (ForgeRegistries.FLUIDS.containsKey(new ResourceLocation(fluidRes))) {
                Fluid f = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(fluidRes));
                if (f != null && f != Fluids.EMPTY) {
                    this.flowingTexture = f.getAttributes().getFlowingTexture();
                    //this.stillTexture = f.getAttributes().getStillTexture();
                }
            }
            return this;
        }

        /**
         * gives this soft fluid a flowing & still texture from a forge fluid that might not be installed
         *
         * @param fluidRes modded optional forge fluid from which the texture will be taken
         * @return builder
         */
        public final Builder copyTexturesFrom(String fluidRes) {
            if (ForgeRegistries.FLUIDS.containsKey(new ResourceLocation(fluidRes))) {
                Fluid f = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(fluidRes));
                if (f != null && f != Fluids.EMPTY) {
                    this.flowingTexture = f.getAttributes().getFlowingTexture();
                    this.stillTexture = f.getAttributes().getStillTexture();
                }
            }
            return this;
        }

        /**
         * you can call this when creating soft fluids for other mods. They will be disabled if said mod is not installed
         * also sets fluid id namespace to provided mod namespace.
         * Note that by default a fluid will be disabled automatically if its id namespace mod is not loaded
         *
         * @param modId mod id of target mod
         * @return builder
         */
        public final Builder fromMod(String modId) {
            this.isDisabled = !ModList.get().isLoaded(modId);
            if(id!=null && !id.getNamespace().equals(modId))id = new ResourceLocation(modId,id.getPath());
            return this;
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
                FilledContainerCategory c = this.filledContainers.computeIfAbsent(emptyItem, s ->
                        new FilledContainerCategory(itemCapacity));
                c.addItem(filledItem);
            }
            return this;
        }

        /**
         * adds an item containing this fluid
         *
         * @param filledItemRes filled item id
         * @param emptyItemRes  empty item id
         * @param itemCapacity  bottle equivalent of fluid contained in this filled item
         * @return builder
         */
        public final Builder containerItem(ResourceLocation filledItemRes, ResourceLocation emptyItemRes, int itemCapacity) {
            Item filled = ForgeRegistries.ITEMS.getValue(filledItemRes);
            Item empty = ForgeRegistries.ITEMS.getValue(emptyItemRes);

            if (filled != null && empty != null) this.containerItem(filled, empty, itemCapacity);

            return this;
        }

        /**
         * adds an item containing this fluid
         *
         * @param filledItemRes filled item id
         * @param emptyItemRes  empty item id
         * @param itemCapacity  bottle equivalent of fluid contained in this filled item
         * @return builder
         */
        public final Builder containerItem(String filledItemRes, String emptyItemRes, int itemCapacity) {
            return this.containerItem(new ResourceLocation(filledItemRes), new ResourceLocation(emptyItemRes), itemCapacity);
        }

        /**
         * adds an item containing this fluid that does not have an empty container. Returns empty hand when placed in the tank
         * @param filledItem   filled item
         * @param itemCapacity bottle equivalent of fluid contained in this filled item
         * @return builder
         */
        public final Builder emptyHandContainerItem(Item filledItem, int itemCapacity) {
            if (filledItem != Items.AIR) {
                return containerItem(filledItem,Items.AIR,itemCapacity);
            }
            return this;
        }

        /**
         * adds an item containing this fluid that does not have an empty container. Returns empty hand when placed in the tank
         * @param filledItemRes filled item id
         * @param itemCapacity bottle equivalent of fluid contained in this filled item
         * @return builder
         */
        public final Builder emptyHandContainerItem(ResourceLocation filledItemRes, int itemCapacity) {
            return this.containerItem(filledItemRes, new ResourceLocation("minecraft:air"), itemCapacity);
        }

        /**
         * adds an item containing this fluid that does not have an empty container. Returns empty hand when placed in the tank
         * @param filledItemRes filled item id
         * @param itemCapacity bottle equivalent of fluid contained in this filled item
         * @return builder
         */
        public final Builder emptyHandContainerItem(String filledItemRes, int itemCapacity) {
            return this.containerItem(filledItemRes, "minecraft:air", itemCapacity);
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
         * adds a bottle containing this fluid
         *
         * @param itemRes filled bottle id
         * @return builder
         */
        public final Builder bottle(ResourceLocation itemRes) {
            if (ForgeRegistries.ITEMS.containsKey(itemRes)) {
                Item i = ForgeRegistries.ITEMS.getValue(itemRes);
                if (i != null) this.bottle(i);
            }
            return this;
        }

        /**
         * adds a bottle containing this fluid
         *
         * @param res filled bottle id
         * @return builder
         */
        public final Builder bottle(String res) {
            return this.bottle(new ResourceLocation(res));
        }

        /**
         * adds a bottle containing this fluid & sets it as food
         *
         * @param item filled bottle
         * @return builder
         */
        public final Builder drink(Item item) {
            return this.bottle(item).food(item,BOTTLE_COUNT);
        }

        /**
         * adds a bottle containing this fluid & sets it as food
         *
         * @param res filled bottle
         * @return builder
         */
        public final Builder drink(String res) {
            return this.bottle(res).food(res,BOTTLE_COUNT);
        }

        /**
         * adds a bucket containing this fluid
         *
         * @param item filled bucket
         * @return builder
         */
        public final Builder bucket(Item item) {
            if (item != Items.AIR) {
                Item i = Items.BUCKET;
                FilledContainerCategory c = this.filledContainers.computeIfAbsent(i, s ->
                        new FilledContainerCategory(BUCKET_COUNT, SoundEvents.BUCKET_FILL, SoundEvents.BUCKET_EMPTY));
                c.addItem(item);
            }
            return this;
        }

        /**
         * adds a bucket containing this fluid
         *
         * @param itemRes filled bucket id
         * @return builder
         */
        public final Builder bucket(ResourceLocation itemRes) {
            if (ForgeRegistries.ITEMS.containsKey(itemRes)) {
                Item i = ForgeRegistries.ITEMS.getValue(itemRes);
                if (i != null) this.bucket(i);
            }
            return this;
        }

        /**
         * adds a bucket containing this fluid
         *
         * @param res filled bucket id
         * @return builder
         */
        public final Builder bucket(String res) {
            return this.bucket(new ResourceLocation(res));
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
         * adds a bowl containing this fluid
         *
         * @param itemRes filled bowl id
         * @return builder
         */
        public final Builder bowl(ResourceLocation itemRes) {
            if (ForgeRegistries.ITEMS.containsKey(itemRes)) {
                Item i = ForgeRegistries.ITEMS.getValue(itemRes);
                if (i != null) this.bowl(i);
            }
            return this;
        }

        /**
         * adds a bowl containing this fluid
         *
         * @param res filled bowl id
         * @return builder
         */
        public final Builder bowl(String res) {
            return this.bowl(new ResourceLocation(res));
        }

        /**
         * adds a bowl containing this fluid & sets it as food
         *
         * @param item filled bowl
         * @return builder
         */
        public final Builder stew(Item item) {
            return this.bowl(item).food(item,BOWL_COUNT);
        }

        /**
         * adds a bowl containing this fluid & sets it as food
         *
         * @param res filled bowl
         * @return builder
         */
        public final Builder stew(String res) {
            return this.bowl(res).food(res,BOWL_COUNT);
        }

        /**
         * sets fill & empty sounds associated to a certain empty container (i.e:empty bucket)
         * Always call after adding empty containers
         *
         * @param fill  fill sound event
         * @param empty empty sound event
         * @return builder
         */
        //TODO: make so it creates category if it doesn't exist
        public final Builder setSoundsForCategory(SoundEvent fill, SoundEvent empty, Item emptyContainer) {
            FilledContainerCategory c = this.filledContainers.get(emptyContainer);
            if (c != null) c.setSounds(fill, empty);
            return this;
        }

        /**
         * sets fill & empty sounds associated to a buckets
         *
         * @param fill  fill sound event
         * @param empty empty sound event
         * @return builder
         */
        public final Builder setBucketSounds(SoundEvent fill, SoundEvent empty) {
            return this.setSoundsForCategory(fill, empty, Items.BUCKET);
        }

        /**
         * adds associated food
         * @param item food item
         * @return builder
         */
        public final Builder food(Item item) {
            return this.food(item,1);
        }
        /**
         * adds associated food
         * @param itemRes food item id
         * @return builder
         */
        public final Builder food(ResourceLocation itemRes) {
            return this.food(itemRes,1);
        }
        /**
         * adds associated food
         * @param res food item id
         * @return builder
         */
        public final Builder food(String res) {
            return this.food(res,1);
        }
        /**
         * adds associated food
         * @param item food item
         * @param foodDivider divider for the food effects. i.e: 2 for bowls. Same as bottles of fluid contained in item
         * @return builder
         */
        public final Builder food(Item item, int foodDivider) {
            if (item != null && item != Items.AIR)
                this.foodDivider = foodDivider;
                this.foodItem = item;
            return this;
        }
        /**
         * adds associated food
         * @param res food item id
         * @param foodDivider divider for the food effects. i.e: 2 for bowls. Same as bottles of fluid contained in item
         * @return builder
         */
        public final Builder food(ResourceLocation res, int foodDivider) {
            if (ForgeRegistries.ITEMS.containsKey(res)) {
                Item i = ForgeRegistries.ITEMS.getValue(res);
                if (i != null) this.food(i,foodDivider);
            }
            return this;
        }
        /**
         * adds associated food
         * @param res food item id
         * @param foodDivider divider for the food effects. i.e: 2 for bowls. Same as bottles of fluid contained in item
         * @return builder
         */
        public final Builder food(String res, int foodDivider) {
            return this.food(new ResourceLocation(res),foodDivider);
        }
    }

    public static class FilledContainerCategory {
        private final int containerCapacity;
        private SoundEvent fillSound;
        private SoundEvent emptySound;
        private final List<Item> filled = new ArrayList<>();


        public FilledContainerCategory(int containerCapacity, @Nullable SoundEvent fillSound, @Nullable SoundEvent emptySound) {
            this.containerCapacity = containerCapacity;
            this.fillSound = fillSound;
            this.emptySound = emptySound;
        }

        public FilledContainerCategory(int containerCapacity) {
            this(containerCapacity, SoundEvents.BOTTLE_FILL, SoundEvents.BOTTLE_EMPTY);
        }

        public void addItem(Item i) {
            if(!filled.contains(i)) filled.add(i);
        }

        public void setSounds(@Nullable SoundEvent fillSound, @Nullable SoundEvent emptySound) {
            this.emptySound = emptySound;
            this.fillSound = fillSound;
        }

        /**
         * @return amount of liquid contained in this item in bottles
         */
        public int getAmount() {
            return containerCapacity;
        }

        @Nullable
        public SoundEvent getFillSound() {
            return fillSound;
        }

        @Nullable
        public SoundEvent getEmptySound() {
            return emptySound;
        }

        public List<Item> getItems() {
            return filled;
        }

        public boolean isEmpty() {
            return this.filled.isEmpty();
        }
        @Nullable
        public Item getFirstFilled() {
            if(this.isEmpty())return null;
            return this.filled.get(0);
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
    public enum TintMethod{
        NO_TINT, //allows special color
        FLOWING, //use particle for flowing
        STILL_AND_FLOWING //both grayscaled
    }
}
