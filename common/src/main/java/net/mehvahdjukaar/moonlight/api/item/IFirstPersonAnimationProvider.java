package net.mehvahdjukaar.moonlight.api.item;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * Implement in an item to allow it to be displayed with a custom animation using provided method callback
 * Will be called before the item actually gets rendered
 */
public interface IFirstPersonAnimationProvider {

    void animateItemFirstPerson(final LivingEntity entity, final ItemStack stack, final InteractionHand hand, final PoseStack matrixStack,
                                float partialTicks, float pitch, float attackAnim, float handHeight);
}
