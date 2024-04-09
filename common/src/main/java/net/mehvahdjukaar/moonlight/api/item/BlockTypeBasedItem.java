package net.mehvahdjukaar.moonlight.api.item;

import com.google.common.base.Suppliers;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.set.BlockType;

import java.util.function.Supplier;

public class BlockTypeBasedItem<T extends BlockType> extends FuelItem {

    private final T blockType;

    public BlockTypeBasedItem(Properties pProperties, T blockType) {
        this(pProperties, blockType, Suppliers.memoize(() -> PlatHelper.getBurnTime(blockType.mainChild().asItem().getDefaultInstance())));
    }

    public BlockTypeBasedItem(Properties pProperties, T blockType, Supplier<Integer> burnTime) {
        super(pProperties, burnTime);
        this.blockType = blockType;
    }

    public T getBlockType() {
        return blockType;
    }
}
