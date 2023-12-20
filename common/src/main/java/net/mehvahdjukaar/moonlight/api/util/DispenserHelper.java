package net.mehvahdjukaar.moonlight.api.util;


import net.mehvahdjukaar.moonlight.core.mixins.accessor.DispenserBlockAccessor;
import net.mehvahdjukaar.moonlight.core.mixins.accessor.DispenserBlockEntityAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;

public class DispenserHelper {

    public static void registerCustomBehavior(AdditionalDispenserBehavior behavior) {
        DispenserBlock.registerBehavior(behavior.item, behavior);
    }

    //block placement behavior
    public static void registerPlaceBlockBehavior(ItemLike block) {
        DispenserBlock.registerBehavior(block, PLACE_BLOCK_BEHAVIOR);
    }

    /**
     * implement this to add your own custom behaviors
     */
    public abstract static class AdditionalDispenserBehavior implements DispenseItemBehavior {

        private final DispenseItemBehavior fallback;

        private final Item item;

        protected AdditionalDispenserBehavior(Item item) {
            this.item = item;
            fallback = DispenserBlockAccessor.getDispenserRegistry().get(item);
        }

        @Override
        public final ItemStack dispense(BlockSource source, ItemStack stack) {
            //this.setSuccessful(false);
            try {
                InteractionResultHolder<ItemStack> result = this.customBehavior(source, stack);
                InteractionResult type = result.getResult();
                if (type != InteractionResult.PASS) {
                    boolean success = type.consumesAction();
                    this.playSound(source, success);
                    this.playAnimation(source, source.state().getValue(DispenserBlock.FACING));
                    if (success) {
                        ItemStack resultStack = result.getObject();
                        if (resultStack.getItem() == stack.getItem()) return resultStack;
                        return fillItemInDispenser(source, stack, result.getObject());
                    }
                }
            } catch (Exception ignored) {
            }
            return fallback.dispense(source, stack);


        }

        /**
         * custom dispenser behavior that you want to implement
         *
         * @param source dispenser block
         * @param stack  stack to dispense
         * @return return ActionResult.SUCCESS / CONSUME for success, FAIL to do nothing and PASS to fall back to vanilla/previously registered behavior will be used. <br>
         * Type parameter is return item stack. If item in itemstack is different from initially provided, such itemstack will be added to dispenser, otherwise will replace existing itemstack
         */
        protected abstract InteractionResultHolder<ItemStack> customBehavior(BlockSource source, ItemStack stack);

        protected void playSound(BlockSource source, boolean success) {
            source.level().levelEvent(success ? 1000 : 1001, source.pos(), 0);
        }

        protected void playAnimation(BlockSource source, Direction direction) {
            source.level().levelEvent(2000, source.pos(), direction.get3DDataValue());
        }

        //returns full bottle to dispenser. same function that's in IDispenserItemBehavior
        private ItemStack fillItemInDispenser(BlockSource source, ItemStack empty, ItemStack filled) {
            empty.shrink(1);
            if (empty.isEmpty()) {
                return filled.copy();
            } else {
                if (!mergeDispenserItem(source.blockEntity(), filled)) {
                    SHOOT_BEHAVIOR.dispense(source, filled.copy());
                }
                return empty;
            }
        }

        //add item to dispenser and merges it if there's one already
        private boolean mergeDispenserItem(DispenserBlockEntity te, ItemStack filled) {
            NonNullList<ItemStack> stacks = ((DispenserBlockEntityAccessor) te).getItems();
            for (int i = 0; i < te.getContainerSize(); ++i) {
                ItemStack s = stacks.get(i);
                if (s.isEmpty() || (s.getItem() == filled.getItem() && s.getMaxStackSize() > s.getCount())) {
                    filled.grow(s.getCount());
                    te.setItem(i, filled);
                    return true;
                }
            }
            return false;
        }
    }

    public static class AddItemToInventoryBehavior extends AdditionalDispenserBehavior {

        public AddItemToInventoryBehavior(Item item) {
            super(item);
        }

        @Override
        protected InteractionResultHolder<ItemStack> customBehavior(BlockSource source, ItemStack stack) {
            //this.setSuccessful(false);
            ServerLevel world = source.level();
            BlockPos blockpos = source.pos().relative(source.state().getValue(DispenserBlock.FACING));
            BlockEntity te = world.getBlockEntity(blockpos);
            if (te instanceof WorldlyContainer tile) {
                if (tile.canPlaceItem(0, stack)) {
                    if (tile.isEmpty()) {
                        tile.setItem(0, stack.split(1));
                    } else {
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


    public static class PlaceBlockDispenseBehavior extends OptionalDispenseItemBehavior {

        @Override
        public ItemStack execute(BlockSource source, ItemStack stack) {
            this.setSuccess(false);
            Item item = stack.getItem();
            if (item instanceof BlockItem bi) {
                Direction direction = source.state().getValue(DispenserBlock.FACING);
                BlockPos blockpos = source.pos().relative(direction);
                // Direction direction1 = source.getLevel().isEmptyBlock(blockpos.below()) ? direction : Direction.UP;
                Direction direction1 = direction;
                InteractionResult result = bi.place(new DirectionalPlaceContext(source.level(), blockpos, direction, stack, direction1));
                this.setSuccess(result.consumesAction());
            }
            return stack;
        }
    }


    public static final DefaultDispenseItemBehavior PLACE_BLOCK_BEHAVIOR = new PlaceBlockDispenseBehavior();
    private static final DefaultDispenseItemBehavior SHOOT_BEHAVIOR = new DefaultDispenseItemBehavior();


}