package net.mehvahdjukaar.moonlight.core.mixins;

import net.mehvahdjukaar.moonlight.api.map.client.MLDecorationRenderState;
import net.mehvahdjukaar.moonlight.core.misc.IMapRenderStateExtension;
import net.minecraft.client.renderer.state.MapRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.List;

@Mixin(MapRenderState.class)
public abstract class MapRenderStateMixin implements IMapRenderStateExtension {

    @Unique
    private final List<MLDecorationRenderState> moonlight$customDecorations = new ArrayList<>();

    @Override
    public List<MLDecorationRenderState> moonlight$getCustomDecorations() {
        return moonlight$customDecorations;
    }
}
