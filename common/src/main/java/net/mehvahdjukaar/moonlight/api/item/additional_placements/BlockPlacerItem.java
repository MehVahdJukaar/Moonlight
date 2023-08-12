package net.mehvahdjukaar.moonlight.api.item.additional_placements;

import net.mehvahdjukaar.moonlight.core.misc.IExtendedItem;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import static net.mehvahdjukaar.moonlight.api.item.additional_placements.AdditionalItemPlacementsAPI.PLACEABLE_ITEMS;

//hacky registered item that handles placing placeable stuff
public final class BlockPlacerItem extends BlockItem {

    private FoodProperties mimicFood;
    private Block mimicBlock;
    private SoundType overrideSound;

    public BlockPlacerItem(Block pBlock, Properties pProperties) {
        super(pBlock, pProperties);
    }

    @Override
    public void registerBlocks(Map<Block, Item> pBlockToItemMap, Item pItem) {
        super.registerBlocks(pBlockToItemMap, pItem);
        for (var p : PLACEABLE_ITEMS) {
            AdditionalItemPlacement placement = p.getFirst().get();
            Item item = p.getSecond().get();
            Block block = placement.getPlacedBlock();
            if (item != null && item != Items.AIR && block != null && block != Blocks.AIR) {
                ((IExtendedItem) item).moonlight$addAdditionalBehavior(placement);
                pBlockToItemMap.put(block, item);
            }
        }
    }
    @Nullable
    public BlockState mimicGetPlacementState(BlockPlaceContext pContext, Block toPlace) {
        this.mimicBlock = toPlace;
        var r = getPlacementState(pContext);
        this.mimicBlock = null;
        return r;
    }

    public InteractionResult mimicUseOn(UseOnContext pContext, Block toPlace, FoodProperties foodProperties) {
        this.mimicFood = foodProperties;
        this.mimicBlock = toPlace;
        var r = super.useOn(pContext);
        this.mimicFood = null;
        this.mimicBlock = null;
        return r;
    }

    public InteractionResult mimicPlace(BlockPlaceContext pContext, Block toPlace, @Nullable SoundType overrideSound) {
        this.overrideSound = overrideSound;
        this.mimicBlock = toPlace;
        var r = super.place(pContext);
        this.overrideSound = null;
        this.mimicBlock = null;
        return r;
    }

    @Override
    public Block getBlock() {
        if (this.mimicBlock != null) return mimicBlock;
        return super.getBlock();
    }

    @Nullable
    @Override
    public FoodProperties getFoodProperties() {
        return mimicFood;
    }

    @Override
    public boolean isEdible() {
        return mimicFood != null;
    }

    @Override
    protected SoundEvent getPlaceSound(BlockState pState) {
        if (this.overrideSound != null) return this.overrideSound.getPlaceSound();
        return super.getPlaceSound(pState);
    }

    @Override
    public boolean canPlace(BlockPlaceContext pContext, BlockState pState) {
        this.mimicBlock = pState.getBlock();
        boolean r = super.canPlace(pContext, pState);
        this.mimicBlock = null;
        return r;
    }

}
