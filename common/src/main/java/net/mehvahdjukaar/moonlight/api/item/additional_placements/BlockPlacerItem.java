package net.mehvahdjukaar.moonlight.api.item.additional_placements;

import net.mehvahdjukaar.moonlight.api.MoonlightRegistry;
import net.mehvahdjukaar.moonlight.api.misc.ForgeOverride;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

//hacky registered item that handles placing placeable stuff
public final class BlockPlacerItem extends BlockItem {

    public static BlockPlacerItem get(){
        return MoonlightRegistry.BLOCK_PLACER.get();
    }

    private FoodProperties mimicFood;
    private Block mimicBlock;
    private SoundType overrideSound;

    public BlockPlacerItem(Block pBlock, Properties pProperties) {
        super(pBlock, pProperties);
    }

    @Override
    public void registerBlocks(Map<Block, Item> pBlockToItemMap, Item pItem) {
      AdditionalItemPlacementsAPI.  onRegistryCallback(pBlockToItemMap);
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
    @ForgeOverride
    public FoodProperties getFoodProperties(ItemStack stack, @Nullable LivingEntity entity) {
        return mimicFood;
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

    @Override
    public String getDescriptionId() {
        return "x";
    }
}
