package net.mehvahdjukaar.moonlight.api.item;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;

/**
 * A sort of third person only ISTER, for items that render relative to a body part that is not the hand, like the spyglass.
 * See ItemInHandLayer and PlayerItemInHandLayer for the vanilla default implementations.
 * Attach with ClientAnimationExtension.attach.
 * Only works for players, at least for now
 */
public interface IThirdPersonSpecialItemRenderer {

    void renderThirdPersonItem(HumanoidModel<AvatarRenderState> parentModel, AvatarRenderState state, ItemStack stack,
                               HumanoidArm arm, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int light);
}
