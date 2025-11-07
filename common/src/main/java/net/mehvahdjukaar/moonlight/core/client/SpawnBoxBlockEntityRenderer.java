package net.mehvahdjukaar.moonlight.core.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.mehvahdjukaar.moonlight.core.worldgen.SpawnBoxBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;

public class SpawnBoxBlockEntityRenderer implements BlockEntityRenderer<SpawnBoxBlockEntity> {


    public SpawnBoxBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    public void render(SpawnBoxBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (Minecraft.getInstance().player.canUseGameMasterBlocks() || Minecraft.getInstance().player.isSpectator()) {
            if (blockEntity.getShowBoundingBox()) {

                BlockPos posOffset = blockEntity.getBoxOffset();
                Vec3i size = blockEntity.getSize();
                if (size.getX() >= 1 && size.getY() >= 1 && size.getZ() >= 1) {
                    double startX = posOffset.getX();
                    double startZ = posOffset.getZ();
                    double startY = posOffset.getY();
                    double endY = startY + size.getY();

                    double endX = size.getX();
                    double endZ = size.getZ();

                    double j = endX < 0.0 ? startX + 1.0 : startX;
                    double k = endZ < 0.0 ? startZ + 1.0 : startZ;
                    double l = j + endX;
                    double m = k + endZ;

                    VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.lines());
                    float red = 0.9F;
                    float green = 0.6F;
                    float blue = 0.0F;
                    LevelRenderer.renderLineBox(poseStack, vertexConsumer, j, startY, k, l, endY, m,
                            red, green, blue, 1.0F);
                }
            }
        }
    }

    @Override
    public boolean shouldRenderOffScreen(SpawnBoxBlockEntity blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 96;
    }
}