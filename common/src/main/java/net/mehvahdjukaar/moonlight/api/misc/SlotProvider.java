package net.mehvahdjukaar.moonlight.api.misc;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.NonNullList;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Iterator;
import java.util.List;
import java.util.stream.IntStream;

public interface SlotProvider {
    Iterator<Slot> getSlots(Inventory inv);


    static SlotProvider hand(InteractionHand hand) {
        if (hand == InteractionHand.MAIN_HAND) return inv -> List.of(Slot.invSlot(inv, inv.selected)).iterator();
        else return inv -> IntStream.range(0, inv.offhand.size() - 1)
                .mapToObj(i -> Slot.offHandSlot(inv, i)).iterator();
    }

    static SlotProvider single(int slot) {
        return inv -> List.of(Slot.invSlot(inv, slot)).iterator();
    }

    SlotProvider ALL = inv -> IntStream.range(0, inv.items.size())
            .mapToObj(i -> Slot.invSlot(inv, i)).iterator();

    interface Slot {
        ItemStack getStack();

        boolean add(ItemStack stack, Inventory inv, Player player);

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
    }

}

