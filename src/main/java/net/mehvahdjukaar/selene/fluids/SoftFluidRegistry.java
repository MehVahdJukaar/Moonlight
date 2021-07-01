package net.mehvahdjukaar.selene.fluids;

import net.minecraft.fluid.FlowingFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.*;

public class SoftFluidRegistry {
    // id -> SoftFluid
    private static final HashMap<String, SoftFluid> ID_MAP = new HashMap<>();
    // filled item -> SoftFluid. need to handle potions separately since they map to same item id
    private static final HashMap<Item, SoftFluid> ITEM_MAP = new HashMap<>();
    // forge fluid  -> SoftFluid
    private static final HashMap<Fluid, SoftFluid> FLUID_MAP = new HashMap<>();

    public static Collection<SoftFluid> getFluids(){
        return ID_MAP.values();
    }

    /**
     * gets a soft fluid provided his registry id
     * @param id fluid registry id
     * @return soft fluid. empty fluid if not found
     */
    public static SoftFluid get(String id){
        return ID_MAP.getOrDefault(id, EMPTY);
    }
    /**
     * gets a soft fluid provided a forge fluid
     * @param fluid equivalent forge fluid
     * @return soft fluid. empty fluid if not found
     */
    public static SoftFluid fromForgeFluid(Fluid fluid){
        return FLUID_MAP.getOrDefault(fluid, EMPTY);
    }
    /**
     * gets a soft fluid provided a bottle like item
     * @param container fluid container item
     * @return soft fluid. empty fluid if not found
     */
    public static SoftFluid fromItem(Item container){
        return ITEM_MAP.getOrDefault(container, EMPTY);
    }


    public static final SoftFluid EMPTY = new SoftFluid(new SoftFluid.Builder(Fluids.EMPTY));
    public static final SoftFluid WATER;
    public static final SoftFluid LAVA;
    public static final SoftFluid HONEY;
    public static final SoftFluid MILK;
    public static final SoftFluid MUSHROOM_STEW;
    public static final SoftFluid BEETROOT_SOUP;
    public static final SoftFluid RABBIT_STEW;
    public static final SoftFluid SUS_STEW;
    public static final SoftFluid POTION;
    public static final SoftFluid DRAGON_BREATH;
    public static final SoftFluid XP;
    public static final SoftFluid SLIME;
    public static final SoftFluid DIRT;
    public static final SoftFluid GHAST_TEAR;
    public static final SoftFluid MAGMA_CREAM;
    public static final SoftFluid SAP;

