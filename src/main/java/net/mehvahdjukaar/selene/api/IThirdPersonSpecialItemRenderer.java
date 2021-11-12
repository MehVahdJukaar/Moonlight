package net.mehvahdjukaar.selene.api;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface IThirdPersonSpecialItemRenderer {

    /**
     * Use this interface to have more control of how your item is rendered in third person.
     * This is useful for items that should render relative to a body part that is not the hand like spyglass.
     * See ItemInHandLayer or PlayerItemInHandLayer classes for its vanilla default implementations
     * See this as a sort of enhanced third person only ISTER
     * Note that this only works for players (at least for now)
     *
     * @param parentModel  model of the entity that is rendering this item
     * @param entity       entity being rendered
     * @param stack        item stack
     * @param humanoidArm  can be either HumanoidLeft or HumanoidRight
     * @param poseStack    matrix stack
     * @param bufferSource buffer
     * @param light        combined light
     */
    <T extends Player, M extends EntityModel<T> & ArmedModel & HeadedModel> void renderThirdPersonItem(
            M parentModel, LivingEntity entity, ItemStack stack,
            HumanoidArm humanoidArm, PoseStack poseStack, MultiBufferSource bufferSource, int light);


}
