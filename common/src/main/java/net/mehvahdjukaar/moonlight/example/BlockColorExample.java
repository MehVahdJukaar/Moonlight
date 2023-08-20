package net.mehvahdjukaar.moonlight.example;

import net.mehvahdjukaar.moonlight.api.set.BlocksColorAPI;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class BlockColorExample {

    // changes item in hand with a new color
    public static void onItemUse(Level level, ItemStack stack, BlockState oldState, BlockPos pos) {
        DyeColor color = BlocksColorAPI.getColor(stack.getItem());
        if (color != null) {
            Block newBlock = BlocksColorAPI.changeColor(oldState.getBlock(), color);
            if (newBlock != null) {
                level.setBlockAndUpdate(pos, newBlock.withPropertiesOf(oldState));
                stack.shrink(1);
            }
        }
    }

    // gets colored terracotta from dye. Works with any modded item as well
    public static Item getColoredTerracotta(DyeColor color) {
        return BlocksColorAPI.getColoredItem("terracotta", color);
    }
}
