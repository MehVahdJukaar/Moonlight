package net.mehvahdjukaar.selene.impl.items;

import net.mehvahdjukaar.selene.block_set.wood.WoodType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

public class WoodBasedBlockItem extends BlockTypeBasedBlockItem<WoodType> {

    private final int burnTime;

    public WoodBasedBlockItem(Block blockIn, Properties builder, WoodType woodType) {
        this(blockIn, builder, woodType, 300);
    }

    public WoodBasedBlockItem(Block blockIn, Properties builder, int burnTicks) {
        this(blockIn, builder, WoodType.OAK_WOOD_TYPE, burnTicks);
    }

    public WoodBasedBlockItem(Block blockIn, Properties builder, WoodType woodType, int burnTicks) {
        super(blockIn, builder, woodType);
        this.burnTime = woodType.canBurn() ? burnTicks : 0;
    }

    @Override
    public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
        return this.burnTime;
    }

}