    //first of all creates all soft fluids for vanilla items without registering them
    static{
        WATER = makeSF(new SoftFluid.Builder(Fluids.WATER)
                //don't put bottles here
                .food(Items.POTION)); //handled via special case in liquid holder along other nbt stff
        LAVA = makeSF(new SoftFluid.Builder(Fluids.LAVA)
                .bottle("alexsmobs:lava_bottle")
                .bucket(Items.LAVA_BUCKET)
                .setBucketSounds(SoundEvents.BUCKET_FILL_LAVA,SoundEvents.BUCKET_EMPTY_LAVA));
        HONEY = makeSF(new SoftFluid.Builder(FluidTextures.HONEY_TEXTURE, FluidTextures.HONEY_TEXTURE,"honey")
                .translationKey("fluid.supplementaries.honey")
                .drink(Items.HONEY_BOTTLE)
                .textureOverrideF("create:honey")
                .addEqFluid("create:honey")
                .addEqFluid("cyclic:honey")
                .addEqFluid("inspirations:honey"));
        MILK = makeSF(new SoftFluid.Builder(FluidTextures.MILK_TEXTURE, FluidTextures.POTION_TEXTURE_FLOW,"milk")
                .bucket(Items.MILK_BUCKET)
                .food(Items.MILK_BUCKET)
                .translationKey("fluid.supplementaries.milk")
                .textureOverrideF("create:milk")
                .addEqFluid("create:milk")
                .addEqFluid("inspirations:milk")
                .bottle("farmersdelight:milk_bottle")
                .bottle("neapolitan:milk_bottle")
                .bottle("fluffy_farmer:bottle_of_milk")
                .bottle("vanillacookbook:milk_bottle")
                .bottle("simplefarming:milk_bottle")
                .bottle("farmersdelight:milk_bottle"));
        MUSHROOM_STEW = makeSF(new SoftFluid.Builder(FluidTextures.SOUP_TEXTURE, FluidTextures.POTION_TEXTURE_FLOW,"mushroom_stew")
                .color(0xffad89)
                .stew(Items.MUSHROOM_STEW)
                .addEqFluid("inspirations:mushroom_stew")
                .textureOverrideF("inspirations:mushroom_stew"));
        BEETROOT_SOUP = makeSF(new SoftFluid.Builder(FluidTextures.SOUP_TEXTURE, FluidTextures.POTION_TEXTURE_FLOW,"beetroot_soup")
                .color(0xC93434)
                .stew(Items.BEETROOT_SOUP)
                .addEqFluid("inspirations:beetroot_soup")
                .textureOverrideF("inspirations:beetroot_soup"));
        RABBIT_STEW = makeSF(new SoftFluid.Builder(FluidTextures.SOUP_TEXTURE, FluidTextures.POTION_TEXTURE_FLOW,"rabbit_stew")
                .color(0xFF904F)
                .stew(Items.RABBIT_STEW)
                .addEqFluid("inspirations:rabbit_stew")
                .textureOverrideF("inspirations:rabbit_stew"));
        SUS_STEW = makeSF(new SoftFluid.Builder(FluidTextures.SOUP_TEXTURE, FluidTextures.POTION_TEXTURE_FLOW,"suspicious_stew")
                .color(0xBAE85F)
                .stew(Items.SUSPICIOUS_STEW)
                .textureOverrideF("inspirations:mushroom_stew"));
        //TODO: automate translation key thing
        POTION = makeSF(new SoftFluid.Builder(FluidTextures.POTION_TEXTURE, FluidTextures.POTION_TEXTURE_FLOW,"potion")
                .color(PotionUtils.getColor(Potions.EMPTY))
                .translationKey(Items.POTION.getDescriptionId())
                .drink(Items.POTION)
                .addEqFluid("create:potion"));
        DRAGON_BREATH = makeSF(new SoftFluid.Builder(FluidTextures.DRAGON_BREATH_TEXTURE, FluidTextures.POTION_TEXTURE_FLOW,"dragon_breath")
                .color(0xFF33FF)
                .luminosity(3)
                .translationKey(Items.DRAGON_BREATH.getDescriptionId())
                .bottle(Items.DRAGON_BREATH));
        XP = makeSF(new SoftFluid.Builder(FluidTextures.XP_TEXTURE, FluidTextures.XP_TEXTURE_FLOW,"experience")
                .translationKey("fluid.supplementaries.experience")
                .textureOverride("cyclic:xpjuice")
                .addEqFluid("cyclic:xpjuice")
                .bottle(Items.EXPERIENCE_BOTTLE));
        SLIME = makeSF(new SoftFluid.Builder(FluidTextures.SLIME_TEXTURE, FluidTextures.SLIME_TEXTURE,"slime")
                .emptyHandContainerItem(Items.SLIME_BALL,1)
                .setSoundsForCategory(SoundEvents.SLIME_BLOCK_PLACE,SoundEvents.SLIME_BLOCK_BREAK,Items.AIR)
                .addEqFluid("cyclic:slime")
                .translationKey("fluid.supplementaries.slime"));
        DIRT = makeSF(new SoftFluid.Builder(FluidTextures.DIRT_TEXTURE, FluidTextures.DIRT_TEXTURE,"dirt")
                .emptyHandContainerItem(Items.DIRT,4)
                .setSoundsForCategory(SoundEvents.GRAVEL_PLACE,SoundEvents.GRAVEL_BREAK,Items.AIR)
                .translationKey("block.minecraft.dirt"));
        GHAST_TEAR = makeSF(new SoftFluid.Builder(FluidTextures.MILK_TEXTURE, FluidTextures.POTION_TEXTURE_FLOW,"ghast_tear")
                .color(0xbff0f0)
                .emptyHandContainerItem(Items.GHAST_TEAR,1)
                .translationKey("item.minecraft.ghast_tear"));
        MAGMA_CREAM = makeSF(new SoftFluid.Builder(FluidTextures.MAGMA_TEXTURE, FluidTextures.MAGMA_TEXTURE_FLOW,"magma_cream")
                .emptyHandContainerItem(Items.MAGMA_CREAM,1)
                .translationKey("item.minecraft.magma_cream"));
        SAP = makeSF(new SoftFluid.Builder(FluidTextures.HONEY_TEXTURE, FluidTextures.POTION_TEXTURE_FLOW,"sap")
                .textureOverride("thermal:sap")
                .color(0xbd6e2a)
                .drink("autumnity:sap_bottle")
                .bucket("thermal:sap_bucket")
                .translationKey("fluid.supplementaries.sap"));
    }


