package net.mehvahdjukaar.moonlight.api.map.client;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.mehvahdjukaar.moonlight.api.map.CustomMapDecoration;
import net.mehvahdjukaar.moonlight.api.map.type.MapDecorationType;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class MapDecorationClientHandler {

    /**
     * Registers a renderer for this decoration. Use it to add fancy ones
     */
    public static <T extends CustomMapDecoration> void registerCustomRenderer(MapDecorationType<T, ?> type, DecorationRenderer<T> renderer) {
        RENDERERS.put(type, renderer);
    }

    private static final Map<MapDecorationType<?, ?>, DecorationRenderer<?>> RENDERERS = Maps.newHashMap();


    private static <T extends CustomMapDecoration> DecorationRenderer<T> simpleRenderer(MapDecorationType<T, ?> type) {
        var id = Utils.getID(type);
        ResourceLocation texture = new ResourceLocation(id.getNamespace(), "map_markers/" + id.getPath());
        return new DecorationRenderer<>(texture);
    }

    public static <E extends CustomMapDecoration> DecorationRenderer<E> getRenderer(E decoration) {
        return (DecorationRenderer<E>) getRenderer(decoration.getType());
    }

    public static <E extends CustomMapDecoration, T extends MapDecorationType<E,?>> DecorationRenderer<E> getRenderer(T type) {
        return (DecorationRenderer<E>) RENDERERS.computeIfAbsent(type, t -> simpleRenderer(type));
    }

    public static <T extends CustomMapDecoration> boolean render(T decoration, PoseStack matrixStack,
                                                                 MultiBufferSource buffer,
                                                                 @Nullable MapItemSavedData mapData,
                                                                 boolean isOnFrame, int light, int index) {
        DecorationRenderer<T> renderer = getRenderer(decoration);
        if (renderer != null) {
            return renderer.render(decoration, matrixStack, buffer, mapData, isOnFrame, light, index);
        }
        return false;
    }


}
