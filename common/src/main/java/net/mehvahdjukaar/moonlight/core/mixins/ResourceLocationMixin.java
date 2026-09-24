package net.mehvahdjukaar.moonlight.core.mixins;

import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.regex.Pattern;

@Mixin(Identifier.class)
public class ResourceLocationMixin {

    @Unique
    private static final Pattern ML$VALID_NAMESPACE = Pattern.compile("[a-z0-9_.-]*");

    @Inject(method = "<init>(Ljava/lang/String;Ljava/lang/String;)V", at = @At("RETURN"))
    private void moonlight$validateValidNamespace(String namespace, String path, CallbackInfo ci) {
        if (!ML$VALID_NAMESPACE.matcher(namespace).matches()) {
            throw new IllegalStateException("Invalid ResourceLocation namespace: " + namespace + ":" + path +
                    ". Some mod disabled vanilla id validation, check the mixins applied to ResourceLocation. This is VERY BAD!");
        }
    }
}
