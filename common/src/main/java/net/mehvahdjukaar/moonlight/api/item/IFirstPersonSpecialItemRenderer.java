package net.mehvahdjukaar.moonlight.api.item;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.candlelight.api.ClientOnly;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Takes over first person item rendering, arm included, like vanilla maps do. Attach with ClientAnimationExtension.attach.
 */
public interface IFirstPersonSpecialItemRenderer {

    /** Return true to cancel the vanilla item renderer. */
    @ClientOnly
    boolean renderFirstPersonItem(AbstractClientPlayer player, ItemStack stack, InteractionHand hand, HumanoidArm arm, PoseStack poseStack,
                                  float partialTicks, float pitch, float attackAnim, float equipAnim,
                                  SubmitNodeCollector submitNodeCollector, int light, ItemInHandRenderer renderer);

    @Nullable
    static IFirstPersonSpecialItemRenderer get(Item target) {
        if (target instanceof IFirstPersonSpecialItemRenderer p) return p;
        ClientAnimationExtension ext = ClientAnimationExtension.get(target);
        return ext == null ? null : ext.firstPersonRenderer();
    }
}
