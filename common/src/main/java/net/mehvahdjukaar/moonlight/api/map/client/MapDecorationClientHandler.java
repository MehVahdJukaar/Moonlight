package net.mehvahdjukaar.moonlight.api.map.client;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.moonlight.api.map.CustomMapDecoration;
import net.mehvahdjukaar.moonlight.api.map.type.MapDecorationType;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.ApiStatus;

import java.util.Map;

public class MapDecorationClientHandler {

    /**
     * Registers a renderer for this decoration. Use it to add fancy ones
     */
    public static <T extends CustomMapDecoration> void registerCustomRenderer(MapDecorationType<T, ?> type, DecorationRenderer<T> renderer) {
        RENDERERS.put(type, renderer);
    }

    private static final Map<MapDecorationType<?, ?>, DecorationRenderer<?>> RENDERERS = Maps.newHashMap();

    //TODO: merge atlas for markers for all textures on an atlas for simple renderers!!


    private static <T extends CustomMapDecoration> DecorationRenderer<T> simpleRenderer(MapDecorationType<T, ?> type) {
        var id = Utils.getID(type);
        ResourceLocation texture = new ResourceLocation(id.getNamespace(), "textures/map_markers/" + id.getPath() + ".png");
        return new DecorationRenderer<>(texture);
    }

    @ApiStatus.Internal
    public static <E extends CustomMapDecoration> DecorationRenderer<E> getRenderer(E decoration) {
        return (DecorationRenderer<E>) RENDERERS.computeIfAbsent(decoration.getType(),t-> simpleRenderer(decoration.getType()));
    }

    @ApiStatus.Internal
    public static <T extends CustomMapDecoration> boolean render(T decoration, PoseStack matrixStack, MultiBufferSource buffer, MapItemSavedData mapData, boolean isOnFrame, int light, int index) {
        DecorationRenderer<T> renderer = getRenderer(decoration);
        if (renderer != null) {
            return renderer.render(decoration, matrixStack, buffer, mapData, isOnFrame, light, index);
        }
        return false;
    }

}
