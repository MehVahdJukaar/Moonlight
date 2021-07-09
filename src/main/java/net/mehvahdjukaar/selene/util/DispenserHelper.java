package net.mehvahdjukaar.selene.util;


import net.mehvahdjukaar.selene.fluids.ISoftFluidHolder;
import net.mehvahdjukaar.selene.fluids.SoftFluid;
import net.mehvahdjukaar.selene.fluids.SoftFluidHolder;
import net.minecraft.block.DispenserBlock;
import net.minecraft.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.dispenser.IDispenseItemBehavior;
import net.minecraft.dispenser.OptionalDispenseBehavior;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.*;
import net.minecraft.tileentity.DispenserTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

import java.util.Map;


public class DispenserHelper {

    public static void registerCustomBehavior(AdditionalDispenserBehavior behavior){
        DispenserBlock.registerBehavior(behavior.item, behavior);
    }

    public static void registerSpawnEggBehavior(IItemProvider egg){
        DispenserBlock.registerBehavior(egg, SPAWN_EGG_BEHAVIOR);
    }

    public static void registerPlaceBlockBehavior(IItemProvider block){
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
    public abstract static class AdditionalDispenserBehavior implements IDispenseItemBehavior {

        private final IDispenseItemBehavior fallback;

        private final Item item;

        protected AdditionalDispenserBehavior(Item item) {
            this.item = item;
            fallback = DispenserBlock.DISPENSER_REGISTRY.get(item);
        }

        @Override
        public final ItemStack dispense(IBlockSource source, ItemStack stack) {
            //this.setSuccessful(false);
            try{
                ActionResult<ItemStack> result = this.customBehavior(source,stack);
                ActionResultType type = result.getResult();
                if (type!=ActionResultType.PASS){
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
        protected abstract ActionResult<ItemStack> customBehavior(IBlockSource source, ItemStack stack);

        protected void playSound(IBlockSource source, boolean success) {
            source.getLevel().levelEvent(success ? 1000 : 1001, source.getPos(), 0);
        }

        protected void playAnimation(IBlockSource source, Direction direction) {
            source.getLevel().levelEvent(2000, source.getPos(), direction.get3DDataValue());
        }
    }

    public static class AddItemToInventoryBehavior extends AdditionalDispenserBehavior {

        public AddItemToInventoryBehavior(Item item) {
            super(item);
        }

        @Override
        protected ActionResult<ItemStack> customBehavior(IBlockSource source, ItemStack stack) {
            //this.setSuccessful(false);
            ServerWorld world = source.getLevel();
            BlockPos blockpos = source.getPos().relative(source.getBlockState().getValue(DispenserBlock.FACING));
            TileEntity te = world.getBlockEntity(blockpos);
            if(te instanceof ISidedInventory){
                ISidedInventory tile = ((ISidedInventory)te);
                if(tile.canPlaceItem(0,stack)){
                    if(tile.isEmpty()){
                        tile.setItem(0, stack.split(1));
                    }
                    else{
                        tile.getItem(0).grow(1);
                        stack.shrink(1);
                    }
                    return ActionResult.success(stack);
                }
                return ActionResult.fail(stack);
            }
            return ActionResult.pass(stack);
        }
    }


    public static class FillFluidHolderBehavior extends AdditionalDispenserBehavior {

        public FillFluidHolderBehavior(Item item) {
            super(item);
        }

        @Override
        protected ActionResult<ItemStack> customBehavior(IBlockSource source, ItemStack stack) {
            //this.setSuccessful(false);
            ServerWorld world = source.getLevel();
            BlockPos blockpos = source.getPos().relative(source.getBlockState().getValue(DispenserBlock.FACING));
            TileEntity te = world.getBlockEntity(blockpos);
            if(te instanceof ISoftFluidHolder){
                ISoftFluidHolder tile = ((ISoftFluidHolder)te);

                ItemStack returnStack;

                if(tile.canInteractWithFluidHolder()) {

                    SoftFluidHolder tank = tile.getSoftFluidHolder();
                    if (!tank.isFull()) {
                        returnStack = tank.interactWithItem(stack, world, blockpos, false);
                        if(returnStack != null) {
                            te.setChanged();
                            return ActionResult.success(returnStack);
                        }
                    }
                }
                return ActionResult.fail(stack);
            }
            return ActionResult.pass(stack);
        }
    }


    public static class PlaceBlockDispenseBehavior extends OptionalDispenseBehavior {

        @Override
        public ItemStack execute(IBlockSource source, ItemStack stack) {
            this.setSuccess(false);
            Item item = stack.getItem();
            if (item instanceof BlockItem) {
                Direction direction = source.getBlockState().getValue(DispenserBlock.FACING);
                BlockPos blockpos = source.getPos().relative(direction);
                Direction direction1 = source.getLevel().isEmptyBlock(blockpos.below()) ? direction : Direction.UP;
                ActionResultType result = ((BlockItem)item).place(new DirectionalPlaceContext(source.getLevel(), blockpos, direction, stack, direction1));
                this.setSuccess(result.consumesAction());
            }
            return stack;
        }
    }

    //returns full bottle to dispenser. same function that's in IDispenserItemBehavior
    private static ItemStack fillItemInDispenser(IBlockSource source, ItemStack empty, ItemStack filled) {
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
    private static boolean MergeDispenserItem(DispenserTileEntity te, ItemStack filled) {
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
        public ItemStack execute(IBlockSource source, ItemStack stack) {
            Direction direction = source.getBlockState().getValue(DispenserBlock.FACING);
            EntityType<?> type = ((SpawnEggItem)stack.getItem()).getType(stack.getTag());
            type.spawn(source.getLevel(), stack, null, source.getPos().relative(direction), SpawnReason.DISPENSER, direction != Direction.UP, false);
            stack.shrink(1);
            return stack;
        }
    };

}