package net.mehvahdjukaar.moonlight.api.client;

import net.mehvahdjukaar.moonlight.api.integration.IrisCompat;
import net.mehvahdjukaar.moonlight.core.CompatHandler;
import net.minecraft.client.renderer.ShaderInstance;

import java.util.function.Supplier;
public class CoreShaderContainer implements Supplier<ShaderInstance> {

    private final Supplier<ShaderInstance> vanillaFallback;
    private ShaderInstance instance;

    public CoreShaderContainer(Supplier<ShaderInstance> vanillaFallback) {
        this.vanillaFallback = vanillaFallback;
    }

    public void assign(ShaderInstance instance) {
        this.instance = instance;
    }

    @Override
    public ShaderInstance get() {
        if (CompatHandler.IRIS && IrisCompat.isIrisShaderFuckerActive()) {
            return vanillaFallback.get();
        }
        return instance;
    }
}
