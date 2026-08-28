package net.mehvahdjukaar.moonlight.core.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.mehvahdjukaar.moonlight.api.client.texture_renderer.MipmappedDynamicTexture;
import net.mehvahdjukaar.moonlight.core.ClientConfigs;
import net.minecraft.client.renderer.texture.DynamicTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Supplier;

// All require 0 because of Optishit. I should have not given in...
@Mixin(targets = "net/minecraft/client/resources/MapTextureManager$MapInstance", priority = 900)
public abstract class MapInstanceMixin {

    @WrapOperation(method = "<init>", require = 0,
            at = @At(value = "NEW", target = "(Ljava/util/function/Supplier;IIZ)Lnet/minecraft/client/renderer/texture/DynamicTexture;"))
    private DynamicTexture moonlight$mipmapMapTexture(Supplier<String> label, int width, int height, boolean clear,
                                                      Operation<DynamicTexture> original) {
        int maxMipLevel = ClientConfigs.MAPS_MIPMAP.get();
        if (maxMipLevel != 0) {
            return new MipmappedDynamicTexture(label, width, height, clear, maxMipLevel);
        }
        return original.call(label, width, height, clear);
    }
}
