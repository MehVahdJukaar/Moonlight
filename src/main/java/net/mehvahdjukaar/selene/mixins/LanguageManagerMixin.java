package net.mehvahdjukaar.selene.mixins;

import net.mehvahdjukaar.selene.resources.DynamicLanguageHandler;
import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.client.resources.language.LanguageInfo;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Map;

@Mixin(ClientLanguage.class)
public abstract class LanguageManagerMixin {

    @Unique
    private static ResourceManager cachedResourceManager;
    @Unique
    private static List<LanguageInfo> cachedLanguageInfo;


    @Inject(method = "loadFrom", at = @At("HEAD"))
    private static void loadFrom(ResourceManager pResourceManager, List<LanguageInfo> pLanguageInfo,
                                 CallbackInfoReturnable<ClientLanguage> cir) {
        cachedResourceManager = pResourceManager;
        cachedLanguageInfo = pLanguageInfo;
    }

    @ModifyArg(method = "loadFrom",
            at = @At(value = "INVOKE",
                    target = "Lcom/google/common/collect/ImmutableMap;copyOf(Ljava/util/Map;)Lcom/google/common/collect/ImmutableMap;"))
    private static Map<String, String> addEntries(Map<String, String> map) {
        DynamicLanguageHandler.addDynamicEntries(cachedResourceManager, cachedLanguageInfo, map);
        return map;
    }


}
