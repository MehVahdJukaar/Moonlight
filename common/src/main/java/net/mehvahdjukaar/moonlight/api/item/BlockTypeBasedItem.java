package net.mehvahdjukaar.moonlight.api.item;

import com.google.common.base.Suppliers;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.set.BlockType;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

// no longer a fuel item. Use platform specific class on fabric. On forge add a fuel data map entry to its json data map. Dont skip on API!
public class BlockTypeBasedItem<T extends BlockType> extends Item {

    private final T blockType;

    public BlockTypeBasedItem(Properties pProperties, T blockType) {
        super(pProperties);
        this.blockType = blockType;
    }

    public T getBlockType() {
        return blockType;
    }
}
