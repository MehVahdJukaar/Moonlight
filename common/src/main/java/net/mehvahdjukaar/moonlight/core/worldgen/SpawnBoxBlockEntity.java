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
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
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
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putString("name", this.targetName);
        output.putInt("posX", this.boxOffset.getX());
        output.putInt("posY", this.boxOffset.getY());
        output.putInt("posZ", this.boxOffset.getZ());
        output.putInt("sizeX", this.boxSize.getX());
        output.putInt("sizeY", this.boxSize.getY());
        output.putInt("sizeZ", this.boxSize.getZ());
        output.putBoolean("showBB", this.showBoundingBox);
        output.putString("final_state", this.finalState);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.targetName = input.getStringOr("name", "");
        this.boxOffset = new BlockPos(
                Mth.clamp(input.getIntOr("posX", 0), -48, 48),
                Mth.clamp(input.getIntOr("posY", 0), -48, 48),
                Mth.clamp(input.getIntOr("posZ", 0), -48, 48));
        this.boxSize = new Vec3i(
                Mth.clamp(input.getIntOr("sizeX", 0), 0, 48),
                Mth.clamp(input.getIntOr("sizeY", 0), 0, 48),
                Mth.clamp(input.getIntOr("sizeZ", 0), 0, 48));
        this.showBoundingBox = input.getBooleanOr("showBB", false);
        this.finalState = input.getStringOr("final_state", "");
    }

    public static Vec3i readBoxSize(CompoundTag tag) {
        int l = Mth.clamp(tag.getIntOr("sizeX", 0), 0, 48);
        int m = Mth.clamp(tag.getIntOr("sizeY", 0), 0, 48);
        int n = Mth.clamp(tag.getIntOr("sizeZ", 0), 0, 48);
        return new Vec3i(l, m, n);
    }

    public static BlockPos readOffsetPos(CompoundTag tag) {
        int i = Mth.clamp(tag.getIntOr("posX", 0), -48, 48);
        int j = Mth.clamp(tag.getIntOr("posY", 0), -48, 48);
        int k = Mth.clamp(tag.getIntOr("posZ", 0), -48, 48);
        return new BlockPos(i, j, k);
    }

    public static String readBoxName(CompoundTag nbt) {
        return nbt.getStringOr("name", "");
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
