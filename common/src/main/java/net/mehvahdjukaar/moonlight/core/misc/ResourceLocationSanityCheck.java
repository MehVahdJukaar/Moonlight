package net.mehvahdjukaar.moonlight.core.misc;

import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.transformer.ClassInfo;

//can mods stop skipping resource locaiton validation?? It causes coutless issues for mods when actually invalid resource paths are shoved around FFS
public class ResourceLocationSanityCheck {

    public static void validateNamespace(String namespace, String path) {
        for (int i = 0; i < namespace.length(); i++) {
            char c = namespace.charAt(i);
            //no regex as thats a hot path
            if (!(c >= 'a' && c <= 'z' || c >= '0' && c <= '9' || c == '_' || c == '.' || c == '-')) {
                logAppliedMixins();
                throw new IllegalStateException("Invalid ResourceLocation namespace: " + namespace + ":" + path +
                        ". Some mod disabled vanilla id validation, check the mixins applied to ResourceLocation. This is VERY BAD!");
            }
        }
    }

    private static void logAppliedMixins() {
        try {
            ClassInfo info = ClassInfo.forName(ResourceLocation.class.getName());
            if (info == null) return;
            Moonlight.LOGGER.error("Mixins applied to ResourceLocation:");
            for (IMixinInfo mixin : info.getAppliedMixins()) {
                Moonlight.LOGGER.error(" - {} ({})", mixin.getClassName(), mixin.getConfig().getName());
            }
        }catch (Exception ignored){}
    }
}
