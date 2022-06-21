package net.mehvahdjukaar.selene.impl.blocks;

import net.mehvahdjukaar.selene.api.IBlockHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.ModelDataManager;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;

import java.util.Objects;


public abstract class MimicBlockTile extends BlockEntity implements IBlockHolder {

    public BlockState mimic = Blocks.AIR.defaultBlockState();
    public static final ModelProperty<BlockState> MIMIC = new ModelProperty<>();


    public MimicBlockTile(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public IModelData getModelData() {
        //return data;
        return new ModelDataMap.Builder()
                .withInitial(MIMIC, this.getHeldBlock())
                .build();
    }

    @Override
    public BlockState getHeldBlock(int index) {
        return this.mimic;
    }

    @Override
    public boolean setHeldBlock(BlockState state, int index) {
        this.mimic = state;
        return true;
    }


    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        this.mimic = NbtUtils.readBlockState(compound.getCompound("Mimic"));
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag) {
        super.saveAdditional(compoundTag);
        compoundTag.put("Mimic", NbtUtils.writeBlockState(mimic));
    }


    //client
    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        BlockState oldMimic = this.mimic;
        CompoundTag tag = pkt.getTag();
        handleUpdateTag(tag);
        if (!Objects.equals(oldMimic, this.mimic)) {
            ModelDataManager.requestModelDataRefresh(this);
            this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS | Block.UPDATE_NEIGHBORS);
        }
    }

    // The getUpdatePacket()/onDataPacket() pair is used when a block update happens on the client
    // (a blockstate change or an explicit notificiation of a block update from the server). It's
    // easiest to implement them based on getUpdateTag()/handleUpdateTag()

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    // The getUpdateTag()/handleUpdateTag() pair is called whenever the client receives a new chunk
    // it hasn't seen before. i.e. the chunk is loaded

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }
}
