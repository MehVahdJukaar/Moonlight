package net.mehvahdjukaar.moonlight.api.item;

import net.mehvahdjukaar.moonlight.api.set.wood.WoodType;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodTypeRegistry;
import net.minecraft.world.level.block.Block;

public class WoodBasedBlockItem extends BlockTypeBasedBlockItem<WoodType> {

    public WoodBasedBlockItem(Block blockIn, Properties builder, WoodType woodType) {
        super(blockIn, builder, woodType);
    }

}