    public static SoftFluid makeSF(SoftFluid.Builder builder){
        if(builder.isDisabled)return null;
        return new SoftFluid(builder);
    }

    public static void addOpt(List<SoftFluid> l, SoftFluid s){
        if(s!=null)l.add(s);
    }

    /**
     * registers provided soft fluid. Automatically replaces any equivalent forge fluid associated with this fluid if already registered
     * does not register fluids marked as disabled (if dependencies are not met)
     * @param s soft fluid to register
     * @return same provided soft fluid
     */
    public static SoftFluid register(SoftFluid s){
        if(s.isDisabled())return s;
        for(Fluid f : s.getEquivalentFluids()){
            //remove non custom equivalent forge fluids in favor of this one
            if(FLUID_MAP.containsKey(f)){
                SoftFluid old = FLUID_MAP.get(f);
                if(!old.isCustom){
                    ID_MAP.remove(old.getID());
                    ITEM_MAP.remove(old.getFilledContainer(Items.BUCKET));
                }
            }
        }
        registerUnchecked(s);
        return s;
    }

    private static void registerUnchecked(SoftFluid...fluids){
        Arrays.stream(fluids).forEach(s->{
            s.getEquivalentFluids().forEach(f->FLUID_MAP.put(f,s));
            s.getAllFilledContainers().forEach(i->ITEM_MAP.put(i,s));
            ID_MAP.put(s.getID(),s);
        });
    }

    private static void convertAndRegisterAllForgeFluids(){
        for (Fluid f : ForgeRegistries.FLUIDS){
            try {
                if (f == null) continue;
                if (f instanceof FlowingFluid && ((FlowingFluid) f).getSource() != f) continue;
                if (f instanceof ForgeFlowingFluid.Flowing || f == Fluids.EMPTY) continue;
                //if fluid map contains fluid it meas that another equivalent fluid has already ben registered
                if(FLUID_MAP.containsKey(f))continue;
                //is not equivalent: create new SoftFluid from forge fluid
                registerUnchecked(new SoftFluid(new SoftFluid.Builder(f)));
            }
            catch (Exception ignored){}
        }
    }

