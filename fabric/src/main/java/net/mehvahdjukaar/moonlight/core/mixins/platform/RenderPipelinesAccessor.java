package net.mehvahdjukaar.moonlight.core.mixins.platform;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

// only registered pipelines get precompiled and validated, and fabric has no registration event
@Mixin(RenderPipelines.class)
public interface RenderPipelinesAccessor {

    @Accessor("PIPELINES_BY_LOCATION")
    static Map<Identifier, RenderPipeline> getPIPELINES_BY_LOCATION() {
        throw new UnsupportedOperationException();
    }
}
