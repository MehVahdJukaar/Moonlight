package net.mehvahdjukaar.moonlight.core.mixins.fabric;

import net.mehvahdjukaar.moonlight.api.platform.fabric.ClientPlatformHelperImpl;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(ModelBakery.class)
public abstract class ModelBakeryMixin {

    @Shadow
    public abstract UnbakedModel getModel(ResourceLocation modelLocation);

    @Shadow
    @Final
    private Map<ResourceLocation, UnbakedModel> unbakedCache;

    @Shadow
    @Final
    private Map<ResourceLocation, UnbakedModel> topLevelModels;

    @Inject(method = "<init>", at = @At(value = "CONSTANT", args = "stringValue=minecraft:trident_in_hand#inventory", shift = At.Shift.AFTER),
            require = 1)
    public void init(ResourceManager resourceManager, BlockColors blockColors, ProfilerFiller profilerFiller, int i, CallbackInfo ci) {
        ClientPlatformHelperImpl.addSpecialModels(rl -> {
            UnbakedModel unbakedmodel = this.getModel(rl);
            this.unbakedCache.put(rl, unbakedmodel);
            this.topLevelModels.put(rl, unbakedmodel);
        });
    }
}
