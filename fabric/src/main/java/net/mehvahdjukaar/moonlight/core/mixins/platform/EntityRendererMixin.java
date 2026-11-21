package net.mehvahdjukaar.moonlight.core.mixins.platform;


import net.mehvahdjukaar.moonlight.api.platform.platform.ClientHelperImpl;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

//same spot as neo
@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {

    @Inject(method = "createRenderState(Lnet/minecraft/world/entity/Entity;F)Lnet/minecraft/client/renderer/entity/state/EntityRenderState;",
            at = @At("RETURN"))
    private void moonlight$extractRenderData(Entity entity, float partialTicks, CallbackInfoReturnable<EntityRenderState> cir) {
        ClientHelperImpl.extractRenderData(entity, cir.getReturnValue(), partialTicks);
        //cuz fabric api has no evnet for the extract, just storage
    }


}
