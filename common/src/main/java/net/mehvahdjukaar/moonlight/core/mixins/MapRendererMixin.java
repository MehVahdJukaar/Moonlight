package net.mehvahdjukaar.moonlight.core.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.moonlight.api.client.util.RenderUtil;
import net.mehvahdjukaar.moonlight.api.map.ExpandedMapData;
import net.mehvahdjukaar.moonlight.api.map.client.MLDecorationRenderState;
import net.mehvahdjukaar.moonlight.api.map.client.MapDecorationClientManager;
import net.mehvahdjukaar.moonlight.api.map.decoration.MLMapDecoration;
import net.mehvahdjukaar.moonlight.core.ClientConfigs;
import net.mehvahdjukaar.moonlight.core.misc.IMapRenderStateExtension;
import net.minecraft.client.renderer.MapRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.MapRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(MapRenderer.class)
public abstract class MapRendererMixin {

    @Inject(method = "extractRenderState", at = @At("RETURN"))
    private void moonlight$extractCustomDecorations(MapId mapId, MapItemSavedData mapData, MapRenderState renderState, CallbackInfo ci) {
        List<MLDecorationRenderState> custom = ((IMapRenderStateExtension) renderState).moonlight$getCustomDecorations();
        custom.clear();
        if (mapData instanceof ExpandedMapData data) {
            for (MLMapDecoration decoration : data.ml$getCustomDecorations().values()) {
                MLDecorationRenderState state = new MLDecorationRenderState();
                if (MapDecorationClientManager.extract(decoration, mapData, state)) {
                    custom.add(state);
                }
            }
        }
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void moonlight$submitCustomDecorations(MapRenderState renderState, PoseStack poseStack, SubmitNodeCollector collector,
                                                   boolean showOnlyFrame, int lightCoords, CallbackInfo ci) {
        //above the vanilla ones so they don't z fight
        int index = renderState.decorations.size();
        for (MLDecorationRenderState state : ((IMapRenderStateExtension) renderState).moonlight$getCustomDecorations()) {
            MapDecorationClientManager.submit(state, poseStack, collector, showOnlyFrame, lightCoords, index++);
        }
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;scale(FFF)V", ordinal = 0))
    private void moonlight$transformVanillaDecoration(MapRenderState renderState, PoseStack poseStack, SubmitNodeCollector collector,
                                                      boolean showOnlyFrame, int lightCoords, CallbackInfo ci) {
        MapDecorationClientManager.applyTransformToSprite(poseStack);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;scale(FFF)V", ordinal = 1))
    private void moonlight$transformVanillaDecorationName(MapRenderState renderState, PoseStack poseStack, SubmitNodeCollector collector,
                                                          boolean showOnlyFrame, int lightCoords, CallbackInfo ci,
                                                          @Local MapRenderState.MapDecorationRenderState decoration) {
        MapDecorationClientManager.applyTransformToName(poseStack, decoration.name);
    }

    @WrapOperation(method = "render",
            at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/client/renderer/rendertype/RenderTypes;text(Lnet/minecraft/resources/Identifier;)Lnet/minecraft/client/renderer/rendertype/RenderType;"))
    private RenderType moonlight$mipmappedMapTexture(Identifier texture, Operation<RenderType> original) {
        if (ClientConfigs.MAPS_MIPMAP.get() != 0) {
            return RenderUtil.getTextMipmapRenderType(texture);
        }
        return original.call(texture);
    }
}
