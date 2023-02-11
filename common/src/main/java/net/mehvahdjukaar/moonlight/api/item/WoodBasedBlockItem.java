package net.mehvahdjukaar.moonlight.api.item;

import net.mehvahdjukaar.moonlight.api.set.wood.WoodType;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodTypeRegistry;
import net.minecraft.world.level.block.Block;

public class WoodBasedBlockItem extends BlockTypeBasedBlockItem<WoodType> {

    public WoodBasedBlockItem(Block blockIn, Properties builder, WoodType woodType) {
        this(blockIn, builder, woodType, 300);
    }

    public WoodBasedBlockItem(Block blockIn, Properties builder, int burnTicks) {
        this(blockIn, builder, WoodTypeRegistry.OAK_TYPE, burnTicks);
    }

    public WoodBasedBlockItem(Block blockIn, Properties builder, WoodType woodType, int burnTicks) {
        super(blockIn, builder, woodType, woodType.canBurn() ? () -> burnTicks : () -> 0);
    }


}
