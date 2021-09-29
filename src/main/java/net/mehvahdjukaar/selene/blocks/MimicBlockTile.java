package net.mehvahdjukaar.selene.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.client.model.ModelDataManager;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.common.util.Constants;

import java.util.Objects;


public abstract class MimicBlockTile extends TileEntity implements IBlockHolder {

    public BlockState mimic = Blocks.AIR.defaultBlockState();
    public static final ModelProperty<BlockState> MIMIC = new ModelProperty<>();


    public MimicBlockTile(TileEntityType<?> type) {
        super(type);
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
    public void load(BlockState state, CompoundNBT compound) {
        super.load(state, compound);
        this.mimic = NBTUtil.readBlockState(compound.getCompound("Mimic"));
    }

    @Override
    public CompoundNBT save(CompoundNBT compound) {
        super.save(compound);
        compound.put("Mimic", NBTUtil.writeBlockState(mimic));

        return compound;
    }

    //client
    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        BlockState oldMimic = this.mimic;
        CompoundNBT tag = pkt.getTag();
        handleUpdateTag(this.getBlockState(), tag);
        if (!Objects.equals(oldMimic, this.mimic)) {
            ModelDataManager.requestModelDataRefresh(this);
            this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), Constants.BlockFlags.BLOCK_UPDATE + Constants.BlockFlags.NOTIFY_NEIGHBORS);
        }
    }

    // The getUpdatePacket()/onDataPacket() pair is used when a block update happens on the client
    // (a blockstate change or an explicit notificiation of a block update from the server). It's
    // easiest to implement them based on getUpdateTag()/handleUpdateTag()

    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(this.worldPosition, 0, this.getUpdateTag());
    }

    // The getUpdateTag()/handleUpdateTag() pair is called whenever the client receives a new chunk
    // it hasn't seen before. i.e. the chunk is loaded

    @Override
    public CompoundNBT getUpdateTag() {
        return this.save(new CompoundNBT());
    }
}