package net.mehvahdjukaar.selene.fluids;

import net.minecraft.fluid.FlowingFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

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
    public static final SoftFluid GHAST_TEAR;
    public static final SoftFluid MAGMA_CREAM;

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
                .translationKey(Items.MUSHROOM_STEW.getDescriptionId())
                .addEqFluid("inspirations:mushroom_stew")
                .textureOverrideF("inspirations:mushroom_stew"));
        BEETROOT_SOUP = makeSF(new SoftFluid.Builder(FluidTextures.SOUP_TEXTURE, FluidTextures.POTION_TEXTURE_FLOW,"beetroot_soup")
                .color(0xC93434)
                .stew(Items.BEETROOT_SOUP)
                .translationKey(Items.BEETROOT_SOUP.getDescriptionId())
                .addEqFluid("inspirations:beetroot_soup")
                .textureOverrideF("inspirations:beetroot_soup"));
        RABBIT_STEW = makeSF(new SoftFluid.Builder(FluidTextures.SOUP_TEXTURE, FluidTextures.POTION_TEXTURE_FLOW,"rabbit_stew")
                .color(0xFF904F)
                .stew(Items.RABBIT_STEW)
                .translationKey(Items.RABBIT_STEW.getDescriptionId())
                .addEqFluid("inspirations:rabbit_stew")
                .textureOverrideF("inspirations:rabbit_stew"));
        SUS_STEW = makeSF(new SoftFluid.Builder(FluidTextures.SOUP_TEXTURE, FluidTextures.POTION_TEXTURE_FLOW,"suspicious_stew")
                .color(0xBAE85F)
                .keepNBTFromItem("Effects")
                .stew(Items.SUSPICIOUS_STEW)
                .translationKey(Items.SUSPICIOUS_STEW.getDescriptionId())
                .textureOverrideF("inspirations:mushroom_stew"));
        //TODO: automate translation key thing
        POTION = makeSF(new SoftFluid.Builder(FluidTextures.POTION_TEXTURE, FluidTextures.POTION_TEXTURE_FLOW,"potion")
                .color(PotionUtils.getColor(Potions.EMPTY))
                .keepNBTFromItem("Potion")
                .translationKey(Items.POTION.getDescriptionId())
                .drink(Items.POTION)
                .addEqFluid("create:potion")
                .addEqFluid("immersiveengineering:potion"));
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
        GHAST_TEAR = makeSF(new SoftFluid.Builder(FluidTextures.MILK_TEXTURE, FluidTextures.POTION_TEXTURE_FLOW,"ghast_tear")
                .color(0xbff0f0)
                .emptyHandContainerItem(Items.GHAST_TEAR,1)
                .translationKey("item.minecraft.ghast_tear"));
        MAGMA_CREAM = makeSF(new SoftFluid.Builder(FluidTextures.MAGMA_TEXTURE, FluidTextures.MAGMA_TEXTURE_FLOW,"magma_cream")
                .emptyHandContainerItem(Items.MAGMA_CREAM,1)
                .color(0xfabd30)
                .setSoundsForCategory(SoundEvents.SLIME_BLOCK_PLACE,SoundEvents.SLIME_BLOCK_BREAK,Items.AIR)
                .translationKey("item.minecraft.magma_cream"));
    }

    //TODO: allow user to modify already registered fluids

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
     * Use your own namespace as id to make them always register
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
        //registers vanilla fluids
        registerUnchecked(WATER,LAVA,HONEY,MILK,MUSHROOM_STEW,MAGMA_CREAM,
                SUS_STEW,BEETROOT_SOUP,RABBIT_STEW,POTION,DRAGON_BREATH,XP,SLIME,GHAST_TEAR);

        //registers all forge fluids
        convertAndRegisterAllForgeFluids();
    }

}
