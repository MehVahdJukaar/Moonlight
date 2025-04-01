package net.mehvahdjukaar.moonlight.core.mixins.fabric;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.mehvahdjukaar.moonlight.api.platform.fabric.ClientHelperImpl;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.Consumer;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Inject(method = "reloadShaders", at = @At(value = "INVOKE",
            ordinal = 52,
            shift = At.Shift.AFTER,
            target = "Ljava/util/List;add(Ljava/lang/Object;)Z"))
    public void moonlight$registerShaders(ResourceProvider resourceProvider, CallbackInfo ci,
                                          @Local(ordinal = 1) List<Pair<ShaderInstance, Consumer<ShaderInstance>>> list) {
        ClientHelper.ShaderEvent event = (id, vertexFormat, setter) -> {
            try {
                ShaderInstance shader = new ShaderInstance(resourceProvider, id.toString()
                        .replace(":", "moonlight_marker"), vertexFormat);
                list.add(Pair.of(shader, setter));
            } catch (Exception e) {
                throw new RuntimeException("Failed to load shader: " + id, e);
            }
        };
        ClientHelperImpl.SHADER_REGISTRATIONS.forEach(l -> l.accept(event));
        ClientHelperImpl.SHADER_REGISTRATIONS.clear();

    }
}
