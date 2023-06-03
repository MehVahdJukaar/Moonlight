package net.mehvahdjukaar.moonlight.core.mixins;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.mehvahdjukaar.moonlight.api.client.model.LazyBlockstateDefinition;
import net.minecraft.client.renderer.block.model.BlockModelDefinition;
import net.minecraft.client.renderer.block.model.Variant;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockModelDefinition.Context.class)
public class BlockModelDefinitionMixin {

    @Mutable
    @Shadow @Final protected Gson gson;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void addSerializer(CallbackInfo ci){
        this.gson = gson.newBuilder().registerTypeAdapter(LazyBlockstateDefinition.class, new LazyBlockstateDefinition.Deserializer()).create();
    }
}
