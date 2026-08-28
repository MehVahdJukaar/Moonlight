package net.mehvahdjukaar.moonlight.api.map.client;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.mehvahdjukaar.moonlight.api.client.util.RenderUtil;
import net.mehvahdjukaar.moonlight.api.map.decoration.MLMapDecoration;
import net.mehvahdjukaar.moonlight.api.map.decoration.MLMapDecorationType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Holder;
import net.minecraft.data.AtlasIds;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Function;

public class MapDecorationClientManager {

    private static final Map<Identifier, Function<Identifier, MapDecorationRenderer<?>>> CUSTOM_RENDERERS_FACTORIES = Maps.newHashMap();
    private static final Map<MLMapDecorationType<?, ?>, MapDecorationRenderer<?>> RENDERERS = Maps.newHashMap();

    @Nullable
    private static DecorationTransform transform = null;

    /**
     * Registers a renderer for this decoration. Use it to add fancy ones
     */
    public static <T extends MLMapDecoration> void registerCustomRenderer(Identifier typeFactoryId, Function<Identifier, MapDecorationRenderer<T>> renderer) {
        CUSTOM_RENDERERS_FACTORIES.put(typeFactoryId, (Function<Identifier, MapDecorationRenderer<?>>) (Object) renderer);
    }

    private static MapDecorationRenderer<?> createRenderer(Holder<MLMapDecorationType<?, ?>> type) {
        Identifier id = type.unwrapKey().get().identifier();
        var custom = CUSTOM_RENDERERS_FACTORIES.get(type.value().getCustomFactoryID());
        if (custom != null) return custom.apply(id);
        else return new MapDecorationRenderer<>(id);
    }

    public static <E extends MLMapDecoration> MapDecorationRenderer<E> getRenderer(E decoration) {
        return getRenderer(decoration.getType());
    }

    public static <E extends MLMapDecoration> MapDecorationRenderer<E> getRenderer(Holder<MLMapDecorationType<?, ?>> type) {
        return (MapDecorationRenderer<E>) RENDERERS.computeIfAbsent(type.value(), t -> createRenderer(type));
    }

    public static TextureAtlasSprite getSprite(Identifier texture) {
        return Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.MAP_DECORATIONS).getSprite(texture);
    }

    /**
     * Extra transform applied to every map decoration, vanilla ones too. Set it around your own
     * MapRenderer calls, then clear it.
     */
    public static void setDecorationTransform(@Nullable DecorationTransform decorationTransform) {
        transform = decorationTransform;
    }

    public static void applyTransformToSprite(PoseStack poseStack) {
        if (transform != null) transform.applyToSprite(poseStack);
    }

    public static void applyTransformToName(PoseStack poseStack, @Nullable Component name) {
        if (transform == null || name == null) return;
        float width = Minecraft.getInstance().font.width(name);
        transform.applyToName(poseStack, width, nameScale(width));
    }

    private static float nameScale(float textWidth) {
        return Mth.clamp(25.0F / textWidth, 0.0F, 6.0F / 9.0F);
    }

    public static <T extends MLMapDecoration> boolean extract(T decoration, MapItemSavedData mapData, MLDecorationRenderState state) {
        MapDecorationRenderer<T> renderer = getRenderer(decoration);
        return renderer != null && renderer.extract(decoration, mapData, state);
    }

    /**
     * index is the stacking order, used to offset the sprite so markers don't z fight.
     */
    public static void submit(MLDecorationRenderState state, PoseStack poseStack, SubmitNodeCollector collector,
                              boolean showOnlyFrame, int light, int index) {
        if (showOnlyFrame && !state.renderOnFrame) return;

        poseStack.pushPose();
        poseStack.translate(state.x / 2.0F + 64.0F, state.y / 2.0F + 64.0F, -0.02F);
        poseStack.mulPose(Axis.ZP.rotationDegrees(state.rot * 360 / 16.0F));
        applyTransformToSprite(poseStack);
        poseStack.scale(4.0F, 4.0F, 3.0F);

        submitSprite(poseStack, collector, state.sprite, state.color, state.outline, light, index);
        poseStack.popPose();

        if (state.name != null) {
            submitName(state, poseStack, collector, light);
        }
    }

    public static void submitSprite(PoseStack poseStack, SubmitNodeCollector collector, @Nullable TextureAtlasSprite sprite,
                                    int color, boolean outline, int light, int index) {
        if (sprite == null || ARGB.alpha(color) == 0) return;

        poseStack.pushPose();
        poseStack.translate(0, 0, index * -0.001F);
        collector.submitCustomGeometry(poseStack, RenderTypes.text(sprite.atlasLocation()),
                (pose, buffer) -> RenderUtil.renderSprite(pose, buffer, light, color, sprite));

        if (outline) {
            //the silhouette render type paints the whole sprite shape, so a plain black ring
            var outlineType = RenderUtil.getColoredTextureRenderType(sprite.atlasLocation());
            int outlineColor = ARGB.color(ARGB.alpha(color), 0xFFFFFF);
            for (int j = -1; j <= 1; ++j) {
                for (int k = -1; k <= 1; ++k) {
                    if (j == 0 && k == 0) continue;
                    poseStack.pushPose();
                    poseStack.translate(j * 0.125F, k * 0.125F, 0.001F);
                    collector.submitCustomGeometry(poseStack, outlineType,
                            (pose, buffer) -> RenderUtil.renderSprite(pose, buffer, LightCoordsUtil.FULL_BRIGHT, outlineColor, sprite));
                    poseStack.popPose();
                }
            }
        }
        poseStack.popPose();
    }

    private static void submitName(MLDecorationRenderState state, PoseStack poseStack, SubmitNodeCollector collector, int light) {
        Font font = Minecraft.getInstance().font;
        Component name = state.name;
        float width = font.width(name);
        float scale = nameScale(width);

        poseStack.pushPose();
        poseStack.translate(state.x / 2.0F + 64.0F - width * scale / 2.0F, state.y / 2.0F + 64.0F + 4.0F, -0.025F);
        if (transform != null) transform.applyToName(poseStack, width, scale);
        poseStack.scale(scale, scale, -1.0F);
        poseStack.translate(0.0F, 0.0F, 0.1F);
        collector.order(1).submitText(poseStack, 0.0F, 0.0F, name.getVisualOrderText(), false,
                Font.DisplayMode.NORMAL, light, -1, Integer.MIN_VALUE, 0);
        poseStack.popPose();
    }

    public interface DecorationTransform {
        void applyToSprite(PoseStack poseStack);

        void applyToName(PoseStack poseStack, float textWidth, float textScale);
    }
}
