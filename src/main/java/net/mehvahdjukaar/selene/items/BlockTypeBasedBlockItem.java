package net.mehvahdjukaar.selene.items;

import net.mehvahdjukaar.selene.block_set.BlockType;
import net.mehvahdjukaar.selene.block_set.wood.WoodType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.util.Lazy;
import org.jetbrains.annotations.Nullable;

public class BlockTypeBasedBlockItem<T extends BlockType> extends BlockItem {

    private final T blockType;
    private final Lazy<Integer> burnTime;

    public BlockTypeBasedBlockItem(Block pBlock, Properties pProperties, T blockType) {
        super(pBlock, pProperties);
        this.blockType = blockType;
        this.burnTime = Lazy.of(()->blockType.mainChild().asItem()
                .getBurnTime(blockType.mainChild().asItem().getDefaultInstance(), null));
    }

    @Override
    public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
        return burnTime.get();
    }

    @Override
    protected boolean allowdedIn(CreativeModeTab pCategory) {
        if (blockType.mainChild().asItem().getItemCategory() == null) return false;
        return super.allowdedIn(pCategory);
    }

    public T getBlockType() {
        return blockType;
    }
}
