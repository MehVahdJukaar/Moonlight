package net.mehvahdjukaar.moonlight.api.map.client;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.mehvahdjukaar.moonlight.api.map.decoration.MLMapDecoration;
import net.mehvahdjukaar.moonlight.api.map.decoration.MLMapDecorationType;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.MapRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.TextureAtlasHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Function;

public class MapDecorationClientManager {

    public static final ResourceLocation LOCATION_MAP_MARKERS = ResourceLocation.withDefaultNamespace("textures/atlas/map_decorations.png");
    public static final RenderType MAP_MARKERS_RENDER_TYPE = RenderType.text(LOCATION_MAP_MARKERS);

    public MapDecorationClientManager() {
    }

    /**
     * Registers a renderer for this decoration. Use it to add fancy ones
     */
    public static <T extends MLMapDecoration> void registerCustomRenderer(ResourceLocation typeFactoryId, Function<ResourceLocation, MapDecorationRenderer<T>> renderer) {
        CUSTOM_RENDERERS_FACTORIES.put(typeFactoryId, (Function<ResourceLocation, MapDecorationRenderer<?>>) (Object) renderer);
    }

    private static final Map<ResourceLocation, Function<ResourceLocation, MapDecorationRenderer<?>>> CUSTOM_RENDERERS_FACTORIES = Maps.newHashMap();

    private static final Map<MLMapDecorationType<?, ?>, MapDecorationRenderer<?>> RENDERERS = Maps.newHashMap();


    private static <T extends MLMapDecoration> MapDecorationRenderer<T> createRenderer(MLMapDecorationType<T, ?> type) {
        var id = Utils.getID(type);
        ResourceLocation texture = id.withPath( "map_marker/" + id.getPath());
        var custom = CUSTOM_RENDERERS_FACTORIES.get(type.getCustomFactoryID());
        if (custom != null) return (MapDecorationRenderer<T>) custom.apply(texture);
        else return new MapDecorationRenderer<>(texture);
    }

    public static <E extends MLMapDecoration> MapDecorationRenderer<E> getRenderer(E decoration) {
        return (MapDecorationRenderer<E>) getRenderer(decoration.getType().value());
    }

    public static <E extends MLMapDecoration, T extends MLMapDecorationType<E, ?>> MapDecorationRenderer<E> getRenderer(T type) {
        return (MapDecorationRenderer<E>) RENDERERS.computeIfAbsent(type, t -> createRenderer(type));
    }

    public static <T extends MLMapDecoration> boolean render(T decoration, PoseStack matrixStack,
                                                             VertexConsumer vertexBuilder,
                                                             MultiBufferSource buffer,
                                                             @Nullable MapItemSavedData mapData,
                                                             boolean isOnFrame, int light, int index) {
        MapDecorationRenderer<T> renderer = getRenderer(decoration);
        if (renderer != null) {
            return renderer.render(decoration, matrixStack, vertexBuilder, buffer, mapData, isOnFrame, light, index);
        }
        return false;
    }

}
