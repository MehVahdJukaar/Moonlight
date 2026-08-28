package net.mehvahdjukaar.moonlight.api.map.client;

import net.mehvahdjukaar.moonlight.api.map.decoration.MLMapDecoration;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

/**
 * Describes how a custom map decoration looks. Fills a MLDecorationRenderState in extract, Moonlight draws it.
 */
public class MapDecorationRenderer<T extends MLMapDecoration> {

    protected final Identifier textureId;

    public MapDecorationRenderer(Identifier texture) {
        this.textureId = texture;
    }

    public Identifier getDefaultSprite() {
        return this.textureId;
    }

    protected Identifier getSprite(T decoration) {
        return this.textureId;
    }

    protected int getColor(T decoration) {
        return -1;
    }

    protected boolean hasOutline(T decoration) {
        return false;
    }

    protected boolean rendersOnFrame(T decoration) {
        return true;
    }

    /**
     * Return false to skip this decoration this frame.
     */
    public boolean extract(T decoration, MapItemSavedData mapData, MLDecorationRenderState state) {
        state.sprite = MapDecorationClientManager.getSprite(this.getSprite(decoration));
        state.x = decoration.getX();
        state.y = decoration.getY();
        state.rot = decoration.getRot();
        state.color = this.getColor(decoration);
        state.outline = this.hasOutline(decoration);
        state.renderOnFrame = this.rendersOnFrame(decoration);
        state.name = decoration.getDisplayName();
        return true;
    }
}
