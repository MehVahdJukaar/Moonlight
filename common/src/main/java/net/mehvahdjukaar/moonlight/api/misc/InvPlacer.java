package net.mehvahdjukaar.moonlight.api.misc;

import com.google.common.base.Predicates;
import com.mojang.datafixers.util.Pair;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.NonNullList;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.IntStream;

//hand not empty, existing item, any item
public interface InvPlacer {


   static void testImpl(ItemStack toAdd, Inventory inventory, Player player) {
        InteractionHand hand = InteractionHand.MAIN_HAND;

        InvPlacer infPlacer = InvPlacer.progressiveExclusive()
                .stage(InvPlacer.handSlot(hand))                    // try hands first
                .stage(InvPlacer.allSlots(), i -> i.is(toAdd.getItem())) // existing matching slots
                .stage(InvPlacer.allSlots(), ItemStack::isEmpty) //remaining empty Slots
                .or(InvPlacer.ANY);    // optional final fallback, any slot works now

       infPlacer.place(toAdd, inventory, player);
    }

    static Incremental progressiveExclusive() {
        return new Incremental();
    }

    class Incremental implements InvPlacer {

        private final List<Pair<SlotProvider, Predicate<ItemStack>>> stages = new ArrayList<>();
        private final Set<Slot> visitedSlots = new HashSet<>();

        public Incremental stage(SlotProvider provider, Predicate<ItemStack> predicate) {
            this.stages.add(Pair.of(provider, predicate));
            return this;
        }

        public Incremental stage(SlotProvider provider) {
            return stage(provider, Predicates.alwaysTrue());
        }

        @Override
        public boolean place(ItemStack stack, Inventory inventory, Player player) {
            for (var stage : stages) {

                Iterator<Slot> slots = stage.getFirst().getSlots(inventory);
                while (slots.hasNext()) {
                    Slot slot = slots.next();
                    if (visitedSlots.contains(slot)) continue; //skip already visited slots
                    if (stage.getSecond().test(slot.getStack())) {
                        visitedSlots.add(slot);
                        if (slot.add(stack, inventory, player)) {
                            return true;
                        }
                        return true;
                    }
                }
            }
            return false;
        }
    }

    static SlotProvider handSlot(InteractionHand hand) {
        if (hand == InteractionHand.MAIN_HAND) return inv -> List.of(invSlot(inv, inv.selected)).iterator();
        else return inv -> IntStream.range(0, inv.offhand.size() - 1)
                .mapToObj(i -> offHandSlot(inv, i)).iterator();
    }

    static SlotProvider allSlots() {
        return inv -> IntStream.range(0, inv.items.size())
                .mapToObj(i -> invSlot(inv, i)).iterator();
    }

    static SlotProvider specificSlot(int slot) {
        return inv -> List.of(invSlot(inv, slot)).iterator();
    }

    interface SlotProvider {
        Iterator<Slot> getSlots(Inventory inv);
    }

    interface Slot {
        ItemStack getStack();

        boolean add(ItemStack stack, Inventory inv, Player player);
    }

    static Slot invSlot(Inventory inv, int slot) {
        return new Slot() {
            @Override
            public ItemStack getStack() {
                return inv.getItem(slot);
            }

            @Override
            public boolean add(ItemStack stack, Inventory inv, Player player) {
                return inv.add(slot, stack);
            }
        };
    }

    static Slot offHandSlot(Inventory inv, int offHandSlot) {
        return new Slot() {
            @Override
            public ItemStack getStack() {
                return inv.offhand.get(offHandSlot);
            }

            //copied from Inventory.addResource but for offhand
            @Override
            public boolean add(ItemStack stack, Inventory inv, Player player) {
                if (stack.isEmpty()) {
                    return false;
                } else {
                    try {
                        int originalCount;
                        do {
                            originalCount = stack.getCount();
                            addResourceOffHand(stack, inv);
                        } while (!stack.isEmpty() && stack.getCount() < originalCount);

                        if (stack.getCount() == originalCount && player.getAbilities().instabuild) {
                            stack.setCount(0);
                            return true;
                        } else {
                            return stack.getCount() < originalCount;
                        }
                    } catch (Throwable var6) {
                        CrashReport crashReport = CrashReport.forThrowable(var6, "Adding item to inventory");
                        CrashReportCategory crashReportCategory = crashReport.addCategory("Item being added");
                        crashReportCategory.setDetail("Item ID", Item.getId(stack.getItem()));
                        crashReportCategory.setDetail("Item data", stack.getDamageValue());
                        crashReportCategory.setDetail("Item name", () -> stack.getHoverName().getString());
                        throw new ReportedException(crashReport);
                    }
                }
            }

            private void addResourceOffHand(ItemStack stack, Inventory inv) {
                Item item = stack.getItem();
                int stackCount;
                NonNullList<ItemStack> offHand = inv.offhand;
                for (int offSlot = 0; offSlot < offHand.size(); offSlot++) {
                    stackCount = stack.getCount();
                    ItemStack handStack = offHand.get(offSlot);
                    if (handStack.isEmpty()) {
                        handStack = new ItemStack(item, 0);
                        if (stack.hasTag()) {
                            handStack.setTag(stack.getTag().copy());
                        }

                        offHand.set(offSlot, handStack);
                    }

                    int addedCount = stackCount;
                    if (addedCount > handStack.getMaxStackSize() - handStack.getCount()) {
                        addedCount = handStack.getMaxStackSize() - handStack.getCount();
                    }

                    if (addedCount > inv.getMaxStackSize() - handStack.getCount()) {
                        addedCount = inv.getMaxStackSize() - handStack.getCount();
                    }

                    if (addedCount != 0) {
                        stackCount -= addedCount;
                        handStack.grow(addedCount);
                        handStack.setPopTime(5);

                        stack.setCount(stackCount);
                    }
                }
            }
        };
    }

    boolean place(ItemStack stack, Inventory inventory, Player player);

    default InvPlacer or(InvPlacer other) {
        return (stack, inventory, player) -> this.place(stack, inventory, player) || other.place(stack, inventory, player);
    }

    static InvPlacer handNotEmpty(InteractionHand hand) {
        return hand(hand, stack -> !stack.isEmpty());
    }

    static InvPlacer hand(InteractionHand hand) {
        return hand(hand, Predicates.alwaysTrue());
    }

    static InvPlacer hand(InteractionHand hand, Predicate<ItemStack> predicate) {
        return filtered(handSlot(hand), predicate);
    }

    static InvPlacer filtered(SlotProvider slots) {
        return filtered(slots, Predicates.alwaysTrue());
    }

    static InvPlacer filtered(SlotProvider slots, Predicate<ItemStack> predicate) {
        return (stack, inventory, player) -> {
            var iterator = slots.getSlots(inventory);
            while (iterator.hasNext()) {
                Slot slot = iterator.next();
                if (predicate.test(slot.getStack())) {
                    if (slot.add(stack, inventory, player)) {
                        return true;
                    }
                }
            }
            return false;
        };
    }

    static InvPlacer slot(int slot) {
        return filtered(specificSlot(slot));
    }

    InvPlacer EXISTING = filtered(allSlots(), stack -> !stack.isEmpty());

    InvPlacer EMPTY = filtered(allSlots(), ItemStack::isEmpty);

    InvPlacer ANY = filtered(allSlots());

    InvPlacer DROP = (stack, inventory, player) -> {
        player.drop(stack, false);
        return true;
    };
}
