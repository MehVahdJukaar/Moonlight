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
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.model.data.ModelData;

public class RenderUtilImpl {


    public static void renderBlock(BakedModel model, long seed, PoseStack matrixStack, MultiBufferSource buffer, BlockState blockstate,
                                   Level level, BlockPos blockpos, BlockRenderDispatcher dispatcher) {
        for (var renderType : model.getRenderTypes(blockstate, RandomSource.create(seed), ModelData.EMPTY)) {
            dispatcher.getModelRenderer().tesselateBlock(level, model, blockstate, blockpos, matrixStack, buffer.getBuffer(renderType), false, RandomSource.create(), seed,
                    OverlayTexture.NO_OVERLAY, ModelData.EMPTY, renderType);
        }
    }

    public static BakedModel handleCameraTransforms(BakedModel model, PoseStack poseStack, ItemDisplayContext transform) {
        return ClientHooks.handleCameraTransforms(poseStack, model, transform, false);
    }


}