package net.mehvahdjukaar.moonlight.api.item;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.core.misc.IExtendedItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Implement in an item to allow it to be displayed with a custom animation using provided method callback
 * Will be called before the item actually gets rendered
 * You probably want to return UseAnim.NONE in item::getUseAnimation to not have two animations at the same time
 */
public interface IFirstPersonAnimationProvider {

    @Deprecated(forRemoval = true)
    default void animateItemFirstPerson(final LivingEntity entity, final ItemStack stack, final InteractionHand hand, final PoseStack matrixStack,
                                        float partialTicks, float pitch, float attackAnim, float handHeight) {
    }

    default void animateItemFirstPerson(final Player entity, final ItemStack stack, final HumanoidArm arm, final PoseStack poseStack,
                                        float partialTicks, float pitch, float attackAnim, float handHeight) {
        animateItemFirstPerson(entity, stack,
                arm == HumanoidArm.RIGHT ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND
                , poseStack, partialTicks, pitch, attackAnim, handHeight);
    }

    /**
     * Alternatively, if you don't own the item and cant implement this interface in it you can use this call to attach your interface to an item
     * Note that when using other any of these 3 extensions only 1 object can be attached to any item, so be sure what you attach implements all of them
     */
    static void attachToItem(Item target, IFirstPersonAnimationProvider object) {
        if (PlatHelper.getPhysicalSide().isClient()) {
            IExtendedItem extendedItem = (IExtendedItem) target;
            if (extendedItem.moonlight$getClientAnimationExtension() != null) {
                if (PlatHelper.isDev())
                    throw new AssertionError("A client animation extension was already registered for this item");
            }
            extendedItem.moonlight$setClientAnimationExtension(object);
        }
    }

    static IFirstPersonAnimationProvider get(Item target) {
        if (target instanceof IFirstPersonAnimationProvider p) return p;
        if (((IExtendedItem) target).moonlight$getClientAnimationExtension() instanceof IFirstPersonAnimationProvider p)
            return p;
        return null;
    }
}
