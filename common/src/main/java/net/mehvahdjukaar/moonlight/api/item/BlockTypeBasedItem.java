package net.mehvahdjukaar.moonlight.api.item;

import com.google.common.base.Suppliers;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.set.BlockType;
import net.minecraft.world.item.CreativeModeTab;

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

    @Override
    protected boolean allowedIn(CreativeModeTab tab) {
        if (blockType.mainChild().asItem().getItemCategory() == null) return false;
        return super.allowedIn(tab);
    }

    public T getBlockType() {
        return blockType;
    }
}
