package net.mehvahdjukaar.moonlight.api.set;

import net.mehvahdjukaar.moonlight.core.set.BlocksColorInternal;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nullable;
import java.util.Set;

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

    /**
     * @return all the possible keys which can be used to access the colored block groups
     */
    public static Set<String> getBlockKeys(){
        return BlocksColorInternal.getBlockKeys();
    }

    /**
     * @return all the possible keys which can be used to access the colored item groups
     */
    public static Set<String> getItemKeys(){
        return BlocksColorInternal.getItemKeys();
    }

    /**
     * This might be expensive so don't call often
     * Tag only works after world load of course
     * @param key set key
     * @return a HolderSet containing oll the values of this colored group. If available a tagged set will be returned (use unwrap().getLeft())
     */
    @Nullable
    public static HolderSet<Block> getBlockHolderSet(String key){
        return BlocksColorInternal.getBlockHolderSet(key);
    }

    /**
     * This might be expensive so don't call often
     * Tag only works after world load of course
     * @param key set key
     * @return a HolderSet containing oll the values of this colored group. If available a tagged set will be returned (use unwrap().getLeft())
     */
    @Nullable
    public static HolderSet<Item> getItemHolderSet(String key){
        return BlocksColorInternal.getItemHolderSet(key);
    }

}