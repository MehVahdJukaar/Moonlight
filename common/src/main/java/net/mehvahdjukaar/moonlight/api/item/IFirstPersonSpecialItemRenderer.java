package net.mehvahdjukaar.moonlight.api.item;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.core.misc.IExtendedItem;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Injected early, allows canceling most of vanilla code.
 * Use if you want to render the arm with item or similar (like vanilla maps)
 */
public interface IFirstPersonSpecialItemRenderer {

    /**
     * Implement if you want to also override the item renderer code
     *
     * @return true to cancel original item renderer
     */
    boolean renderFirstPersonItem(final AbstractClientPlayer player, final ItemStack stack, final HumanoidArm arm, final PoseStack poseStack,
                                  float partialTicks, float pitch, float attackAnim, float equipAnim,
                                  MultiBufferSource buffer, int light, ItemInHandRenderer renderer);
//TODO: replace all interaction hands with HumanoidHand!!
    /**
     * Alternatively, if you don't own the item and cant implement this interface in it you can use this call to attach your interface to an item
     * Note that when using other any of these 3 extensions only 1 object can be attached to any item, so be sure what you attach implements all of them
     */
    static void attachToItem(Item target, IFirstPersonSpecialItemRenderer object) {
        if (PlatHelper.getPhysicalSide().isClient()) {
            IExtendedItem extendedItem = (IExtendedItem) target;
            if (extendedItem.moonlight$getClientAnimationExtension() != null) {
                if (PlatHelper.isDev())
                    throw new AssertionError("A client animation extension was already registered for this item");
            }
            extendedItem.moonlight$setClientAnimationExtension(object);
        }
    }

    static IFirstPersonSpecialItemRenderer get(Item target) {
        if (target instanceof IFirstPersonSpecialItemRenderer p) return p;
        if (((IExtendedItem) target).moonlight$getClientAnimationExtension() instanceof IFirstPersonSpecialItemRenderer p)
            return p;
        return null;
    }
}
