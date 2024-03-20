package net.mehvahdjukaar.moonlight.api.block;

import net.mehvahdjukaar.moonlight.api.client.model.ExtraModelData;
import net.mehvahdjukaar.moonlight.api.client.model.IExtraModelDataProvider;
import net.mehvahdjukaar.moonlight.api.client.model.ModelDataKey;
import net.mehvahdjukaar.moonlight.api.client.util.LOD;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * A mix of ItemDisplayTile and DynamicRenderedBlockTile
 * A tile entity that is able to switch off and on its tile renderer based on distance from the camera
 * Should be used with a dynamic baked model that can react to the change in the extra model data to render with or without TESR
 */
public abstract class DynamicRenderedItemDisplayTile extends ItemDisplayTile implements IExtraModelDataProvider {

    public static final ModelDataKey<Boolean> IS_FANCY = DynamicRenderedBlockTile.IS_FANCY;

    // lod stuff (client)
    private boolean isFancy = false; // current
    private int extraFancyTicks = 0;

    protected DynamicRenderedItemDisplayTile(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state, int capacity) {
        super(tileEntityTypeIn, pos, state, capacity);
    }

    protected DynamicRenderedItemDisplayTile(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
        super(tileEntityTypeIn, pos, state);
    }

    @Override
    public void addExtraModelData(ExtraModelData.Builder builder) {
        builder.with(IS_FANCY, this.isFancy);
    }

    // Can be hooked on some configs to turn off the fancy renderer
    public abstract boolean isNeverFancy();

    public void onFancyChanged(boolean fancy) {
    }

    // use if you need this info in your tile anywhere. Shouldn't be needed in TESR
    public boolean rendersFancy() {
        return isFancy;
    }

    /**
     * Must be called in your TESR shouldRender method.
     *
     * @return true if the block should render with TESR
     */
    public boolean shouldRenderFancy(Vec3 cameraPos) {
        if (isNeverFancy()) return false;

        boolean newFancyStatus = getFancyDistance(cameraPos);
        boolean oldStatus = this.isFancy;
        if (oldStatus != newFancyStatus) {
            this.isFancy = newFancyStatus;
            onFancyChanged(isFancy);
            if (this.level == Minecraft.getInstance().level) {
                this.requestModelReload();
                this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), Block.UPDATE_IMMEDIATE);
            }
            if (!isFancy) extraFancyTicks = 4;
        }
        if (extraFancyTicks > 0) {
            extraFancyTicks--;
            return true;
        }
        // 1 tick delay
        return isFancy;
    }

    protected boolean getFancyDistance(Vec3 cameraPos) {
        LOD lod = new LOD(cameraPos, this.getBlockPos());
        return lod.isNear();
    }

}
