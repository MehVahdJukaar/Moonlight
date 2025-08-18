package net.mehvahdjukaar.moonlight.api.util;

import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;

public final class SlotBlacklist {
    private final IntSet slots = new IntArraySet();
    private final IntSet offHandSlots = new IntArraySet();

    public void addSlot(int i) {
        this.slots.add(i);
    }

    public void addOffHand(int i) {
        this.offHandSlots.add(i);
    }

    public boolean containsSlot(int i) {
        return this.slots.contains(i);
    }

    public boolean containsOffHand(int i) {
        return this.offHandSlots.contains(i);
    }

    public void clear() {
        this.slots.clear();
        this.offHandSlots.clear();
    }

    public void removeSlot(int i) {
        this.slots.remove(i);
    }

    public void removeOffHand(int i) {
        this.offHandSlots.remove(i);
    }

    public void addHand(Inventory inventory, InteractionHand hand) {
        if (hand == InteractionHand.MAIN_HAND) {
            addSlot(inventory.selected);
        } else {
            for (int i = 0; i < inventory.offhand.size(); i++) {
                addOffHand(i);
            }
        }
    }
}