package net.mehvahdjukaar.moonlight.api.integration;

import net.irisshaders.iris.Iris;
import net.irisshaders.iris.pipeline.ShaderRenderingPipeline;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;

public class IrisCompat {

    public static boolean isIrisShaderFuckerActive() {
        WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();
        return pipeline instanceof ShaderRenderingPipeline s && s.shouldOverrideShaders();
    }

}
