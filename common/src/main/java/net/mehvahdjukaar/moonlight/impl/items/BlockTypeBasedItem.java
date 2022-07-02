package net.mehvahdjukaar.moonlight.impl.items;

import net.mehvahdjukaar.moonlight.block_set.BlockType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.common.util.Lazy;
import org.jetbrains.annotations.Nullable;

public class BlockTypeBasedItem<T extends BlockType> extends Item {

    private final T blockType;
    private final Lazy<Integer> burnTime;

    public BlockTypeBasedItem(Properties pProperties, T blockType) {
        super(pProperties);

        this.blockType = blockType;
        this.burnTime = Lazy.of(() -> blockType.mainChild().asItem()
                .getBurnTime(blockType.mainChild().asItem().getDefaultInstance(), null));
    }

    @Override
    public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
        return burnTime.get();
    }

    @Override
    protected boolean allowedIn(CreativeModeTab tab) {
        if (blockType.mainChild().asItem().getItemCategory() == null) return false;
        return super.allowedIn(tab);
    }

    public T getBlockType() {
        return blockType;
    }
}
