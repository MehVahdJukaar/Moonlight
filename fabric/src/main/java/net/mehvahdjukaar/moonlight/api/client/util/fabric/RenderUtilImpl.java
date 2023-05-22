package net.mehvahdjukaar.moonlight.api.client.util.fabric;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.mehvahdjukaar.moonlight.core.mixins.fabric.ItemRendererAccessor;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class RenderUtilImpl {

    public static void renderBlock(long seed, PoseStack poseStack, MultiBufferSource buffer, BlockState state, Level level, BlockPos pos, BlockRenderDispatcher blockRenderer) {
        blockRenderer.getModelRenderer().tesselateBlock(level, blockRenderer.getBlockModel(state), state, pos, poseStack, buffer.getBuffer(ItemBlockRenderTypes.getMovingBlockRenderType(state)),
                false, RandomSource.create(), seed, OverlayTexture.NO_OVERLAY);
    }

    public static BakedModel handleCameraTransforms(BakedModel model, PoseStack matrixStack, ItemDisplayContext pTransformType) {
        model.getTransforms().getTransform(pTransformType).apply(false, matrixStack);
        return model;
    }

    public static void renderGuiItem(BakedModel model, ItemStack stack, ItemRenderer renderer, int combinedLight, int combinedOverlay, PoseStack poseStack, MultiBufferSource.BufferSource buffer, boolean flatItem) {

        poseStack.pushPose();

        poseStack.translate(-0.5, -0.5, -0.5);
        if (model.isCustomRenderer() || stack.is(Items.TRIDENT) && !flatItem) {
            ((ItemRendererAccessor) renderer).getBlockEntityRenderer().renderByItem(stack, ItemDisplayContext.GUI,
                    poseStack, buffer, combinedLight, combinedOverlay);
        } else {
            VertexConsumer vertexConsumer;
            boolean fabulous = true;
            RenderType renderType = ItemBlockRenderTypes.getRenderType(stack, fabulous);
            if (stack.is(ItemTags.COMPASSES) && stack.hasFoil()) {
                poseStack.pushPose();
                PoseStack.Pose pose = poseStack.last();

                pose.pose().scale(0.5f);

                vertexConsumer = ItemRenderer.getCompassFoilBufferDirect(buffer, renderType, pose);
                poseStack.popPose();
            } else {
                vertexConsumer = ItemRenderer.getFoilBufferDirect(buffer, renderType, true, stack.hasFoil());
            }
            renderer.renderModelLists(model, stack, combinedLight, combinedOverlay, poseStack, vertexConsumer);
        }
        poseStack.popPose();
    }


}
