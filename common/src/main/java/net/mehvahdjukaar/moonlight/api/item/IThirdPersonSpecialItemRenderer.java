package net.mehvahdjukaar.moonlight.api.item;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.candlelight.api.ClientOnly;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * A sort of third person only ISTER, for items that render relative to a body part that is not the hand, like the spyglass.
 * See ItemInHandLayer and PlayerItemInHandLayer for the vanilla default implementations.
 * Attach with ClientAnimationExtension.attach, or implement directly in a client only item class.
 * Only works for players, at least for now
 */
public interface IThirdPersonSpecialItemRenderer {

    @ClientOnly
    <S extends AvatarRenderState, M extends EntityModel<S> & ArmedModel<S> & HeadedModel> void renderThirdPersonItem(
            M parentModel, S state, ItemStack stack, HumanoidArm arm,
            PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int light);

    @Nullable
    static IThirdPersonSpecialItemRenderer get(Item target) {
        if (target instanceof IThirdPersonSpecialItemRenderer p) return p;
        ClientAnimationExtension ext = ClientAnimationExtension.get(target);
        return ext == null ? null : ext.thirdPersonRenderer();
    }

}
