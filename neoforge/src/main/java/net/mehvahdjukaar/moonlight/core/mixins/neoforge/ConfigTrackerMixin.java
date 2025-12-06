package net.mehvahdjukaar.moonlight.core.mixins.neoforge;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.neoforged.fml.config.ConfigTracker;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ConfigTracker.class)
public class ConfigTrackerMixin {


    @WrapWithCondition(method = "openConfig",
            require = 1,
            at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V"))
    private static boolean ml$ignoreConfigWarning(Logger instance, String s, Object a, Object b,
                                                  @Local(argsOnly = true) ModConfig config) {
        //ignore
        return !Moonlight.getDependents().contains(config.getModId());
    }
}
