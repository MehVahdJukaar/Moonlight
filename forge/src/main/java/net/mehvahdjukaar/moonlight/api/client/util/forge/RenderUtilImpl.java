package net.mehvahdjukaar.moonlight.api.client.util.forge;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.client.model.data.ModelData;

public class RenderUtilImpl {


    public static void renderBlock(long seed, PoseStack matrixStack, MultiBufferSource buffer, BlockState blockstate,
                                   Level level, BlockPos blockpos, BlockRenderDispatcher dispatcher) {

        BakedModel model = dispatcher.getBlockModel(blockstate);
        for (var renderType : model.getRenderTypes(blockstate, RandomSource.create(seed), ModelData.EMPTY)) {
            dispatcher.getModelRenderer().tesselateBlock(level, model, blockstate, blockpos, matrixStack, buffer.getBuffer(renderType), false, RandomSource.create(), seed,
                    OverlayTexture.NO_OVERLAY, ModelData.EMPTY, renderType);
        }
    }

    public static void renderGuiItem(BakedModel model, ItemStack stack, ItemRenderer renderer, int combinedLight, int pCombinedOverlay, PoseStack poseStack,
                                     MultiBufferSource.BufferSource buffer, boolean flatItem) {

        poseStack.translate(-0.5D, -0.5D, -0.5D);

        if (!model.isCustomRenderer() && (!stack.is(Items.TRIDENT) || flatItem)) {
            boolean fabulous = true;

            for (var m : model.getRenderPasses(stack, fabulous)) {
                for (var renderType : m.getRenderTypes(stack, fabulous)) {

                    VertexConsumer vertexconsumer;
                    if (stack.is(Items.COMPASS) && stack.hasFoil()) {
                        poseStack.pushPose();
                        PoseStack.Pose pose = poseStack.last();
                        pose.pose().multiply(0.5F);

                        vertexconsumer = ItemRenderer.getCompassFoilBufferDirect(buffer, renderType, pose);

                        poseStack.popPose();
                    } else {
                        vertexconsumer = ItemRenderer.getFoilBufferDirect(buffer, renderType, true, stack.hasFoil());
                    }

                    renderer.renderModelLists(model, stack, combinedLight, pCombinedOverlay, poseStack, vertexconsumer);
                }
            }
        } else {
            IClientItemExtensions.of(stack).getCustomRenderer().renderByItem(stack, ItemTransforms.TransformType.GUI,
                    poseStack, buffer, combinedLight, pCombinedOverlay);
        }

        poseStack.popPose();
    }

    public static BakedModel handleCameraTransforms(BakedModel model, PoseStack poseStack, ItemTransforms.TransformType transform) {
        return ForgeHooksClient.handleCameraTransforms(poseStack, model, transform, false);
    }


}