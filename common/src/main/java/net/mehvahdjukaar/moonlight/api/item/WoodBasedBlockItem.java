package net.mehvahdjukaar.moonlight.api.item;

import net.mehvahdjukaar.moonlight.api.set.wood.WoodType;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodTypeRegistry;
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
        this(blockIn, builder, WoodTypeRegistry.OAK_TYPE, burnTicks);
    }

    public WoodBasedBlockItem(Block blockIn, Properties builder, WoodType woodType, int burnTicks) {
        super(blockIn, builder, woodType);
        this.burnTime = woodType.canBurn() ? burnTicks : 0;
    }

    @Override
    public final int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
        return this.burnTime;
    }

}
