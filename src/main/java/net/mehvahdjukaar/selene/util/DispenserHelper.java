package net.mehvahdjukaar.selene.util;


import net.mehvahdjukaar.selene.fluids.ISoftFluidHolder;
import net.mehvahdjukaar.selene.fluids.SoftFluid;
import net.mehvahdjukaar.selene.fluids.SoftFluidHolder;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.BlockSource;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.Map;


import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.ItemLike;

public class DispenserHelper {

    public static void registerCustomBehavior(AdditionalDispenserBehavior behavior){
        DispenserBlock.registerBehavior(behavior.item, behavior);
    }

    //default spawn egg behavior
    public static void registerSpawnEggBehavior(ItemLike egg){
        DispenserBlock.registerBehavior(egg, SPAWN_EGG_BEHAVIOR);
    }

    //block placement behavior
    public static void registerPlaceBlockBehavior(ItemLike block){
        DispenserBlock.registerBehavior(block, PLACE_BLOCK_BEHAVIOR);
    }

    public static void registerFluidBehavior(SoftFluid f){
        Map<Item, SoftFluid.FilledContainerCategory> map = f.getFilledContainersMap();
        for(Item empty : map.keySet()){
            if(empty!=Items.AIR)registerCustomBehavior(new FillFluidHolderBehavior(empty));
            for(Item full : map.get(empty).getItems()){
                if(full!=Items.AIR)registerCustomBehavior(new FillFluidHolderBehavior(full));
            }
        }
    }

    /**
     * implement this to add your own custom behaviors
     */
    public abstract static class AdditionalDispenserBehavior implements DispenseItemBehavior {

        private final DispenseItemBehavior fallback;

        private final Item item;

        protected AdditionalDispenserBehavior(Item item) {
            this.item = item;
            fallback = DispenserBlock.DISPENSER_REGISTRY.get(item);
        }

        @Override
        public final ItemStack dispense(BlockSource source, ItemStack stack) {
            //this.setSuccessful(false);
            try{
                InteractionResultHolder<ItemStack> result = this.customBehavior(source,stack);
                InteractionResult type = result.getResult();
                if (type!=InteractionResult.PASS){
                    boolean success = type.consumesAction();
                    this.playSound(source,success);
                    this.playAnimation(source, source.getBlockState().getValue(DispenserBlock.FACING));
                    if(success) {
                        ItemStack resultStack = result.getObject();
                        if (resultStack.getItem() == stack.getItem()) return resultStack;
                        return fillItemInDispenser(source, stack, result.getObject());
                    }
                }
            }
            catch (Exception ignored) {}
            return fallback.dispense(source, stack);
        }

        /**
         * custom dispenser behavior that you want to implement
         * @param source dispenser block
         * @param stack stack to dispense
         * @return return ActionResult.SUCCESS / CONSUME for success, FAIL to do nothing and PASS to fallback to vanilla/previously registered behavior will be used. <br>
         * Type parameter is return item stack. If item in itemstack is different than initially provided, such itemstack will be added to dispenser, otherwise will replace existing itemstack
         */
        protected abstract InteractionResultHolder<ItemStack> customBehavior(BlockSource source, ItemStack stack);

        protected void playSound(BlockSource source, boolean success) {
            source.getLevel().levelEvent(success ? 1000 : 1001, source.getPos(), 0);
        }

        protected void playAnimation(BlockSource source, Direction direction) {
            source.getLevel().levelEvent(2000, source.getPos(), direction.get3DDataValue());
        }
    }

    public static class AddItemToInventoryBehavior extends AdditionalDispenserBehavior {

        public AddItemToInventoryBehavior(Item item) {
            super(item);
        }

        @Override
        protected InteractionResultHolder<ItemStack> customBehavior(BlockSource source, ItemStack stack) {
            //this.setSuccessful(false);
            ServerLevel world = source.getLevel();
            BlockPos blockpos = source.getPos().relative(source.getBlockState().getValue(DispenserBlock.FACING));
            BlockEntity te = world.getBlockEntity(blockpos);
            if(te instanceof WorldlyContainer){
                WorldlyContainer tile = ((WorldlyContainer)te);
                if(tile.canPlaceItem(0,stack)){
                    if(tile.isEmpty()){
                        tile.setItem(0, stack.split(1));
                    }
                    else{
                        tile.getItem(0).grow(1);
                        stack.shrink(1);
                    }
                    return InteractionResultHolder.success(stack);
                }
                return InteractionResultHolder.fail(stack);
            }
            return InteractionResultHolder.pass(stack);
        }
    }


