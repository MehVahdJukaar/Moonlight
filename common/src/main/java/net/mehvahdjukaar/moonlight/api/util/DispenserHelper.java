package net.mehvahdjukaar.moonlight.api.util;


import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.mehvahdjukaar.moonlight.api.block.ISoftFluidTankProvider;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.mehvahdjukaar.moonlight.api.item.additional_placements.AdditionalItemPlacementsAPI;
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
        if (isClient) return;
        //clear all behaviors
        Set<Item> failed = new HashSet<>();
        Map<Item, DispenseItemBehavior> originals = new HashMap<>();
        for (var e : MODDED_BEHAVIORS.entrySet()) {
            Item item = e.getKey();
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
                ItemStackResult result = this.customBehavior(source, stack);
                InteractionResult type = result.result();
                if (type != InteractionResult.PASS) {
                    boolean success = type.consumesAction();
                    this.playSound(source, success);
                    this.playAnimation(source, source.state().getValue(DispenserBlock.FACING));
                    if (success) {
                        ItemStack resultStack = result.stack();
                        if (resultStack.getItem() == stack.getItem()) return resultStack;
                        return fillItemInDispenser(source, stack, resultStack);
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
         * @return SUCCESS or CONSUME for success, FAIL to do nothing, PASS to fall back to vanilla. On success the
         * returned stack replaces the dispensed one, or is added to the dispenser if its item differs
         */
        protected abstract ItemStackResult customBehavior(BlockSource source, ItemStack stack);

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
        protected ItemStackResult customBehavior(BlockSource source, ItemStack stack) {
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
                    return ItemStackResult.success(stack);
                }
                return ItemStackResult.fail(stack);
            }
            return ItemStackResult.pass(stack);
        }
    }


    public static class PlaceBlockBehavior extends DispenserHelper.AdditionalDispenserBehavior {

        public PlaceBlockBehavior(Item item) {
            super(item);
        }

        @Override
        protected ItemStackResult customBehavior(BlockSource source, ItemStack stack) {
            Item item = stack.getItem();
            Direction direction = source.state().getValue(DispenserBlock.FACING);
            BlockPos blockpos = source.pos().relative(direction);
            DirectionalPlaceContext context = new DirectionalPlaceContext(source.level(), blockpos, direction, stack, direction);

            InteractionResult result = null;
            //takes priority, same as when used by a player
            var placement = AdditionalItemPlacementsAPI.getBehavior(item);
            if (placement != null) {
                result = placement.overridePlace(context);
            }
            if ((result == null || !result.consumesAction()) && item instanceof BlockItem bi) {
                result = bi.place(context);
            }
            if (result != null && result.consumesAction()) {
                return new ItemStackResult(result, stack);
            }
            return ItemStackResult.pass(stack);
        }
    }

    public static class FillFluidHolderBehavior extends DispenserHelper.AdditionalDispenserBehavior {

        public FillFluidHolderBehavior(Item item) {
            super(item);
        }

        @Override
        protected ItemStackResult customBehavior(BlockSource source, ItemStack stack) {
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
                            return ItemStackResult.success(returnStack);
                        }
                    }
                }
                return ItemStackResult.fail(stack);
            }
            return ItemStackResult.pass(stack);
        }
    }

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