package net.mehvahdjukaar.moonlight.api.util;


import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.mixins.accessor.DispenserBlockAccessor;
import net.mehvahdjukaar.moonlight.core.mixins.accessor.DispenserBlockEntityAccessor;
import net.minecraft.core.*;
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
    private static final Map<Item, List<DispenseItemBehavior>> STATIC_MODDED_BEHAVIORS = new HashMap<>();
    private static final List<Consumer<Event>> EVENT_LISTENERS = new ArrayList<>();

    public static void addListener(Consumer<Event> listener) {
        EVENT_LISTENERS.add(listener);
    }

    @ApiStatus.Internal
    public static void reload(RegistryAccess registryAccess) {
        //clear all behaviors
        Set<Item> failed = new HashSet<>();
        Map<Item, DispenseItemBehavior> originals = new HashMap<>();
        for (var e : MODDED_BEHAVIORS.entrySet()) {
            Item item = e.getKey();
            var expected = new ArrayList<>(e.getValue());
            expected.addAll(STATIC_MODDED_BEHAVIORS.getOrDefault(item, List.of()));
            var current = DispenserBlock.DISPENSER_REGISTRY.get(item);
            if (current instanceof AdditionalDispenserBehavior behavior) {
                List<AdditionalDispenserBehavior> visited = new ArrayList<>();
                var original = unwrapBehavior(behavior, visited);
                if (expected.equals(visited)) {
                    originals.put(item, original);
                } else {
                    Moonlight.LOGGER.warn("Failed to unwrap original behavior for item: {}, {}, {}", item, current, expected);
                    failed.add(item);
                }
            } else if (expected.size() == 1 && expected.get(0) == current) {
                originals.put(item, null);
            } else {
                failed.add(item);
                Moonlight.LOGGER.error("Failed to restore original behavior for item: {}, {}", item, current);
            }
        }
        //restore vanilla state
        for (var e : originals.entrySet()) {
            DispenserBlock.registerBehavior(e.getKey(), e.getValue());
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
        EVENT_LISTENERS.forEach(listener -> listener.accept(event));
    }


    // this only works if our behaviors are the outermost of the wrappers. This should usually be the case as most mods will run their registering code in setup and not on world load
    private static DispenseItemBehavior unwrapBehavior(AdditionalDispenserBehavior behavior, List<AdditionalDispenserBehavior> visited) {
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
    }

    //block placement behavior
    @Deprecated(forRemoval = true)
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
                    this.playAnimation(source, source.getBlockState().getValue(DispenserBlock.FACING));
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
            source.getLevel().levelEvent(success ? 1000 : 1001, source.getPos(), 0);
        }

        protected void playAnimation(BlockSource source, Direction direction) {
            source.getLevel().levelEvent(2000, source.getPos(), direction.get3DDataValue());
        }

        //returns full bottle to dispenser. same function that's in IDispenserItemBehavior
        private ItemStack fillItemInDispenser(BlockSource source, ItemStack empty, ItemStack filled) {
            empty.shrink(1);
            if (empty.isEmpty()) {
                return filled.copy();
            } else {
                if (!mergeDispenserItem(source.getEntity(), filled)) {
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
            ServerLevel world = source.getLevel();
            BlockPos blockpos = source.getPos().relative(source.getBlockState().getValue(DispenserBlock.FACING));
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
                Direction direction = source.getBlockState().getValue(DispenserBlock.FACING);
                BlockPos blockpos = source.getPos().relative(direction);
                // Direction direction1 = source.getLevel().isEmptyBlock(blockpos.below()) ? direction : Direction.UP;
                Direction direction1 = direction;
                InteractionResult result = bi.place(new DirectionalPlaceContext(source.getLevel(), blockpos, direction, stack, direction1));
                this.setSuccess(result.consumesAction());
            }
            return stack;
        }
    }


    public static final DefaultDispenseItemBehavior PLACE_BLOCK_BEHAVIOR = new PlaceBlockDispenseBehavior();
    private static final DefaultDispenseItemBehavior SHOOT_BEHAVIOR = new DefaultDispenseItemBehavior();


    public interface Event {
        void register(Item i, DispenseItemBehavior behavior);

        default void register(AdditionalDispenserBehavior behavior) {
            register(behavior.item, behavior);
        }

        default void registerPlaceBlock(ItemLike i) {
            register(i.asItem(), PLACE_BLOCK_BEHAVIOR);
        }

        RegistryAccess getRegistryAccess();
    }

}