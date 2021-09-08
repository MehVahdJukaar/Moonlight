package net.mehvahdjukaar.selene.api;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.mehvahdjukaar.selene.util.TwoHandedAnimation;
import net.minecraft.client.renderer.entity.model.AgeableModel;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;

/**
 * Implement in an item to allow it to be displayed with a custom animation using provided method callback
 * Will be called before the item actually gets rendered
 */
public interface IThirdPersonAnimationProvider {

    /**
     * animate right hand
     * @param stack itemstack in hand
     * @param model entity model. Can be cast to BipedModel
     * @param entity entity
     * @param mainHand hand side
     * @param twoHanded set to true to skip off hand animation
     * @return True if default animation should be skipped
     */
     <T extends LivingEntity> boolean poseRightArm(ItemStack stack, BipedModel<T> model, T entity, HandSide mainHand, TwoHandedAnimation twoHanded);

    default <T extends LivingEntity> boolean poseRightArmGeneric(ItemStack stack, AgeableModel<T> model, T entity, HandSide mainHand, TwoHandedAnimation twoHanded){
        return poseRightArm(stack, (BipedModel)model, entity, mainHand, twoHanded);
    }

    /**
     * animate left hand
     * @param stack itemstack in hand
     * @param model entity model. Can be cast to BipedModel
     * @param entity entity
     * @param mainHand hand side
     * @return True if default animation should be skipped
     */
    <T extends LivingEntity> boolean poseLeftArm(ItemStack stack, BipedModel<T> model, T entity, HandSide mainHand,TwoHandedAnimation twoHanded);

    default <T extends LivingEntity> boolean poseLeftArmGeneric(ItemStack stack, AgeableModel<T> model, T entity, HandSide mainHand, TwoHandedAnimation twoHanded){
        return poseLeftArm(stack, (BipedModel)model, entity, mainHand, twoHanded);
    }

    default boolean isTwoHanded(){
        return false;
    }

}
