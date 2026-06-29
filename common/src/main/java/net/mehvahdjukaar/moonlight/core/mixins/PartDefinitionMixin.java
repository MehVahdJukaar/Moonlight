package net.mehvahdjukaar.moonlight.core.mixins;

import net.mehvahdjukaar.moonlight.api.client.model.IModelPartExtension;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.PartDefinition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PartDefinition.class)
public class PartDefinitionMixin {

    @Inject(method = "bake", at = @At("RETURN"))
    private void moonlight$onBakeRoot(int texWidth, int texHeight, CallbackInfoReturnable<ModelPart> cir) {
        ModelPart returnValue = cir.getReturnValue();
        ((IModelPartExtension) (Object) returnValue).moonlight$setDimensions(texWidth, texHeight);
    }
}
