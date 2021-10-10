package net.mehvahdjukaar.selene.blocks;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.ModelDataManager;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import java.util.Objects;


public abstract class MimicBlockTile extends BlockEntity {

    public BlockState mimic = Blocks.AIR.defaultBlockState();
    public static final ModelProperty<BlockState> MIMIC = new ModelProperty<>();


    public MimicBlockTile(BlockEntityType<?> type) {
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
    public void load(BlockState state, CompoundTag compound) {
        super.load(state, CompoundTag compound);
        this.mimic = NbtUtils.readBlockState(compound.getCompound("Mimic"));
    }


    public CompoundContainer save(CompoundContainer compound) {
        super.save(compound);
        compound.put("Mimic", NbtUtils.writeBlockState(mimic));

        return compound;
    }

    //client
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        BlockState oldMimic = this.mimic;
        CompoundTag tag = pkt.getTag();
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
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return new ClientboundBlockEntityDataPacket(this.worldPosition, 0, this.getUpdateTag());
    }

    // The getUpdateTag()/handleUpdateTag() pair is called whenever the client receives a new chunk
    // it hasn't seen before. i.e. the chunk is loaded

    @Override
    public CompoundTag getUpdateTag() {
        return this.save(new CompoundTag());
    }
}
