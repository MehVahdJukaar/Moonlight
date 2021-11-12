package net.mehvahdjukaar.selene.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.selene.api.IThirdPersonSpecialItemRenderer;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.PlayerItemInHandLayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerItemInHandLayer.class)
public abstract class PlayerItemInHandLayerMixin<T extends Player, M extends EntityModel<T> & ArmedModel & HeadedModel> extends ItemInHandLayer<T, M> {

    public PlayerItemInHandLayerMixin(RenderLayerParent<T, M> parent) {
        super(parent);
    }

    @Inject(method = "renderArmWithItem", at = @At(value = "HEAD"), cancellable = true)
    public void poseRightArm(LivingEntity entity, ItemStack stack, ItemTransforms.TransformType transformType,
                             HumanoidArm humanoidArm, PoseStack poseStack, MultiBufferSource bufferSource,
                             int light, CallbackInfo ci) {
        if (stack.getItem() instanceof IThirdPersonSpecialItemRenderer item) {
            item.renderThirdPersonItem(this.getParentModel(), entity, stack, humanoidArm, poseStack, bufferSource, light);
            ci.cancel();
        }

    }


}
