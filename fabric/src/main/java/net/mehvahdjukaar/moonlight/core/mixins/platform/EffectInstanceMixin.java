package net.mehvahdjukaar.moonlight.core.mixins.platform;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.shaders.Program;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EffectInstance.class)
public class EffectInstanceMixin {

    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/resources/ResourceLocation;withDefaultNamespace(Ljava/lang/String;)Lnet/minecraft/resources/ResourceLocation;"))
    private ResourceLocation ml$allowModShaderRes(String string, Operation<ResourceLocation> original,
                                                  @Local(argsOnly = true) String name) {
        try {
            ResourceLocation id = ResourceLocation.tryParse(name);
            if (id != null && Moonlight.isDependant(id.getNamespace())) {
                return id.withPath("shaders/program/" + id.getPath() + ".json");
            }
        } catch (Exception e) {
            //ignore
        }
        return original.call(string);
    }

    @WrapOperation(method = "getOrCreate", at = @At(value = "INVOKE", target = "Lnet/minecraft/resources/ResourceLocation;withDefaultNamespace(Ljava/lang/String;)Lnet/minecraft/resources/ResourceLocation;"))
    private static ResourceLocation ml$allowModPostShaderRes(String string, Operation<ResourceLocation> original,
                                                             @Local(argsOnly = true) String name, @Local(argsOnly = true) Program.Type type) {
        try {
            ResourceLocation id = ResourceLocation.tryParse(name);
            if (id != null && Moonlight.isDependant(id.getNamespace())) {
                return id.withPath("shaders/program/" + id.getPath() + type.getExtension());
            }
        } catch (Exception e) {
            //ignore
        }
        return original.call(string);
    }
}
