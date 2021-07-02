package net.mehvahdjukaar.selene.fluids;

import net.mehvahdjukaar.selene.fluids.client.FluidParticleColors;
import net.mehvahdjukaar.selene.util.Utils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.potion.*;
import net.minecraft.stats.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


/**
 * instance this fluid tank in your tile entity
 */
public class SoftFluidHolder {
    public static final int BOTTLE_COUNT = 1;
    public static final int BOWL_COUNT = 2;
    public static final int BUCKET_COUNT = 4;

    //count in bottles
    private int count = 0;
    private final int capacity;
    private CompoundNBT nbt = new CompoundNBT();
    private SoftFluid fluid = SoftFluidRegistry.EMPTY;
    //special tint color. Used for dynamic tint fluids like water and potions
    private int specialColor = 0;
    private boolean needColorRefresh = true;

    public SoftFluidHolder(int capacity) {
        this.capacity = capacity;
    }

    /**
     * call this method from your block when player interacts. tries to fill or empty current held item in tank
     * @param player player
     * @param hand hand
     * @return interaction successful
     */
    public boolean interactWithPlayer(PlayerEntity player, Hand hand, @Nullable World world, @Nullable BlockPos pos) {
        ItemStack handStack = player.getItemInHand(hand);

        ItemStack returnStack = this.interactWithItem(handStack, world, pos);
        //for items that have no bottle
        if(returnStack!=null){
            Utils.swapItem(player, hand, returnStack);

            if(!handStack.isEmpty()) player.awardStat(Stats.ITEM_USED.get(handStack.getItem()));
            return true;
        }
        return false;
    }
    /**
     * makes current item interact with fluid tank. returns empty stack if
     * @param stack ItemStack to be interacted with
     * @param world world. null if no sound is to be played
     * @param pos position. null if no sound is to be played
     * @return resulting ItemStack: empty for empty hand return, null if it failed
     */
    @Nullable
    public ItemStack interactWithItem(ItemStack stack, @Nullable World world, @Nullable BlockPos pos) {

        ItemStack returnStack;
        //try filling
        returnStack = this.tryFillingItem(stack.getItem(), world, pos);
        if(returnStack!=null)return returnStack;
        //try emptying
        returnStack = this.tryDrainItem(stack, world, pos);

        return returnStack;
    }

    //handles special nbt items such as potions or soups
    private void applyNBTtoItemStack(ItemStack stack) {
        if (this.nbt != null && !this.nbt.isEmpty())
            stack.setTag(this.nbt.copy());
    }

    //returns null to handle the item with previous behavior (water bottles)
    @Nullable
    private ItemStack handleStewsAndPotions(ItemStack stack) {
        Item item = stack.getItem();
        CompoundNBT com = stack.getTag();
        if (com != null) {
            if (item instanceof PotionItem) {
                Potion potion = PotionUtils.getPotion(stack);
                if(potion == Potions.WATER){
                    if (tryAddingFluid(SoftFluidRegistry.WATER, BOTTLE_COUNT)) {
                        return getEmptyBottle();
                    }
                }
                else if (potion != Potions.EMPTY && com.contains("Potion")) {
                    CompoundNBT newCom = new CompoundNBT();
                    newCom.putString("Potion", com.getString("Potion"));
                    if (tryAddingFluid(SoftFluidRegistry.POTION, BOTTLE_COUNT, newCom)) {
                        return getEmptyBottle();
                    }
                }
                //every other potion is water bottle. handles by normal code
                return null;
            }
            else if (item instanceof SuspiciousStewItem) {
                if (com.contains("Effects", 9)) {
                    ListNBT listnbt = com.getList("Effects", 10);
                    CompoundNBT newCom = new CompoundNBT();
                    newCom.put("Effects", listnbt);
                    if (tryAddingFluid(SoftFluidRegistry.SUS_STEW, BOWL_COUNT, newCom)) {
                        return getEmptyBowl();
                    }
                }
            }
        }
        return null;
    }

