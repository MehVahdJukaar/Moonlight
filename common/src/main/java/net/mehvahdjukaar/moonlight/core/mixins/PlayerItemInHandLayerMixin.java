package net.mehvahdjukaar.moonlight.core.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.moonlight.api.item.ClientAnimationExtension;
import net.mehvahdjukaar.moonlight.api.item.IThirdPersonSpecialItemRenderer;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.PlayerItemInHandLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerItemInHandLayer.class)
public abstract class PlayerItemInHandLayerMixin<S extends AvatarRenderState, M extends EntityModel<S> & ArmedModel<S> & HeadedModel>
        extends ItemInHandLayer<S, M> {

    protected PlayerItemInHandLayerMixin(RenderLayerParent<S, M> renderLayerParent) {
        super(renderLayerParent);
    }

    @SuppressWarnings("unchecked")
    @Inject(method = "submitArmWithItem*", at = @At(value = "HEAD"), cancellable = true)
    private void moonlight$specialThirdPersonRenderer(S state, ItemStackRenderState item, ItemStack stack, HumanoidArm arm,
                                                      PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
                                                      int light, CallbackInfo ci) {
        ClientAnimationExtension ext = ClientAnimationExtension.get(stack.getItem());
        IThirdPersonSpecialItemRenderer provider = ext == null ? null : ext.thirdPersonRenderer();
        if (provider != null && this.getParentModel() instanceof HumanoidModel<?> model) {
            provider.renderThirdPersonItem((HumanoidModel<AvatarRenderState>) model, state, stack, arm,
                    poseStack, submitNodeCollector, light);
            ci.cancel();
        }
    }

}
