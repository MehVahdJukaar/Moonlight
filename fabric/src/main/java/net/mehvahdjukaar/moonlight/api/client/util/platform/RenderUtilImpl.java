package net.mehvahdjukaar.moonlight.api.client.util.platform;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.moonlight.api.client.util.RenderUtil;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class RenderUtilImpl {

    public static void renderBlockInWorld(BakedModel model, long seed, PoseStack poseStack, MultiBufferSource buffer, BlockState state, Level level, BlockPos pos, BlockRenderDispatcher blockRenderer) {
        blockRenderer.getModelRenderer().tesselateBlock(level, blockRenderer.getBlockModel(state), state, pos, poseStack, buffer.getBuffer(ItemBlockRenderTypes.getMovingBlockRenderType(state)),
                false, RandomSource.create(), seed, OverlayTexture.NO_OVERLAY);
    }

    public static void renderBlockDetached(BakedModel model, BlockState state, PoseStack poseStack, MultiBufferSource buffer,
                                           int light, int overlay) {
        RandomSource random = RandomSource.create();
        RenderUtil.putBlockQuads(poseStack.last(), buffer.getBuffer(ItemBlockRenderTypes.getMovingBlockRenderType(state)),
                state, dir -> {
                    random.setSeed(42L);
                    return model.getQuads( state, dir, random);
                }, light, overlay);
    }

    public static BakedModel handleCameraTransforms(BakedModel model, PoseStack matrixStack, ItemDisplayContext pTransformType) {
        model.getTransforms().getTransform(pTransformType).apply(false, matrixStack);
        return model;
    }

}
