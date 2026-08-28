package net.mehvahdjukaar.moonlight.api.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.FallingBlockRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

public class FallingBlockRendererGeneric<T extends FallingBlockEntity> extends EntityRenderer<T, FallingBlockRenderState> {

    public FallingBlockRendererGeneric(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.5F;
    }

    @Override
    public FallingBlockRenderState createRenderState() {
        return new FallingBlockRenderState();
    }

    @Override
    public boolean shouldRender(T entity, Frustum culler, double camX, double camY, double camZ) {
        if (!super.shouldRender(entity, culler, camX, camY, camZ)) return false;
        BlockState state = entity.getBlockState();
        if (state.getRenderShape() != RenderShape.MODEL) return false;
        // just spawned ones still overlap their block and would z fight
        return state != entity.level().getBlockState(entity.blockPosition());
    }

    @Override
    public void extractRenderState(T entity, FallingBlockRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        BlockPos pos = BlockPos.containing(entity.getX(), entity.getBoundingBox().maxY, entity.getZ());
        var block = state.movingBlockRenderState;
        block.randomSeedPos = entity.getStartPos();
        block.blockPos = pos;
        block.blockState = entity.getBlockState();
        if (entity.level() instanceof ClientLevel clientLevel) {
            block.biome = clientLevel.getBiome(pos);
            block.cardinalLighting = clientLevel.cardinalLighting();
            block.lightEngine = clientLevel.getLightEngine();
        }
    }

    @Override
    public void submit(FallingBlockRenderState state, PoseStack poseStack, SubmitNodeCollector collector,
                       CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(-0.5, 0, -0.5);
        collector.submitMovingBlock(poseStack, state.movingBlockRenderState);
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }
}
