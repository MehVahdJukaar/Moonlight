package net.mehvahdjukaar.moonlight.core.mixins.fabric;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ShaderInstance.class)
public class ShaderInstanceMixin {

    @WrapOperation(method = "<init>", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/resources/ResourceLocation;withDefaultNamespace(Ljava/lang/String;)Lnet/minecraft/resources/ResourceLocation;"))
    private ResourceLocation moonlight$namespacedShader(String location,
                                                        Operation<ResourceLocation> original,
                                                        @Local(argsOnly = true) String name) {
        if (name.contains("moonlight_marker")) {
            var res = ResourceLocation.parse(name.replace("moonlight_marker", ":"));
            String namespace = res.getNamespace();
            String path = res.getPath();
            return ResourceLocation.fromNamespaceAndPath(namespace, "shaders/core/" + path + ".json");

        }
        return original.call(location);
    }
}
