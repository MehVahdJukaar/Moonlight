package net.mehvahdjukaar.moonlight.api.item;

import com.google.common.base.Suppliers;
import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.mehvahdjukaar.moonlight.api.set.BlockType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

public class BlockTypeBasedBlockItem<T extends BlockType> extends FuelBlockItem {

    private final T blockType;

    public BlockTypeBasedBlockItem(Block pBlock, Properties pProperties, T blockType, Supplier<Integer> burnTime) {
        super(pBlock, pProperties, burnTime);
        this.blockType = blockType;
    }

    public BlockTypeBasedBlockItem(Block pBlock, Properties pProperties, T blockType) {
        this(pBlock, pProperties, blockType, Suppliers.memoize(() -> PlatformHelper.getBurnTime(blockType.mainChild().asItem().getDefaultInstance())));
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
