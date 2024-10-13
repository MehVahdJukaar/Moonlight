package net.mehvahdjukaar.moonlight.api.util;


import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.mehvahdjukaar.moonlight.api.block.ISoftFluidTankProvider;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.mixins.accessor.DispenserBlockAccessor;
import net.mehvahdjukaar.moonlight.core.mixins.accessor.DispenserBlockEntityAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
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
import org.jetbrains.annotations.ApiStatus;

import java.util.*;
import java.util.function.Consumer;

// point of this class is to
// dynamically register dispenser behaviors such that they can depend on tags
public class DispenserHelper {

    private static final Map<Item, List<DispenseItemBehavior>> MODDED_BEHAVIORS = new HashMap<>();
    //TODO: remove once mods have updated
    private static final Map<Item, List<DispenseItemBehavior>> STATIC_MODDED_BEHAVIORS = new HashMap<>();
    private static final Map<Priority, List<Consumer<Event>>> EVENT_LISTENERS = Map.of(
            Priority.LOW, new ArrayList<>(),
            Priority.NORMAL, new ArrayList<>(),
            Priority.HIGH, new ArrayList<>()
    );

    public static void addListener(Consumer<Event> listener, Priority priority) {
        EVENT_LISTENERS.get(priority).add(listener);
    }

    @ApiStatus.Internal
    public static void reload(RegistryAccess registryAccess, boolean isClient) {
        //clear all behaviors
        Set<Item> failed = new HashSet<>();
        Map<Item, DispenseItemBehavior> originals = new HashMap<>();
        for (var e : MODDED_BEHAVIORS.entrySet()) {
            Item item = e.getKey();
            // dont alter these as we cant override them since they are static otherwise we would lose them
            if (STATIC_MODDED_BEHAVIORS.containsKey(item)) continue;
            var expected = new ReferenceOpenHashSet<>(e.getValue());
            var current = DispenserBlock.DISPENSER_REGISTRY.get(item);
            if (current instanceof AdditionalDispenserBehavior behavior) {
                Set<AdditionalDispenserBehavior> visited = new ReferenceOpenHashSet<>();
                var original = unwrapBehavior(behavior, visited);
                if (expected.contains(original)) {
                    expected.remove(original);
                    //if original was also a custom behavior we unregister it
                    original = null;
                }
                if (expected.equals(visited)) {
                    originals.put(item, original);
                } else {
                    Moonlight.LOGGER.warn("Failed to unwrap original behavior for item: {}, {}, {}", item, current, expected);
                    failed.add(item);
                }
            } else if (expected.size() == 1 && expected.stream().findAny().get() == current) {
                originals.put(item, null);
            } else {
                failed.add(item);
                Moonlight.LOGGER.error("Failed to restore original behavior for item: {}, {}", item, current);
            }
        }
        //restore vanilla state
        for (var e : originals.entrySet()) {
            Item item = e.getKey();
            DispenseItemBehavior behavior = e.getValue();
            if (behavior != null) {
                DispenserBlock.registerBehavior(item, behavior);
            } else {
                DispenserBlock.DISPENSER_REGISTRY.remove(item);
            }
        }

        //re-register all behaviors
        MODDED_BEHAVIORS.clear();

        failed.addAll(STATIC_MODDED_BEHAVIORS.keySet());

        Event event = new Event() {
            @Override
            public void register(Item i, DispenseItemBehavior behavior) {
                if (!failed.contains(i)) {
                    MODDED_BEHAVIORS.computeIfAbsent(i, k -> new ArrayList<>()).add(behavior);
                    DispenserBlock.registerBehavior(i, behavior);
                }
            }

            @Override
            public RegistryAccess getRegistryAccess() {
                return registryAccess;
            }
        };
        EVENT_LISTENERS.get(Priority.LOW).forEach(l -> l.accept(event));
        EVENT_LISTENERS.get(Priority.NORMAL).forEach(l -> l.accept(event));
        EVENT_LISTENERS.get(Priority.HIGH).forEach(l -> l.accept(event));
    }


