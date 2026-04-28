package net.mehvahdjukaar.moonlight.core.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.moonlight.core.ClientConfigs;
import net.mehvahdjukaar.moonlight.core.commands.DebugRenderersCommand;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.debug.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DebugRenderer.class)
public class DebugRendererMixin {

    @Shadow
    @Final
    public GoalSelectorDebugRenderer goalSelectorRenderer;

    @Shadow
    @Final
    public DebugRenderer.SimpleDebugRenderer neighborsUpdateRenderer;

    @Shadow
    @Final
    public PathfindingRenderer pathfindingRenderer;

    @Shadow
    @Final
    public StructureRenderer structureRenderer;

    @Shadow
    @Final
    public DebugRenderer.SimpleDebugRenderer waterDebugRenderer;

    @Shadow
    @Final
    public DebugRenderer.SimpleDebugRenderer heightMapRenderer;

    @Shadow
    @Final
    public DebugRenderer.SimpleDebugRenderer collisionBoxRenderer;

    @Shadow
    @Final
    public DebugRenderer.SimpleDebugRenderer supportBlockRenderer;

    @Shadow
    @Final
    public DebugRenderer.SimpleDebugRenderer lightDebugRenderer;

    @Shadow
    @Final
    public BreezeDebugRenderer breezeDebugRenderer;

    @Shadow
    @Final
    public LightSectionDebugRenderer skyLightSectionDebugRenderer;

    @Shadow
    @Final
    public GameEventListenerRenderer gameEventListenerRenderer;

    @Shadow
    @Final
    public DebugRenderer.SimpleDebugRenderer solidFaceRenderer;

    @Shadow
    @Final
    public DebugRenderer.SimpleDebugRenderer worldGenAttemptRenderer;

    @Inject(method = "render", at = @At("TAIL"))
    public void supp$renderVanillaDebug(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, double camX, double camY, double camZ, CallbackInfo ci) {
        if (ClientConfigs.DEBUG_RENDERS.get()) {
            this.goalSelectorRenderer.render(poseStack, bufferSource, camX, camY, camZ);
            this.neighborsUpdateRenderer.render(poseStack, bufferSource, camX, camY, camZ);
            this.pathfindingRenderer.render(poseStack, bufferSource, camX, camY, camZ);
            this.structureRenderer.render(poseStack, bufferSource, camX, camY, camZ);
            if (DebugRenderersCommand.DEBUG_WATER)
                this.waterDebugRenderer.render(poseStack, bufferSource, camX, camY, camZ);
            if (DebugRenderersCommand.DEBUG_HEIGHTMAP)
                this.heightMapRenderer.render(poseStack, bufferSource, camX, camY, camZ);
            if (DebugRenderersCommand.DEBUG_COLLISION)
                this.collisionBoxRenderer.render(poseStack, bufferSource, camX, camY, camZ);
            if (DebugRenderersCommand.DEBUG_SUPPORT)
                this.supportBlockRenderer.render(poseStack, bufferSource, camX, camY, camZ);
            if (DebugRenderersCommand.DEBUG_LIGHT)
                this.lightDebugRenderer.render(poseStack, bufferSource, camX, camY, camZ);
            if (DebugRenderersCommand.DEBUG_WORLD_GEN_ATTEMPTS)
                this.worldGenAttemptRenderer.render(poseStack, bufferSource, camX, camY, camZ);
            if (DebugRenderersCommand.DEBUG_SOLID_FACES)
                this.solidFaceRenderer.render(poseStack, bufferSource, camX, camY, camZ);
            if (DebugRenderersCommand.DEBUG_GAME_EVENTS)
                this.gameEventListenerRenderer.render(poseStack, bufferSource, camX, camY, camZ);
            if (DebugRenderersCommand.DEBUG_SKY_LIGHT_SECTIONS)
                this.skyLightSectionDebugRenderer.render(poseStack, bufferSource, camX, camY, camZ);
            if (DebugRenderersCommand.DEBUG_BREEZE)
                this.breezeDebugRenderer.render(poseStack, bufferSource, camX, camY, camZ);
        }
    }
}