    public static void init() {

        convertAndRegisterAllForgeFluids();

        registerUnchecked(WATER,LAVA,HONEY,MILK,MUSHROOM_STEW,
                SUS_STEW,BEETROOT_SOUP,RABBIT_STEW,POTION,DRAGON_BREATH,XP,SLIME,DIRT,GHAST_TEAR,SAP);

        List<SoftFluid> custom = new ArrayList<>(Collections.emptyList());


        addOpt(custom,makeSF(new SoftFluid.Builder(FluidTextures.POTION_TEXTURE, FluidTextures.POTION_TEXTURE_FLOW,"komodo_spit")
                .fromMod("alexsmobs")
                .color(0xa8b966)
                .translationKey("item.alexmobs.komodo_spit")
                .bottle("alexsmobs:komodo_spit_bottle")));
        addOpt(custom,makeSF(new SoftFluid.Builder(FluidTextures.SOUP_TEXTURE, FluidTextures.POTION_TEXTURE_FLOW,"squash_soup")
                .fromMod("simplefarming")
                .color(0xe6930a)
                .stew("simplefarming:squash_soup")));
        addOpt(custom,makeSF(new SoftFluid.Builder("inspirations:potato_soup")
                .textures(FluidTextures.SOUP_TEXTURE, FluidTextures.POTION_TEXTURE_FLOW)
                .fromMod("inspirations")
                .stew("inspirations:potato_soup")));
        addOpt(custom,makeSF(new SoftFluid.Builder(FluidTextures.WATER_TEXTURE, FluidTextures.FLOWING_WATER_TEXTURE,"fish_oil")
                .fromMod("alexsmobs")
                .color(0xFFE89C)
                .translationKey("item.alexsmobs.fish_oil")
                .food("alexsmobs:fish_oil")
                .bottle("alexsmobs:fish_oil")));
        addOpt(custom,makeSF(new SoftFluid.Builder(FluidTextures.POTION_TEXTURE, FluidTextures.POTION_TEXTURE_FLOW,"poison")
                .fromMod("alexsmobs")
                .color(0x8AEB67)
                .translationKey("item.alexsmobs:poison")
                .bottle("alexsmobs:poison_bottle")));
        addOpt(custom,makeSF(new SoftFluid.Builder(FluidTextures.SOUP_TEXTURE, FluidTextures.POTION_TEXTURE_FLOW,"sopa_de_macaco")
                .fromMod("alexsmobs")
                .color(0xB6C184)
                .food("alexsmobs:sopa_de_macaco")
                .bowl("alexsmobs:sopa_de_macaco")));
        addOpt(custom,makeSF(new SoftFluid.Builder(FluidTextures.SOUP_TEXTURE, FluidTextures.POTION_TEXTURE_FLOW,"baked_cod_stew")
                .fromMod("farmersdelight")
                .color(0xECCD96)
                .stew("farmersdelight:baked_cod_stew")));
        addOpt(custom,makeSF(new SoftFluid.Builder(FluidTextures.SOUP_TEXTURE, FluidTextures.POTION_TEXTURE_FLOW,"beef_stew")
                .fromMod("farmersdelight")
                .color(0x713F2D)
                .food("farmersdelight:beef_stew")
                .stew("farmersdelight:beef_stew")));
        addOpt(custom,makeSF(new SoftFluid.Builder(FluidTextures.SOUP_TEXTURE, FluidTextures.POTION_TEXTURE_FLOW,"chicken_soup")
                .fromMod("farmersdelight")
                .color(0xDEA766)
                .food("farmersdelight:chicken_soup")
                .bowl("farmersdelight:chicken_soup")));
        addOpt(custom,makeSF(new SoftFluid.Builder(FluidTextures.SOUP_TEXTURE, FluidTextures.POTION_TEXTURE_FLOW,"fish_stew")
                .fromMod("farmersdelight")
                .color(0xB34420)
                .food("farmersdelight:fish_stew")
                .bowl("farmersdelight:fish_stew")));
        //TODO: add honey and milk flowing textures
        addOpt(custom,makeSF(new SoftFluid.Builder(FluidTextures.MILK_TEXTURE, FluidTextures.MILK_TEXTURE,"hot_cocoa")
                .fromMod("farmersdelight")
                //.textureOverride("create:chocolate",0xe98352)
                .color(0x8F563B)
                .food("farmersdelight:hot_cocoa")
                .translationKey("item.farmersdelight.hot_cocoa")
                .bottle("farmersdelight:hot_cocoa")));
        addOpt(custom,makeSF(new SoftFluid.Builder(FluidTextures.SOUP_TEXTURE, FluidTextures.POTION_TEXTURE_FLOW,"pumpkin_soup")
                .fromMod("farmersdelight")
                .color(0xE38A1D)
                .food("farmersdelight:pumpkin_soup")
                .bowl("farmersdelight:pumpkin_soup")));
        addOpt(custom,makeSF(new SoftFluid.Builder(FluidTextures.SOUP_TEXTURE, FluidTextures.POTION_TEXTURE_FLOW,"tomato_sauce")
                .fromMod("farmersdelight")
                .color(0xC0341F)
                .food("farmersdelight:tomato_sauce")
                .bowl("farmersdelight:tomato_sauce")));
        addOpt(custom,makeSF(new SoftFluid.Builder(FluidTextures.HONEY_TEXTURE, FluidTextures.POTION_TEXTURE_FLOW,"syrup")
                .fromMod("autumnity")
                .textureOverride("create:honey")
                .color(0x8e3f26)
                .addEqFluid("thermal:syrup")
                .food("autumnity:syrup_bottle")
                .translationKey("item.autumnity.syrup")
                .bottle("autumnity:syrup_bottle")));
        addOpt(custom,makeSF(new SoftFluid.Builder(FluidTextures.SOUP_TEXTURE, FluidTextures.POTION_TEXTURE_FLOW,"fire_stew")
                .fromMod("iceandfire")
                .color(0xEB5D10)
                .food("iceandfire:fire_stew")
                .bowl("iceandfire:fire_stew")));
        addOpt(custom,makeSF(new SoftFluid.Builder(FluidTextures.SOUP_TEXTURE, FluidTextures.POTION_TEXTURE_FLOW,"frost_stew")
                .fromMod("iceandfire")
                .color(0x81F2F9)
                .food("iceandfire:frost_stew")
                .bowl("iceandfire:frost_stew")));
        addOpt(custom,makeSF(new SoftFluid.Builder(FluidTextures.SOUP_TEXTURE, FluidTextures.POTION_TEXTURE_FLOW,"lightning_stew")
                .fromMod("iceandfire")
                .color(0x7552C2)
                .food("iceandfire:lightning_stew")
                .bowl("iceandfire:lightning_stew")));
        addOpt(custom,makeSF(new SoftFluid.Builder(FluidTextures.POTION_TEXTURE, FluidTextures.POTION_TEXTURE_FLOW,"fire_dragon_blood")
                .fromMod("iceandfire")
                .color(0xEB5D10)
                .food("iceandfire:fire_dragon_blood")
                .translationKey("item.iceandfire.fire_dragon_blood")
                .bottle("iceandfire:fire_dragon_blood")));
        addOpt(custom,makeSF(new SoftFluid.Builder(FluidTextures.POTION_TEXTURE, FluidTextures.POTION_TEXTURE_FLOW,"lightning_dragon_blood")
                .fromMod("iceandfire")
                .color(0xA700FC)
                .food("iceandfire:lightning_dragon_blood")
                .translationKey("item.iceandfire.lightning_dragon_blood")
                .bottle("iceandfire:lightning_dragon_blood")));
        addOpt(custom,makeSF(new SoftFluid.Builder(FluidTextures.POTION_TEXTURE, FluidTextures.POTION_TEXTURE_FLOW,"ice_dragon_blood")
                .fromMod("iceandfire")
                .color(0x1BCFFC)
                .food("iceandfire:ice_dragon_blood")
                .translationKey("item.iceandfire.ice_dragon_blood")
                .bottle("iceandfire:ice_dragon_blood")));
        addOpt(custom,makeSF(new SoftFluid.Builder(FluidTextures.SOUP_TEXTURE, FluidTextures.POTION_TEXTURE_FLOW,"vegetable_soup")
                .fromMod("farmersdelight")
                .color(0x8A7825)
                .food("farmersdelight:vegetable_soup")
                .bowl("farmersdelight:vegetable_soup")));
        addOpt(custom,makeSF(new SoftFluid.Builder(FluidTextures.MILK_TEXTURE, FluidTextures.MILK_TEXTURE,"goat_milk")
                .fromMod("betteranimalsplus")
                .translationKey("item.betteranimalsplus.goatmilk")
                .bucket("betteranimalsplus:goatmilk")));
        addOpt(custom,makeSF(new SoftFluid.Builder(FluidTextures.WATER_TEXTURE, FluidTextures.FLOWING_WATER_TEXTURE,"whiskey")
                .fromMod("simplefarming")
                .color(0xd29062)
                .translationKey("item.simplefarming.whiskey")
                .food("simplefarming:whiskey")
                .bottle("simplefarming:whiskey")));
        addOpt(custom,makeSF(new SoftFluid.Builder(FluidTextures.WATER_TEXTURE, FluidTextures.FLOWING_WATER_TEXTURE,"olive_oil")
                .fromMod("simplefarming")
                .color(0x969F1C)
                .translationKey("item.simplefarming.olive_oil")
                .food("simplefarming:olive_oil")
                .bottle("simplefarming:olive_oil")));
        addOpt(custom,makeSF(new SoftFluid.Builder(FluidTextures.WATER_TEXTURE, FluidTextures.FLOWING_WATER_TEXTURE,"vinegar")
                .fromMod("simplefarming")
                .color(0xD4D2C4)
                .translationKey("item.simplefarming.vinegar")
                .food("simplefarming:vinegar")
                .bottle("simplefarming:vinegar")));
        addOpt(custom,makeSF(new SoftFluid.Builder(FluidTextures.WATER_TEXTURE, FluidTextures.FLOWING_WATER_TEXTURE,"mead")
                .fromMod("simplefarming")
                .color(0xC39710)
                .translationKey("item.simplefarming.mead")
                .food("simplefarming:mead")
                .bottle("simplefarming:mead")));
        addOpt(custom,makeSF(new SoftFluid.Builder(FluidTextures.WATER_TEXTURE, FluidTextures.FLOWING_WATER_TEXTURE,"beer")
                .fromMod("simplefarming")
                .color(0xCB9847)
                .translationKey("item.simplefarming.beer")
                .food("simplefarming:beer")
                .bottle("simplefarming:beer")));
        addOpt(custom,makeSF(new SoftFluid.Builder(FluidTextures.WATER_TEXTURE, FluidTextures.FLOWING_WATER_TEXTURE,"cauim")
                .fromMod("simplefarming")
                .color(0xCFC273)
                .translationKey("item.simplefarming.cauim")
                .food("simplefarming:cauim")
                .bottle("simplefarming:cauim")));
        addOpt(custom,makeSF(new SoftFluid.Builder(FluidTextures.WATER_TEXTURE, FluidTextures.FLOWING_WATER_TEXTURE,"cider")
                .fromMod("simplefarming")
                .color(0xDC921E)
                .translationKey("item.simplefarming.cider")
                .food("simplefarming:cider")
                .bottle("simplefarming:cider")));
        addOpt(custom,makeSF(new SoftFluid.Builder(FluidTextures.WATER_TEXTURE, FluidTextures.FLOWING_WATER_TEXTURE,"sake")
                .fromMod("simplefarming")
                .color(0xE3D56C)
                .translationKey("item.simplefarming.sake")
                .drink("simplefarming:sake")));
        addOpt(custom,makeSF(new SoftFluid.Builder(FluidTextures.WATER_TEXTURE, FluidTextures.FLOWING_WATER_TEXTURE,"tiswin")
                .fromMod("simplefarming")
                .color(0xDC5826)
                .translationKey("item.simplefarming.tiswin")
                .drink("simplefarming:tiswin")));
        addOpt(custom,makeSF(new SoftFluid.Builder(FluidTextures.WATER_TEXTURE, FluidTextures.FLOWING_WATER_TEXTURE,"vodka")
                .fromMod("simplefarming")
                .color(0xCFDFEB)
                .translationKey("item.simplefarming.vodka")
                .drink("simplefarming:vodka")));
        addOpt(custom,makeSF(new SoftFluid.Builder(FluidTextures.WATER_TEXTURE, FluidTextures.FLOWING_WATER_TEXTURE,"wine")
                .fromMod("simplefarming")
                .color(0x961D49)
                .translationKey("item.simplefarming.wine")
                .drink("simplefarming:wine")));
        addOpt(custom,makeSF(new SoftFluid.Builder(FluidTextures.HONEY_TEXTURE, FluidTextures.HONEY_TEXTURE,"jam")
                .fromMod("simplefarming")
                .color(0x970C1F)
                .translationKey("item.simplefarming.jam")
                .food("simplefarming:jam")
                .bottle("simplefarming:jam")));
        addOpt(custom,makeSF(new SoftFluid.Builder(FluidTextures.POTION_TEXTURE, FluidTextures.POTION_TEXTURE_FLOW,"umbrella_cluster_juice")
                .fromMod("betterendforge")
                .color(0xBE53F6)
                .translationKey("item.betterendforge.umbrella_cluster_juice")
                .drink("betterendforge:umbrella_cluster_juice")));
        addOpt(custom,makeSF(new SoftFluid.Builder(FluidTextures.POTION_TEXTURE, FluidTextures.POTION_TEXTURE_FLOW,"soap")
                .fromMod("fluffy_farmer")
                .color(0xdb9eff)
                .translationKey("item.fluffy_farmer.soap")
                .bottle("fluffy_farmer:bottle_with_soap_bubbles")));
        addOpt(custom,makeSF(new SoftFluid.Builder(FluidTextures.POTION_TEXTURE, FluidTextures.POTION_TEXTURE_FLOW,"soap")
                .fromMod("betteranimalsplus")
                .color(0x60A8E0)
                .translationKey("item.betteranimalsplus.horseshoe_crab_blood")
                .drink("betteranimalsplus:horseshoe_crab_blood")));
        addOpt(custom,makeSF(new SoftFluid.Builder("tconstruct:sky_congealed_slime",
                "tconstruct:sky_congealed_slime","sky_slime")
                .fromMod("tconstruct")
                .emptyHandContainerItem("tconstruct:sky_slime_ball",1)
                .food("tconstruct:sky_slime_ball")));
        addOpt(custom,makeSF(new SoftFluid.Builder("tconstruct:ichor_congealed_slime",
                "tconstruct:ichor_congealed_slime","ichor_slime")
                .fromMod("tconstruct")
                .emptyHandContainerItem("tconstruct:ichor_slime_ball",1)
                .food("tconstruct:ichor_slime_ball")));
        addOpt(custom,makeSF(new SoftFluid.Builder("tconstruct:blood_congealed_slime",
                "tconstruct:blood_congealed_slime","blood_slime")
                .fromMod("tconstruct")
                .emptyHandContainerItem("tconstruct:blood_slime_ball",1)
                .food("tconstruct:blood_slime_ball")));
        addOpt(custom,makeSF(new SoftFluid.Builder("tconstruct:ender_congealed_slime",
                "tconstruct:ender_congealed_slime","ender_slime")
                .fromMod("tconstruct")
                .emptyHandContainerItem("tconstruct:ender_slime_ball",1)
                .food("tconstruct:ender_slime_ball")));

        //"upgrade_aquatic:mulberry_jar_bottle" d3385d
        //"atmospehric:aloe_vera_bottle" 66dc46

        //inspirations dye bottles. not adding nbt mixed ones


        for (DyeColor c: DyeColor.values()){
            Item dye = ForgeRegistries.ITEMS.getValue(new ResourceLocation("minecraft:"+c.getName()+"_dye"));
            String name = "inspirations:"+c.getSerializedName()+"_dyed_bottle";
            SoftFluid s = makeSF(new SoftFluid.Builder(FluidTextures.WATER_TEXTURE, FluidTextures.FLOWING_WATER_TEXTURE,name)
                    .bottle(name)
                    .translationKey(dye.getDescriptionId())
                    .color(c.getColorValue())
                    .fromMod("inspirations")
                    .textureOverride("inspirations:potato_soup")
            );
            if(s==null)continue;
            custom.add(s);
        }

        registerUnchecked(custom.toArray(new SoftFluid[0]));
    }

}
