package net.mehvahdjukaar.moonlight.api.set;

import net.mehvahdjukaar.moonlight.core.set.BlocksColorInternal;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nullable;

public class BlocksColorAPI {

    @Nullable
    public static DyeColor getColor(Block block) {
        return BlocksColorInternal.getColor(block);
    }

    @Nullable
    public static DyeColor getColor(Item item) {
        return BlocksColorInternal.getColor(item);
    }

    @Nullable
    public static Item getColoredItem(String key, @Nullable DyeColor color) {
        return BlocksColorInternal.getColoredItem(key, color);
    }

    @Nullable
    public static Block getColoredBlock(String key, @Nullable DyeColor color) {
        return BlocksColorInternal.getColoredBlock(key, color);
    }

    /**
     * Changes this block color
     * If the given color is null it will yield the default colored block, usually uncolored or white
     * Will return null if no block can be found using that color
     */
    @Nullable
    public static Block changeColor(Block old, @Nullable DyeColor newColor) {
        return BlocksColorInternal.changeColor(old, newColor);
    }

    /**
     * Changes this item color
     * If the given color is null it will yield the default colored item, usually uncolored or white
     * Will return null if no item can be found using that color
     */
    @Nullable
    public static Item changeColor(Item old, @Nullable DyeColor newColor) {
        return BlocksColorInternal.changeColor(old, newColor);
    }

    @Nullable
    public static String getKey(Block block) {
        return BlocksColorInternal.getKey(block);
    }

    @Nullable
    public static String getKey(Item item) {
        return BlocksColorInternal.getKey(item);
    }

}