    // this only works if our behaviors are the outermost of the wrappers. This should usually be the case as most mods will run their registering code in setup and not on world load
    private static DispenseItemBehavior unwrapBehavior(AdditionalDispenserBehavior behavior, Set<AdditionalDispenserBehavior> visited) {
        visited.add(behavior);
        var inner = behavior.fallback;
        if (inner instanceof AdditionalDispenserBehavior ab) {
            return unwrapBehavior(ab, visited);
        }
        return inner;
    }

    @Deprecated(forRemoval = true)
    public static void registerCustomBehavior(AdditionalDispenserBehavior behavior) {
        DispenserBlock.registerBehavior(behavior.item, behavior);
        STATIC_MODDED_BEHAVIORS.computeIfAbsent(behavior.item, k -> new ArrayList<>()).add(behavior);
    }

    //block placement behavior
    @Deprecated(forRemoval = true)
    public static void registerPlaceBlockBehavior(ItemLike block) {
        DispenserBlock.registerBehavior(block, PLACE_BLOCK_BEHAVIOR);
        STATIC_MODDED_BEHAVIORS.computeIfAbsent(block.asItem(), k -> new ArrayList<>()).add(PLACE_BLOCK_BEHAVIOR);
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


    public static class PlaceBlockBehavior extends DispenserHelper.AdditionalDispenserBehavior {

        public PlaceBlockBehavior(Item item) {
            super(item);
        }

        @Override
        protected InteractionResultHolder<ItemStack> customBehavior(BlockSource source, ItemStack stack) {
            Item item = stack.getItem();
            if (item instanceof BlockItem bi) {
                Direction direction = source.state().getValue(DispenserBlock.FACING);
                BlockPos blockpos = source.pos().relative(direction);
                // Direction direction1 = source.getLevel().isEmptyBlock(blockpos.below()) ? direction : Direction.UP;
                InteractionResult result = bi.place(new DirectionalPlaceContext(source.level(), blockpos, direction, stack, direction));
                var res = new InteractionResultHolder<>(result, stack);
                if (result.consumesAction()) {
                    return res;
                }
            }
            return InteractionResultHolder.pass(stack);
        }
    }

    @Deprecated(forRemoval = true)
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

    public static class FillFluidHolderBehavior extends DispenserHelper.AdditionalDispenserBehavior {

        public FillFluidHolderBehavior(Item item) {
            super(item);
        }

        @Override
        protected InteractionResultHolder<ItemStack> customBehavior(BlockSource source, ItemStack stack) {
            BlockPos blockpos = source.pos().relative(source.state().getValue(DispenserBlock.FACING));
            BlockEntity te = source.level().getBlockEntity(blockpos);
            if (te instanceof ISoftFluidTankProvider tile) {
                ItemStack returnStack;
                if (tile.canInteractWithSoftFluidTank()) {

                    SoftFluidTank tank = tile.getSoftFluidTank();
                    if (!tank.isFull()) {
                        returnStack = tank.interactWithItem(stack, source.level(), blockpos, false);
                        if (returnStack != null) {
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

    @Deprecated(forRemoval = true)
    public static final DefaultDispenseItemBehavior PLACE_BLOCK_BEHAVIOR = new PlaceBlockDispenseBehavior();
    private static final DefaultDispenseItemBehavior SHOOT_BEHAVIOR = new DefaultDispenseItemBehavior();


    public interface Event {

        void register(Item i, DispenseItemBehavior behavior);

        default void register(AdditionalDispenserBehavior behavior) {
            register(behavior.item, behavior);
        }

        default void registerPlaceBlock(ItemLike i) {
            register(i.asItem(), new PlaceBlockBehavior(i.asItem()));
        }


        RegistryAccess getRegistryAccess();
    }


    // when wrapping order is important. Use this to set priority
    public enum Priority {
        LOW,
        NORMAL,
        HIGH
    }

}