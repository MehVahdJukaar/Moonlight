package net.mehvahdjukaar.moonlight.core.misc;

import net.mehvahdjukaar.moonlight.api.map.client.MLDecorationRenderState;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

// vanilla's decoration state has no room for our tints and outlines, so we carry our own list
@ApiStatus.Internal
public interface IMapRenderStateExtension {

    List<MLDecorationRenderState> moonlight$getCustomDecorations();
}
