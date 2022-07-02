package net.mehvahdjukaar.moonlight.util;

import com.google.common.collect.Sets;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Set;
import java.util.function.Supplier;

public final class ItemCategoryFiller {
    private final Supplier<Item> startFillingFrom;
    private final Set<Item> processedItems = Sets.newHashSet();
    private int currentIndex = 1;

    public ItemCategoryFiller(Supplier<Item> targetItem) {
        this.startFillingFrom = targetItem;
    }

    public void fillItemCategory(Item item, NonNullList<ItemStack> items) {
        if (processedItems.contains(item)) {
            this.currentIndex = 1;
            this.processedItems.clear();
        }
        Item target = this.startFillingFrom.get();
        int index = -1;
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).is(target)) index = i;
        }
        if (index != -1) {
            items.add(index + currentIndex, new ItemStack(item));
            processedItems.add(item);
            currentIndex++;
        } else {
            items.add(new ItemStack(item));
        }
    }

}