    public static class FillFluidHolderBehavior extends AdditionalDispenserBehavior {

        public FillFluidHolderBehavior(Item item) {
            super(item);
        }

        @Override
        protected InteractionResultHolder<ItemStack> customBehavior(BlockSource source, ItemStack stack) {
            //this.setSuccessful(false);
            ServerLevel world = source.getLevel();
            BlockPos blockpos = source.getPos().relative(source.getBlockState().getValue(DispenserBlock.FACING));
            BlockEntity te = world.getBlockEntity(blockpos);
            if(te instanceof ISoftFluidHolder){
                ISoftFluidHolder tile = ((ISoftFluidHolder)te);

                ItemStack returnStack;

                if(tile.canInteractWithFluidHolder()) {

                    SoftFluidHolder tank = tile.getSoftFluidHolder();
                    if (!tank.isFull()) {
                        returnStack = tank.interactWithItem(stack, world, blockpos, false);
                        if(returnStack != null) {
                            te.setChanged();
                            return InteractionResultHolder.success(returnStack);
                        }
                    }
                }
                return InteractionResultHolder.fail(stack);
            }
            return InteractionResultHolder.pass(stack);
        }
    }

    public static class PlaceBlockDispenseBehavior extends OptionalDispenseItemBehavior {

        @Override
        public ItemStack execute(BlockSource source, ItemStack stack) {
            this.setSuccess(false);
            Item item = stack.getItem();
            if (item instanceof BlockItem) {
                Direction direction = source.getBlockState().getValue(DispenserBlock.FACING);
                BlockPos blockpos = source.getPos().relative(direction);
                Direction direction1 = source.getLevel().isEmptyBlock(blockpos.below()) ? direction : Direction.UP;
                InteractionResult result = ((BlockItem)item).place(new DirectionalPlaceContext(source.getLevel(), blockpos, direction, stack, direction1));
                this.setSuccess(result.consumesAction());
            }
            return stack;
        }
    }

    //returns full bottle to dispenser. same function that's in IDispenserItemBehavior
    private static ItemStack fillItemInDispenser(BlockSource source, ItemStack empty, ItemStack filled) {
        empty.shrink(1);
        if (empty.isEmpty()) {
            return filled.copy();
        } else {
            if (!MergeDispenserItem(source.getEntity(), filled)) {
                SHOOT_BEHAVIOR.dispense(source, filled.copy());
            }
            return empty;
        }
    }

    //add item to dispenser and merges it if there's one already
    private static boolean MergeDispenserItem(DispenserBlockEntity te, ItemStack filled) {
        NonNullList<ItemStack> stacks = te.items;
        for (int i = 0; i < te.getContainerSize(); ++i) {
            ItemStack s = stacks.get(i);
            if (s.isEmpty() || (s.getItem() == filled.getItem() && s.getMaxStackSize()>s.getCount())) {
                filled.grow(s.getCount());
                te.setItem(i, filled);
                return true;
            }
        }
        return false;
    }

    public static final DefaultDispenseItemBehavior PLACE_BLOCK_BEHAVIOR = new PlaceBlockDispenseBehavior();
    private static final DefaultDispenseItemBehavior SHOOT_BEHAVIOR = new DefaultDispenseItemBehavior();
    public static final DefaultDispenseItemBehavior SPAWN_EGG_BEHAVIOR = new DefaultDispenseItemBehavior() {
        @Override
        public ItemStack execute(BlockSource source, ItemStack stack) {
            Direction direction = source.getBlockState().getValue(DispenserBlock.FACING);
            EntityType<?> type = ((SpawnEggItem)stack.getItem()).getType(stack.getTag());
            type.spawn(source.getLevel(), stack, null, source.getPos().relative(direction), MobSpawnType.DISPENSER, direction != Direction.UP, false);
            stack.shrink(1);
            return stack;
        }
    };

}