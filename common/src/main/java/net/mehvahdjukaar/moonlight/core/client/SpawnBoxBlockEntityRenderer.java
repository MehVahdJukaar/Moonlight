package net.mehvahdjukaar.moonlight.core.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.moonlight.core.worldgen.SpawnBoxBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.util.ARGB;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class SpawnBoxBlockEntityRenderer implements BlockEntityRenderer<SpawnBoxBlockEntity, SpawnBoxBlockEntityRenderer.State> {

    private static final int COLOR = ARGB.colorFromFloat(1f, 0.9f, 0.2f, 0.7f);

    public SpawnBoxBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    public static class State extends BlockEntityRenderState {
        @Nullable
        public AABB box;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(SpawnBoxBlockEntity blockEntity, State state, float partialTicks,
                                   Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        state.box = null;

        var player = Minecraft.getInstance().player;
        if (player == null || !(player.canUseGameMasterBlocks() || player.isSpectator())) return;
        if (!blockEntity.getShowBoundingBox()) return;

        Vec3i size = blockEntity.getSize();
        if (size.getX() < 1 || size.getY() < 1 || size.getZ() < 1) return;

        BlockPos offset = blockEntity.getBoxOffset();
        // a negative extent grows the box the other way, so it starts one block over
        double startX = size.getX() < 0 ? offset.getX() + 1.0 : offset.getX();
        double startZ = size.getZ() < 0 ? offset.getZ() + 1.0 : offset.getZ();
        state.box = new AABB(startX, offset.getY(), startZ,
                startX + size.getX(), offset.getY() + size.getY(), startZ + size.getZ());
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        AABB box = state.box;
        if (box == null) return;
        Gizmos.cuboid(box.move(state.blockPos), GizmoStyle.stroke(COLOR), true);
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 96;
    }
}
