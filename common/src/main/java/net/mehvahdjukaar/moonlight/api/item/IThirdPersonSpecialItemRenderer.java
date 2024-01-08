package net.mehvahdjukaar.moonlight.api.item;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.core.misc.IExtendedItem;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
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


    /**
     * Alternatively, if you don't own the item and cant implement this interface in it you can use this call to attach your interface to an item
     * Note that when using other any of these 3 extensions only 1 object can be attached to any item, so be sure what you attach implements all of them
     */
    static void attachToItem(Item target, IThirdPersonSpecialItemRenderer object) {
        if (PlatHelper.getPhysicalSide().isClient()) {
            IExtendedItem extendedItem = (IExtendedItem) target;
            if (extendedItem.moonlight$getClientAnimationExtension() != null) {
                if (PlatHelper.isDev())
                    throw new AssertionError("A client animation extension was already registered for this item");
            }
            extendedItem.moonlight$setClientAnimationExtension(object);
        }
    }

    static IThirdPersonSpecialItemRenderer get(Item target) {
        if (target instanceof IThirdPersonSpecialItemRenderer p) return p;
        if (((IExtendedItem) target).moonlight$getClientAnimationExtension() instanceof IThirdPersonSpecialItemRenderer p)
            return p;
        return null;
    }

}
