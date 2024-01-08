package net.mehvahdjukaar.moonlight.api.item;

import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.core.misc.IExtendedItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Implement in an item to allow it to be displayed with a custom animation using provided method callback
 * Will be called before the item actually gets rendered
 * It is suggested to return ArmPose.SPYGLASS in your item getUseAnimation if you do not want to have the arm bob animation play
 * Alternatively you can simply call AnimationUtils.bobModelPart(model.leftArm, entity.tickCount, -1.0F); to "unbob" your arms, this is required for 2 handed animations
 */
public interface IThirdPersonAnimationProvider {

    /**
     * animate right hand
     *
     * @param stack    itemstack in hand
     * @param model    entity model. Can be cast to BipedModel
     * @param entity   entity
     * @param mainHand hand side
     * @return True if default animation should be skipped
     */
    <T extends LivingEntity> boolean poseRightArm(ItemStack stack, HumanoidModel<T> model, T entity, HumanoidArm mainHand);


    /**
     * animate left hand
     *
     * @param stack    itemstack in hand
     * @param model    entity model. Can be cast to BipedModel
     * @param entity   entity
     * @param mainHand hand side
     * @return True if default animation should be skipped
     */
    <T extends LivingEntity> boolean poseLeftArm(ItemStack stack, HumanoidModel<T> model, T entity, HumanoidArm mainHand);


    /**
     * Controls weather the other hand item renders or not
     */
    default boolean isTwoHanded() {
        return false;
    }


    /**
     * Alternatively, if you don't own the item and cant implement this interface in it you can use this call to attach your interface to an item
     * Note that when using other any of these 3 extensions only 1 object can be attached to any item, so be sure what you attach implements all of them
     */
    static void attachToItem(Item target, IThirdPersonAnimationProvider object) {
        if (PlatHelper.getPhysicalSide().isClient()) {
            IExtendedItem extendedItem = (IExtendedItem) target;
            if (extendedItem.moonlight$getClientAnimationExtension() != null) {
                if (PlatHelper.isDev())
                    throw new AssertionError("A client animation extension was already registered for this item");
            }
            extendedItem.moonlight$setClientAnimationExtension(object);
        }
    }

    static IThirdPersonAnimationProvider get(Item target) {
        if (target instanceof IThirdPersonAnimationProvider p) return p;
        if (((IExtendedItem) target).moonlight$getClientAnimationExtension() instanceof IThirdPersonAnimationProvider p)
            return p;
        return null;
    }

}
