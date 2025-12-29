package net.mehvahdjukaar.moonlight.core.mixins.fabric;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.FileUtil;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net.minecraft.client.renderer.ShaderInstance$1")
public class NonsenseMixin {

    @Shadow
    @Final
    String val$relativePath;

    @WrapOperation(method = "applyImport", at = @At(value = "INVOKE", target = "Lnet/minecraft/FileUtil;normalizeResourcePath(Ljava/lang/String;)Ljava/lang/String;"))
    private String moonlight$identity(String path, Operation<String> original, @Local(argsOnly = true) boolean isRelative,
                                      @Local(ordinal = 0, argsOnly = true) String directory) {
        if (directory.contains(":")) {
            ResourceLocation loc = ResourceLocation.tryParse(directory);
            if (loc != null) {
                String normalised = FileUtil.normalizeResourcePath((isRelative ? this.val$relativePath : "shaders/include/") + loc.getPath());
                return new ResourceLocation(loc.getNamespace(), normalised).toString();
            }
        }
        return original.call(path);
    }
}
