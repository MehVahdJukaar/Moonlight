package net.mehvahdjukaar.moonlight.api.entity;

import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

public class ImprovedFallingBlockEntity extends FallingBlockEntity {

    protected boolean saveTileDataToItem;

    public ImprovedFallingBlockEntity(EntityType<? extends FallingBlockEntity> type, Level level) {
        super(type, level);
        saveTileDataToItem = false;
    }

    public ImprovedFallingBlockEntity(EntityType<? extends FallingBlockEntity> type, Level level, BlockPos pos,
                                      BlockState blockState, boolean saveDataToItem) {
        super(type, level);
        this.blocksBuilding = true;
        this.xo = pos.getX() + 0.5D;
        this.yo = pos.getY();
        this.zo = pos.getZ() + 0.5D;
        this.setPos(xo, yo + ((1.0F - this.getBbHeight()) / 2.0F), zo);
        this.setDeltaMovement(Vec3.ZERO);
        this.setStartPos(this.blockPosition());
        this.setBlockState(blockState);
        this.saveTileDataToItem = saveDataToItem;
    }

    public static ImprovedFallingBlockEntity fall(EntityType<? extends FallingBlockEntity> type, Level level,
                                                  BlockPos pos, BlockState state, boolean saveDataToItem) {
        ImprovedFallingBlockEntity entity = new ImprovedFallingBlockEntity(type, level, pos, state,
                saveDataToItem);
        level.setBlock(pos, state.getFluidState().createLegacyBlock(), 3);
        level.addFreshEntity(entity);
        return entity;
    }

    public void setSaveTileDataToItem(boolean b) {
        this.saveTileDataToItem = b;
    }

    @Override
    public void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("saveToItem", this.saveTileDataToItem);
    }

    @Override
    public void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.saveTileDataToItem = input.getBooleanOr("saveToItem", false);
    }

    //workaround
    public void setBlockState(BlockState state) {
        if (state.hasProperty(BlockStateProperties.WATERLOGGED)) {
            state = state.setValue(BlockStateProperties.WATERLOGGED, false);
        }
        CompoundTag tag = new CompoundTag();
        tag.put("BlockState", NbtUtils.writeBlockState(state));
        tag.putInt("Time", this.time);
        this.readAdditionalSaveData(TagValueInput.create(ProblemReporter.DISCARDING, level().registryAccess(), tag));
    }

    // only ever called with this entity's own block
    @Override
    public ItemEntity spawnAtLocation(ServerLevel level, ItemStack stack, float yOffset) {
        if (this.saveTileDataToItem && this.blockData != null) {
            BlockEntity be = BlockEntity.loadStatic(BlockPos.ZERO, getBlockState(), blockData, level.registryAccess());
            if (be != null) stack.applyComponents(be.collectComponents());
            else Moonlight.LOGGER.warn("Failed to load block entity for falling block. Block Entity data: {}", blockData);
        }
        return super.spawnAtLocation(level, stack, yOffset);
    }

    @Override
    public boolean causeFallDamage(double fallDistance, float damageModifier, DamageSource source) {
        return super.causeFallDamage(fallDistance, damageModifier, source);
    }

    public void setCancelDrop(boolean cancelDrop) {
        this.cancelDrop = cancelDrop;
    }
}
