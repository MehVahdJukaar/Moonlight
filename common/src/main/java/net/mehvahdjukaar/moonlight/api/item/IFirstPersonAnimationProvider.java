package net.mehvahdjukaar.moonlight.api.item;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Transforms the item before it's rendered in first person.
 * Attach with ClientAnimationExtension.attach.
 * You probably want to return UseAnim.NONE in getUseAnimation to not have two animations at the same time
 */
public interface IFirstPersonAnimationProvider {

    void animateItemFirstPerson(Player entity, ItemStack stack, InteractionHand hand, HumanoidArm arm, PoseStack poseStack,
                                float partialTicks, float pitch, float attackAnim, float handHeight);
}
