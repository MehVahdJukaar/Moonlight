package net.mehvahdjukaar.moonlight.api.client;

import com.google.common.base.Preconditions;
import net.mehvahdjukaar.moonlight.core.integration.IrisCompat;
import net.mehvahdjukaar.moonlight.core.CompatHandler;
import net.minecraft.client.renderer.ShaderInstance;
import org.jetbrains.annotations.Nullable;

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
        if (CompatHandler.IRIS && IrisCompat.isIrisShaderStuffActive()) {
            return vanillaFallback.get();
        }
        return Preconditions.checkNotNull(instance, "Shader {} was not assigned! How?!" + instance);
    }
}
