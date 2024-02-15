package net.mehvahdjukaar.moonlight.api.item.additional_placements;

import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Something called by mixin which should place or alter a block when clicked on
 * Extend this class if you intend on having a placeable block
 */
public class AdditionalItemPlacement{

    private final Block placeable;

    public AdditionalItemPlacement(Block placeable){
        this.placeable = placeable;
    }

    public static BlockPlacerItem getBlockPlacer(){
        return BlockPlacerItem.get();
    }

    @Nullable
    public BlockState overrideGetPlacementState(BlockPlaceContext pContext) {
        return getBlockPlacer().mimicGetPlacementState(pContext, placeable);
    }

    public InteractionResult overrideUseOn(UseOnContext pContext, FoodProperties foodProperties) {
        return getBlockPlacer().mimicUseOn(pContext, placeable, foodProperties);
    }

    public InteractionResult overridePlace(BlockPlaceContext pContext) {
        return getBlockPlacer().mimicPlace(pContext, placeable, null);
    }

    @Nullable
    public BlockPlaceContext overrideUpdatePlacementContext(BlockPlaceContext context) {
        return null;
    }

    public Block getPlacedBlock() {
        return placeable;
    }
}
