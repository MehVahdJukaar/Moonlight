package net.mehvahdjukaar.moonlight.api.set;

import com.google.common.collect.ImmutableList;
import net.mehvahdjukaar.moonlight.core.set.BlocksColorInternal;
import net.minecraft.Util;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Stream;

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


    /**
     * Registers or override a color set if not already detected
     */
    public static void registerBlockColorSet(ResourceLocation key, EnumMap<DyeColor, Block> blocks, @Nullable Block defaultBlock) {
        BlocksColorInternal.registerBlockColorSet(key, blocks, defaultBlock);
    }

    public static void registerItemColorSet(ResourceLocation key, EnumMap<DyeColor, Item> items, @Nullable Item defaultItem) {
        BlocksColorInternal.registerItemColorSet(key, items, defaultItem);
    }

    public static final List<DyeColor> SORTED_COLORS = Util.make(() -> {
        ImmutableList.Builder<DyeColor> b = ImmutableList.builder();
        var l = List.of(DyeColor.WHITE, DyeColor.LIGHT_GRAY, DyeColor.GRAY, DyeColor.BLACK, DyeColor.BROWN,
                DyeColor.RED, DyeColor.ORANGE, DyeColor.YELLOW, DyeColor.LIME, DyeColor.GREEN,
                DyeColor.CYAN, DyeColor.LIGHT_BLUE, DyeColor.BLUE, DyeColor.PURPLE, DyeColor.MAGENTA, DyeColor.PINK);
        b.addAll(l);
        for(var v : DyeColor.values()){
            if(!l.contains(v))b.add(v);
        }
        return b.build();
    });

    /**
     * Helper to register colors in order
     */
    public static<T> Stream<T> ordered(Map<DyeColor, T> map){
       return map.entrySet().stream()
                .sorted(Comparator.comparing(entry -> SORTED_COLORS.indexOf(entry.getKey())))
                .map(Map.Entry::getValue);
    }
}