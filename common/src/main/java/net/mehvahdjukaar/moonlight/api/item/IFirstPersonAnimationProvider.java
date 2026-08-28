package net.mehvahdjukaar.moonlight.api.item;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.candlelight.api.ClientOnly;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Transforms the item before it's rendered in first person.
 * Attach with ClientAnimationExtension.attach, or implement directly in a client only item class.
 * You probably want to return UseAnim.NONE in getUseAnimation to not have two animations at the same time
 */
public interface IFirstPersonAnimationProvider {

    @ClientOnly
    void animateItemFirstPerson(Player entity, ItemStack stack, InteractionHand hand, HumanoidArm arm, PoseStack poseStack,
                                float partialTicks, float pitch, float attackAnim, float handHeight);

    @Nullable
    static IFirstPersonAnimationProvider get(Item target) {
        if (target instanceof IFirstPersonAnimationProvider p) return p;
        ClientAnimationExtension ext = ClientAnimationExtension.get(target);
        return ext == null ? null : ext.firstPersonAnimation();
    }
}
