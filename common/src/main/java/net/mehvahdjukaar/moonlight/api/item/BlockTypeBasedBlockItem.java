package net.mehvahdjukaar.moonlight.api.item;

import com.google.common.base.Suppliers;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.set.BlockType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

public class BlockTypeBasedBlockItem<T extends BlockType> extends BlockItem {

    private final T blockType;

    public BlockTypeBasedBlockItem(Block pBlock, Properties pProperties, T blockType) {
        super(pBlock, pProperties);
        this.blockType = blockType;
    }

    public T getBlockType() {
        return blockType;
    }
}
