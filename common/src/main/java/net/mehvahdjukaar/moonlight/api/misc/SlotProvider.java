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
        if (hand == InteractionHand.MAIN_HAND) return MAIN_HAND;
        else return OFF_HAND;
    }

    static SlotProvider single(int slot) {
        return inv -> List.of(Slot.invSlot(inv, slot)).iterator();
    }

    SlotProvider ALL = inv -> IntStream.range(0, inv.items.size())
            .mapToObj(i -> Slot.invSlot(inv, i)).iterator();

    SlotProvider OFF_HAND = inv -> IntStream.range(0, inv.offhand.size())
            .mapToObj(i -> Slot.offHandSlot(inv, i)).iterator();

    SlotProvider MAIN_HAND = inv -> List.of(Slot.invSlot(inv, inv.selected)).iterator();

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
                public boolean add(ItemStack toAdd, Inventory inv, Player player) {
                    ItemStack current = getStack();
                    //vanilla doesn't do this for some reason... calling add alone will just incrememnt the count of an existing item
                    if (!current.isEmpty() && !inv.hasRemainingSpaceForItem(current, toAdd)) return false;

                    //same as vanilla .add but no damageable and creative bs logic
                    if (toAdd.isEmpty()) {
                        return false;
                    } else {
                        try {
                            int originalCount;
                            do {
                                originalCount = toAdd.getCount();
                                toAdd.setCount(inv.addResource(slot, toAdd));
                            } while (!toAdd.isEmpty() && toAdd.getCount() < originalCount);

                            return toAdd.getCount() < originalCount;
                        } catch (Throwable var6) {
                            CrashReport crashReport = CrashReport.forThrowable(var6, "Adding item to inventory");
                            CrashReportCategory crashReportCategory = crashReport.addCategory("Item being added");
                            crashReportCategory.setDetail("Item ID", Item.getId(toAdd.getItem()));
                            crashReportCategory.setDetail("Item data", toAdd.getDamageValue());
                            crashReportCategory.setDetail("Item name", () -> toAdd.getHoverName().getString());
                            throw new ReportedException(crashReport);
                        }
                    }
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

                private void addResourceOffHand(ItemStack toAdd, Inventory inv) {
                    Item item = toAdd.getItem();
                    int stackCount;
                    NonNullList<ItemStack> offHand = inv.offhand;
                    for (int offSlot = 0; offSlot < offHand.size(); offSlot++) {
                        stackCount = toAdd.getCount();
                        ItemStack handStack = offHand.get(offSlot);
                        if (handStack.isEmpty()) {
                            handStack = new ItemStack(item, 0);
                            if (toAdd.hasTag()) {
                                handStack.setTag(toAdd.getTag().copy());
                            }

                            offHand.set(offSlot, handStack);
                        } else if (!inv.hasRemainingSpaceForItem(handStack, toAdd)) {
                            continue;
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

                            toAdd.setCount(stackCount);
                        }
                    }
                }
            };
        }
    }

}

