package net.mehvahdjukaar.moonlight.core.worldgen;

import net.mehvahdjukaar.moonlight.api.MoonlightRegistry;
import net.mehvahdjukaar.moonlight.api.client.IScreenProvider;
import net.mehvahdjukaar.moonlight.core.client.SpawnBoxScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class SpawnBoxBlockEntity extends BlockEntity implements IScreenProvider {
    private BlockPos boxOffset = new BlockPos(0, 1, 0);
    private Vec3i boxSize = new Vec3i(3, 3, 3);
    private String targetName = "";
    private boolean showBoundingBox = true;
    private String finalState = "minecraft:air";

    public SpawnBoxBlockEntity(BlockPos pos, BlockState state) {
        super(MoonlightRegistry.SPAWN_BOX_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("name", this.targetName);
        tag.putInt("posX", this.boxOffset.getX());
        tag.putInt("posY", this.boxOffset.getY());
        tag.putInt("posZ", this.boxOffset.getZ());
        tag.putInt("sizeX", this.boxSize.getX());
        tag.putInt("sizeY", this.boxSize.getY());
        tag.putInt("sizeZ", this.boxSize.getZ());
        tag.putBoolean("showBB", this.showBoundingBox);
        tag.putString("final_state", this.finalState);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.targetName = readBoxName(tag);
        this.boxOffset = readOffsetPos(tag);
        this.boxSize = readBoxSize(tag);
        this.showBoundingBox = tag.getBoolean("showBB");
        this.finalState = tag.getString("final_state");
    }

    public static Vec3i readBoxSize(CompoundTag tag) {
        int l = Mth.clamp(tag.getInt("sizeX"), 0, 48);
        int m = Mth.clamp(tag.getInt("sizeY"), 0, 48);
        int n = Mth.clamp(tag.getInt("sizeZ"), 0, 48);
        return new Vec3i(l, m, n);
    }

    public static BlockPos readOffsetPos(CompoundTag tag) {
        int i = Mth.clamp(tag.getInt("posX"), -48, 48);
        int j = Mth.clamp(tag.getInt("posY"), -48, 48);
        int k = Mth.clamp(tag.getInt("posZ"), -48, 48);
        return new BlockPos(i, j, k);
    }

    public static String readBoxName(CompoundTag nbt) {
        return nbt.getString("name");
    }

    public String getFinalState() {
        return finalState;
    }

    public void setFinalState(String state) {
        this.finalState = state;
    }

    public boolean getShowBoundingBox() {
        return showBoundingBox;
    }

    public void setShowBoundingBox(boolean show) {
        this.showBoundingBox = show;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String name) {
        this.targetName = name;
    }

    public BlockPos getBoxOffset() {
        return boxOffset;
    }

    public void setBoxOffset(BlockPos pos) {
        this.boxOffset = pos;
    }

    public Vec3i getSize() {
        return boxSize;
    }

    public void setBoxSize(Vec3i size) {
        this.boxSize = size;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveCustomOnly(registries);
    }

    @Override
    public void openScreen(Level level, Player player, Direction direction, Vec3 hitPos) {
        SpawnBoxScreen.open(this);
    }

}
