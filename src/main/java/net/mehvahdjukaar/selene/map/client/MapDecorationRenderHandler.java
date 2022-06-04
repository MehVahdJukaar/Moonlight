package net.mehvahdjukaar.selene.map.client;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.selene.map.CustomMapDecoration;
import net.mehvahdjukaar.selene.map.MapDecorationRegistry;
import net.mehvahdjukaar.selene.map.type.IMapDecorationType;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MapDecorationRenderHandler {

    private static final Map<IMapDecorationType<?, ?>, DecorationRenderer<?>> RENDERERS = Maps.newHashMap();

    //bindSimpleRenderer(MapDecorationRegistry.GENERIC_STRUCTURE_TYPE);

    //TODO: create atlas for markers for all textures on an atlas for simple renderers!!

    /**
     * Registers a renderer for this decoration. Use it to add fancy ones
     */
    public static <T extends CustomMapDecoration> void bindDecorationRenderer(IMapDecorationType<T, ?> type, DecorationRenderer<T> renderer) {
        if (RENDERERS.containsKey(type)) {
            throw new IllegalArgumentException("Duplicate map decoration renderer registration " + type.getId());
        } else {
            RENDERERS.put(type, renderer);
        }
    }

    /**
     * binds the default simple decoration renderer.<br>
     * will associate each decoration a texture based on its name<br>
     * texture location will be as follows:<br>
     * "textures/map/[type.id].png" under the namespace the decoration is registered under<br>
     * <p>
     * For more control use {@link MapDecorationRenderHandler#bindDecorationRenderer(IMapDecorationType, DecorationRenderer)}<br>
     */
    public static void bindSimpleRenderer(IMapDecorationType<?, ?> type) {
        ResourceLocation texture = new ResourceLocation(type.getId().getNamespace(), "textures/map/" + type.getId().getPath() + ".png");
        bindDecorationRenderer(type, new DecorationRenderer<>(texture));
    }

    //on pack reload
    public static void unbindRenderer(IMapDecorationType<?, ?> type) {
        RENDERERS.remove(type);
    }

    @ApiStatus.Internal
    public static <E extends CustomMapDecoration> DecorationRenderer<E> getRenderer(E decoration) {
        return (DecorationRenderer<E>) RENDERERS.get(decoration.getType());
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
