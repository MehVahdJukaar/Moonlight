package net.mehvahdjukaar.moonlight.core.mixins.fabric;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.shaders.Program;
import net.minecraft.FileUtil;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.io.InputStream;

@Mixin(value = ShaderInstance.class, priority = 10) //very high priority
public class ShaderInstanceMixin {

    @WrapOperation(method = "<init>", at = @At(value = "NEW",
            target = "(Ljava/lang/String;)Lnet/minecraft/resources/ResourceLocation;"))
    private ResourceLocation moonlight$namespacedShader(String location,
                                                        Operation<ResourceLocation> original,
                                                        @Local(argsOnly = true) String name) {
        if (name.contains("moonlight_marker")) {
            var res = new ResourceLocation(name.replace("moonlight_marker", ":"));
            String namespace = res.getNamespace();
            String path = res.getPath();
            return new ResourceLocation(namespace, "shaders/core/" + path + ".json");

        }
        return original.call(location);
    }

    @ModifyVariable(method = "getOrCreate", at = @At("LOAD"), ordinal = 1)
    private static String moonlight$fixResourceLocation(String name, @Local(argsOnly = true) Program.Type programType) {
        if (name.contains(":")) {
            var loc = new ResourceLocation(name);
            String path = loc.getPath();
            return "shaders/core/" + path + programType.getExtension();
        }
        return name;
    }

    @WrapOperation(method = "getOrCreate", at = @At(value = "NEW", target = "(Ljava/lang/String;)Lnet/minecraft/resources/ResourceLocation;"))
    private static ResourceLocation moonlight$namespacedShader(String location,
                                                                Operation<ResourceLocation> original,
                                                                @Local(argsOnly = true) String name,
                                                                @Local(argsOnly = true) Program.Type programType) {
        if (name.contains(":")) {
            var loc = new ResourceLocation(name);
            String path = loc.getPath();
            String s = "shaders/core/" + path + programType.getExtension();
            return new ResourceLocation(loc.getNamespace(), s);
        }
        return original.call(location);
    }

}