    /**
     * tries pouring the content of provided item in the tank
     * also plays sound
     * @return empty container item, null if it failed
     */
    @Nullable
    public ItemStack tryDrainItem(ItemStack filledContainerStack, @Nullable World world, @Nullable BlockPos pos) {
        //special case to empty nbt items like potions and stews
        ItemStack specialCase = this.handleStewsAndPotions(filledContainerStack);
        if (specialCase != null) {
            if (world != null && !world.isClientSide && pos != null && !specialCase.isEmpty())
                world.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundCategory.BLOCKS, 1, 1);
            return specialCase;
        }
        Item filledContainer = filledContainerStack.getItem();
        //set new fluid if empty
        if (this.isEmpty()) {
            SoftFluid s = SoftFluidRegistry.fromItem(filledContainer);
            //case to convert water
            if (PotionUtils.getPotion(filledContainerStack) == Potions.WATER) s = SoftFluidRegistry.WATER;

            if (s.isEmpty()) return null;
            this.setFluid(s);
        }
        Item empty = fluid.tryGettingEmptyItem(filledContainer);
        SoftFluid.FilledContainerCategory category = fluid.tryGettingFilledItems(empty);
        if(category != null && empty != null) {
            int amount = category.getAmount();
            if (this.canAdd(amount)) {

                this.grow(amount);

                SoundEvent sound = category.getEmptySound();
                if(sound!=null && world!=null && pos != null) world.playSound(null, pos, sound, SoundCategory.BLOCKS, 1, 1);

                return new ItemStack(empty);
            }
        }
        return null;
    }

    /**
     * tries removing said amount of fluid and returns filled item
     * also plays sound
     * @return filled bottle item. null if it failed
     */
    @Nullable
    public ItemStack tryFillingItem(Item emptyContainer, @Nullable World world, @Nullable  BlockPos pos) {
        SoftFluid.FilledContainerCategory category = fluid.tryGettingFilledItems(emptyContainer);
        if(category != null) {
            int amount = category.getAmount();
            if (this.canRemove(amount)) {
                ItemStack stack = new ItemStack(category.getFirstFilled());
                //converts water bottles into potions
                if (emptyContainer == Items.GLASS_BOTTLE && fluid == SoftFluidRegistry.WATER)
                    stack = PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER);

                this.applyNBTtoItemStack(stack);
                this.shrink(amount);

                SoundEvent sound = category.getEmptySound();
                if(sound!=null && world!=null && pos != null) world.playSound(null, pos, sound, SoundCategory.BLOCKS, 1, 1);

                return stack;
            }
        }
        return null;
    }
    /**
     * tries removing bottle amount and returns filled bottle
     * @return filled bottle item. null if it failed
     */
    @Nullable
    public ItemStack tryFillingBottle(World world, BlockPos pos) {
        return tryFillingItem(Items.GLASS_BOTTLE, world, pos);
    }
    /**
     * tries removing bucket amount and returns filled bucket
     * @return filled bucket item. null if it failed
     */
    @Nullable
    public ItemStack tryFillingBucket(World world, BlockPos pos) {
        return tryFillingItem(Items.BUCKET, world, pos);
    }
    /**
     * tries removing bowl amount and returns filled bowl
     * @return filled bowl item. null if it failed
     */
    @Nullable
    public ItemStack tryFillingBowl(World world, BlockPos pos) {
        return tryFillingItem(Items.BOWL, world, pos);
    }

    /**
     * checks if current tank holds equivalent fluid as provided forge fluids stack & nbt
     * @param fluidStack forge fluid stack
     * @param com fluid nbt
     * @return is same
     */
    public boolean isSameFluidAs(FluidStack fluidStack, CompoundNBT com) {
        return this.fluid.isEquivalent(fluidStack.getFluid()) && com.equals(this.nbt);
    }
    /**
     * checks if current tank holds equivalent fluid as provided forge fluids stack
     * @param fluidStack forge fluid stack
     * @return is same
     */
    public boolean isSameFluidAs(FluidStack fluidStack) {
        return this.fluid.isEquivalent(fluidStack.getFluid()) && fluidStack.getOrCreateTag().equals(this.nbt);
    }
    /**
     * checks if current tank holds equivalent fluid as provided soft fluid
     * @param other soft fluid
     * @return is same
     */
    public boolean isSameFluidAs(SoftFluid other) {
        return isSameFluidAs(other, new CompoundNBT());
    }
    /**
     * checks if current tank holds equivalent fluid as provided soft fluid
     * @param other soft fluid
     * @param com fluid nbt
     * @return is same
     */
    public boolean isSameFluidAs(SoftFluid other, CompoundNBT com) {
        return this.fluid.equals(other) && this.nbt.equals(com);
    }
    /**
     * try adding provided forge fluid to the tank
     * @param fluidStack forge fluid stack
     * @return success
     */
    public boolean tryAddingFluid(FluidStack fluidStack) {
        int count = fluidStack.getAmount();
        SoftFluid s = SoftFluidRegistry.fromForgeFluid(fluid.getForgeFluid());
        return tryAddingFluid(s,count,fluidStack.getTag());
    }
    /**
     * try adding provided soft fluid to the tank
     * @param s soft fluid to add
     * @param count count to add
     * @param com fluid nbt
     * @return success
     */
    public boolean tryAddingFluid(SoftFluid s, int count, CompoundNBT com) {
        if (this.canAdd(count)) {
            if (this.isEmpty()) {
                this.setFluid(s, com);
                this.setCount(count);
                return true;
            } else if (this.isSameFluidAs(s,com)) {
                this.grow(count);
                return true;
            }
        }
        return false;
    }
    /**
     * try adding provided soft fluid to the tank
     * @param s soft fluid to add
     * @param count count to add
     * @return success
     */
    public boolean tryAddingFluid(SoftFluid s, int count) {
        return tryAddingFluid(s,count,new CompoundNBT());
    }
    /**
     * try adding 1 bottle of provided soft fluid to the tank
     * @param s soft fluid to add
     * @return success
     */
    public boolean tryAddingFluid(SoftFluid s) {
        return this.tryAddingFluid(s,1);
    }

    public boolean tryTransferFluid(SoftFluidHolder other) {
        return this.tryTransferFluid(other, BOTTLE_COUNT);
    }

    //transfers between two fluid holders
    public boolean tryTransferFluid(SoftFluidHolder other, int amount) {
        if (other.canAdd(amount) && this.canRemove(amount)) {
            if (other.isEmpty()) {
                other.setFluid(this.getFluid(), this.getNbt());
                this.shrink(amount);
                other.grow(amount);
                return true;
            } else if (this.isSameFluidAs(other.getFluid(), other.nbt)) {
                this.shrink(amount);
                other.grow(amount);
                return true;
            }
        }
        return false;
    }

    /**
     * empties n bottle of content into said forge fluid tank
     * @param fluidDestination forge fluid tank handler
     * @param bottles number of bottles to empty (1blt = 250mb)
     * @return success
     */
    public boolean tryTransferToFluidTank(IFluidHandler fluidDestination, int bottles) {
        if (!this.canRemove(bottles)) return false;
        int milliBuckets = bottles * 250;
        FluidStack stack = this.toEquivalentForgeFluid(milliBuckets);
        if (!stack.isEmpty()) {
            int fillableAmount = fluidDestination.fill(stack, IFluidHandler.FluidAction.SIMULATE);
            if (fillableAmount == milliBuckets) {
                fluidDestination.fill(stack, IFluidHandler.FluidAction.EXECUTE);
                this.shrink(bottles);
                return true;
            }
        }
        return false;
    }

    public boolean tryTransferToFluidTank(IFluidHandler fluidDestination) {
        return this.tryTransferToFluidTank(fluidDestination, BOTTLE_COUNT);
    }

    //drains said fluid tank of 250mb (1 bottle) of fluid
    public boolean drainFluidTank(IFluidHandler fluidSource, int bottles) {
        if (!this.canAdd(bottles)) return false;
        int milliBuckets = bottles * 250;
        FluidStack drainable = fluidSource.drain(milliBuckets, IFluidHandler.FluidAction.SIMULATE);
        if (!drainable.isEmpty() && drainable.getAmount() == milliBuckets) {
            boolean transfer = false;
            CompoundNBT fsTag = drainable.getOrCreateTag();
            if (this.fluid.isEmpty()) {
                this.setFluid(SoftFluidRegistry.fromForgeFluid(drainable.getFluid()), drainable.getOrCreateTag());
                transfer = true;
            } else if (this.isSameFluidAs(drainable, fsTag)) {
                transfer = true;
            }
            if (transfer) {
                fluidSource.drain(milliBuckets, IFluidHandler.FluidAction.EXECUTE);
                this.grow(bottles);
                return true;
            }
        }
        return false;
    }

    public boolean drainFluidTank(IFluidHandler fluidSource) {
        return this.drainFluidTank(fluidSource, BOTTLE_COUNT);
    }

    /**
     * gets the equivalent forge fluid without draining the tank. returned stack might be empty
     * @param mb forge minecraft buckets
     * @return forge fluid stacks
     */
    public FluidStack toEquivalentForgeFluid(int mb) {
        FluidStack stack = new FluidStack(this.fluid.getForgeFluid(), mb);
        this.applyNBTtoFluidStack(stack);
        return stack;
    }

    private void applyNBTtoFluidStack(FluidStack fluidStack) {
        if (this.nbt != null && !this.nbt.isEmpty() && !fluidStack.isEmpty())
            fluidStack.setTag(this.nbt.copy());
    }
    /**
     * can I remove n bottles of fluid
     * @param n bottles amount
     * @return can remove
     */
    public boolean canRemove(int n) {
        return this.count >= n && !this.isEmpty();
    }
    /**
     * can I add n bottles of fluid
     * @param n bottles amount
     * @return can add
     */
    public boolean canAdd(int n) {
        return this.count + n <= this.capacity;
    }
    /**
     * can provided soft fluid be added to tank
     * @param s soft fluid to add
     * @param count bottles amount
     * @return can add
     */
    public boolean canAddSoftFluid(SoftFluid s, int count){
        return canAddSoftFluid(s,count,new CompoundNBT());
    }
    /**
     * can provided soft fluid be added to tank
     * @param s soft fluid to add
     * @param count bottles amount
     * @param nbt soft fluid nbt
     * @return can add
     */
    public boolean canAddSoftFluid(SoftFluid s, int count, CompoundNBT nbt){
        return this.canAdd(count) && this.isSameFluidAs(s,nbt);
    }

    public boolean isFull() {
        return this.count == this.capacity;
    }

    public boolean isEmpty() {
        //count 0 should always = to fluid.empty
        return this.fluid.isEmpty()||this.count<=0;
    }
    /**
     * grows contained fluid by at most inc bottles. doesn't need checking
     * @param inc maximum increment
     */
    public void lossyAdd(int inc) {
        this.count = Math.min(this.capacity, this.count + inc);
    }

    /**
     * unchecked sets the tank fluid count
     * @param count bottles count
     */
    public void setCount(int count) {
        this.count = count;
    }
    /**
     * fills out tank to the maximum capacity
     */
    public void fillCount() {
        this.setCount(capacity);
    }
    /**
     * unchecked grows the tank by inc bottles. Check with canAdd()
     * @param inc bottles increment
     */
    public void grow(int inc) {
        this.setCount(this.count + inc);
    }
    /**
     * unchecked shrinks the tank by inc bottles
     * @param inc bottles increment
     */
    public void shrink(int inc) {
        this.grow(-inc);
        if(this.count==0)this.fluid = SoftFluidRegistry.EMPTY;
    }
    /**
     * gets liquid height for renderer
     * @param maxHeight maximum height in blocks
     * @return fluid height
     */
    public float getHeight(float maxHeight) {
        return maxHeight * (float) this.count / (float) this.capacity;
    }
    /**
     * @return comparator block redstone power
     */
    public int getComparatorOutput() {
        float f = this.count / (float) this.capacity;
        return MathHelper.floor(f * 14.0F) + 1;
    }

    public int getCount() {
        return count;
    }

    @Nonnull
    public SoftFluid getFluid() {
        return fluid;
    }

    public CompoundNBT getNbt() {
        return nbt;
    }

    public void setNbt(CompoundNBT nbt) {
        this.nbt = nbt;
    }
    /**
     * resets & clears the tank
     */
    public void clear() {
        this.fluid = SoftFluidRegistry.EMPTY;
        this.setCount(0);
        this.nbt = new CompoundNBT();
        this.specialColor = 0;
    }

    /**
     * copies the content of a fluid tank into this
     * @param other
     */
    public void copy(SoftFluidHolder other) {
        this.setFluid(other.getFluid(), other.getNbt());
        this.setCount(Math.min(this.capacity, other.getCount()));
    }
    /**
     * fills to max capacity with provided forge fluid
     * @param fluidStack forge fluid
     */
    public void fill(FluidStack fluidStack) {
        this.setFluid(fluidStack);
        this.fillCount();
    }
    /**
     * fills to max capacity with provided soft fluid
     * @param fluid forge fluid
     */
    public void fill(SoftFluid fluid) {
        this.fill(fluid, new CompoundNBT());
    }
    /**
     * fills to max capacity with provided soft fluid
     * @param fluid soft fluid
     * @param nbt soft fluid nbt
     */
    public void fill(SoftFluid fluid, CompoundNBT nbt) {
        this.setFluid(fluid, nbt);
        this.fillCount();
    }
    /**
     * sets current fluid to provided forge fluid equivalent
     * @param fluidStack forge fluid
     */
    public void setFluid(FluidStack fluidStack) {
        SoftFluid s = SoftFluidRegistry.fromForgeFluid(fluidStack.getFluid());
        this.setFluid(s, fluidStack.getOrCreateTag());
    }
    /**
     * sets current fluid to provided soft fluid equivalent
     * @param fluid soft fluid
     */
    public void setFluid(SoftFluid fluid) {
        this.setFluid(fluid, new CompoundNBT());
    }

    //called when it goes from empty to full
    public void setFluid(SoftFluid fluid, CompoundNBT nbt) {
        this.fluid = fluid;
        this.nbt = nbt;
        this.specialColor = 0;
        if(this.fluid.isEmpty())this.setCount(0);
        this.needColorRefresh = true;
    }

    /**
     * @return tint color to be applied on the fuid texture
     */
    public int getTintColor(@Nullable IWorldReader world, @Nullable BlockPos pos) {
        if(this.needColorRefresh){
            this.refreshSpecialColor(world,pos);
            this.needColorRefresh = false;
        }
        if (this.specialColor != 0) return this.specialColor;
        return this.fluid.getTintColor();
    }

    /**
     * @return tint color to be used on particle. Differs from getTintColor since it returns an average color extrapolated from their fluid textures
     */
    public int getParticleColor(@Nullable IWorldReader world, @Nullable BlockPos pos) {
        if (this.isEmpty()) return -1;
        int tintColor = this.getTintColor(world, pos);
        //only gets color from texture for colored textures fluids(ones that don't have tint). Might remove if
        if (tintColor == -1) return FluidParticleColors.get(this.fluid.getID());
        return tintColor;
    }

    private void refreshSpecialColor(@Nullable IWorldReader world, @Nullable BlockPos pos){

        if (fluid == SoftFluidRegistry.POTION) {
            this.specialColor = PotionUtils.getColor(PotionUtils.getPotion(this.nbt));
        }
        else{
            Fluid f = this.getFluid().getForgeFluid();
            if(f != Fluids.EMPTY){
                FluidAttributes att = f.getAttributes();
                //world accessor
                int w = -1;
                if(world != null && pos != null) w = att.getColor(world,pos);
                //stack accessor
                if(w==-1) w = att.getColor(this.toEquivalentForgeFluid(1));
                if(w!=-1) this.specialColor = w;
            }
        }

    }
    /**
     * @return true if contained fluid has associated food
     */
    public boolean isFood() {
        return this.fluid.isFood();
    }
    /**
     * returns associated food items
     * @return food
     */
    public ItemStack getFood() {
        ItemStack stack = new ItemStack(this.fluid.getFoodItem());
        this.applyNBTtoItemStack(stack);
        return stack;
    }
    /**
     * call from tile entity. loads tank from nbt
     * @param compound nbt
     */
    public void load(CompoundNBT compound) {
        if (compound.contains("FluidHolder")) {
            CompoundNBT cmp = compound.getCompound("FluidHolder");
            this.count = cmp.getInt("Count");
            this.nbt = cmp.getCompound("NBT");
            String id = cmp.getString("Fluid");
            SoftFluid sf = SoftFluidRegistry.get(id);
            //supplementaries backwards compat
            if(sf.isEmpty()){
                sf = SoftFluidRegistry.get(id.replace("supplementaries","minecraft"));
                if(sf.isEmpty()){
                    sf = SoftFluidRegistry.get(id.replace("minecraft","supplementaries"));
                }
            }

            this.setFluid(sf, this.nbt);
        }
    }
    /**
     * call from tile entity. saves to nbt
     * @param compound nbt
     * @return nbt
     */
    public CompoundNBT save(CompoundNBT compound) {
        CompoundNBT cmp = new CompoundNBT();
        cmp.putInt("Count", this.count);
        cmp.putString("Fluid", this.fluid.getID());
        //for item render. needed for potion colors
        cmp.putInt("CachedColor", this.getTintColor(null, null));
        if (!this.nbt.isEmpty()) cmp.put("NBT", this.nbt);
        compound.put("FluidHolder", cmp);

        return compound;
    }
    /**
     * makes player drink 1 bottle and removes it from the tank
     * @param player player
     * @param world world
     * @param hand hand
     * @return success
     */
    public boolean tryDrinkUpFluid(PlayerEntity player, World world, Hand hand) {
        if (this.isEmpty()) return false;
        ItemStack stack = this.getFood();
        Item item = stack.getItem();

        //case for xp
        if(this.fluid == SoftFluidRegistry.XP){
            player.giveExperiencePoints(Utils.getXPinaBottle(1,world.random));
            if (world.isClientSide) return true;
            this.shrink(1);

            player.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1, 1);
            return true;
        }
        //food
        else if(this.isFood()) {

            Food food = item.getFoodProperties();
            int div = this.fluid.getFoodDivider();
            //single items are handled by items themselves
            if (div == 1) {
                stack.getItem().finishUsingItem(stack.copy(), world, player);
                if (world.isClientSide) return true;
                this.shrink(1);
                if (food == null || stack.getItem().isEdible()) {
                    player.playNotifySound(item.getDrinkingSound(), SoundCategory.PLAYERS, 1, 1);
                }
                //player already plays sound
                return true;
            }

            boolean success = false;
            //stew & bucket case
            if (food != null && player.canEat(false)) {

                if (item instanceof SuspiciousStewItem) susStewBehavior(player, stack, div);

                if (world.isClientSide) return true;
                player.getFoodData().eat(food.getNutrition() / div, food.getSaturationModifier() / (float) div);

                success = true;
            }
            else if (item instanceof MilkBucketItem) {
                if (world.isClientSide) return true;
                milkBottleBehavior(player, stack);
                success = true;
            }
            if (success) {
                this.shrink(1);
                player.playNotifySound(item.getDrinkingSound(), SoundCategory.PLAYERS, 1, 1);
                return true;
            }
        }
        return false;
    }

    //vanilla fluids special behaviors

    //stew code
    private static void susStewBehavior(PlayerEntity player, ItemStack stack, int div) {
        CompoundNBT compoundnbt = stack.getTag();
        if (compoundnbt != null && compoundnbt.contains("Effects", 9)) {
            ListNBT listnbt = compoundnbt.getList("Effects", 10);
            for (int i = 0; i < listnbt.size(); ++i) {
                int j = 160;
                CompoundNBT compoundnbt1 = listnbt.getCompound(i);
                if (compoundnbt1.contains("EffectDuration", 3))
                    j = compoundnbt1.getInt("EffectDuration") / div;
                Effect effect = Effect.byId(compoundnbt1.getByte("EffectId"));
                if (effect != null) {
                    player.addEffect(new EffectInstance(effect, j));
                }
            }
        }
    }

    //removes just 1 effect
    private static boolean milkBottleBehavior(PlayerEntity player, ItemStack stack) {
        for (EffectInstance effect : player.getActiveEffectsMap().values()) {
            if (effect.isCurativeItem(stack)) {
                player.removeEffect(effect.getEffect());
                return true;
            }
        }
        return false;
    }

    //util functions
    public static int getLiquidCountFromItem(Item i) {
        if (i == Items.GLASS_BOTTLE) {
            return BOTTLE_COUNT;
        } else if (i == Items.BOWL) {
            return BOWL_COUNT;
        } else if (i == Items.BUCKET) {
            return BUCKET_COUNT;
        }
        return 0;
    }
    public static ItemStack getEmptyBottle(){
        return new ItemStack(Items.GLASS_BOTTLE);
    }
    public static ItemStack getEmptyBucket(){
        return new ItemStack(Items.BUCKET);
    }
    public static ItemStack getEmptyBowl(){
        return new ItemStack(Items.BOWL);
    }

}
