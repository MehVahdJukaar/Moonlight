package net.mehvahdjukaar.moonlight.api.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.moonlight.core.criteria_triggers.GrindItemTrigger;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

//TODO: finish these in 1.22
public interface ItemRenderExtension {

    @Nullable
    default ItemStackRenderer getItemRenderer() {
        return null;
    }

    default void renderHelmetOverlay(ItemStack stack, Player player, GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
    }

    /**
     * Implement in an item to allow it to be displayed with a custom animation using provided method callback
     * Will be called before the item actually gets rendered
     * You probably want to return UseAnim.NONE in item::getUseAnimation to not have two animations at the same time
     *
     * @return true to cancel original animation
     */
    default boolean animateItemFirstPerson(final Player entity, final ItemStack stack, final InteractionHand hand, final HumanoidArm arm, final PoseStack poseStack,
                                           float partialTicks, float pitch, float attackAnim, float handHeight) {
        return false;
    }

    //TODO: is this needed? why cant we use itemstack renderer? because it requires a new model json too

    /**
     * Implement if you want to also override the item renderer code
     *
     * @return true to cancel original item renderer
     */
    default boolean renderFirstPersonItem(final AbstractClientPlayer player, final ItemStack stack, final InteractionHand hand, final HumanoidArm arm, final PoseStack poseStack,
                                          float partialTicks, float pitch, float attackAnim, float equipAnim,
                                          MultiBufferSource buffer, int light, ItemInHandRenderer renderer) {
        return false;
    }

    /**
     * animate right hand
     *
     * @param stack    itemstack in hand
     * @param model    entity model. Can be cast to BipedModel
     * @param entity   entity
     * @param mainHand hand side
     * @return True if default animation should be skipped
     */
    default <T extends LivingEntity> boolean poseRightArm(ItemStack stack, HumanoidModel<T> model, T entity, HumanoidArm mainHand) {
        return false;
    }


    /**
     * animate left hand
     *
     * @param stack    itemstack in hand
     * @param model    entity model. Can be cast to BipedModel
     * @param entity   entity
     * @param mainHand hand side
     * @return True if default animation should be skipped
     */
    default <T extends LivingEntity> boolean poseLeftArm(ItemStack stack, HumanoidModel<T> model, T entity, HumanoidArm mainHand) {
        return false;
    }


    /**
     * Controls weather the other hand item renders or not
     */
    default HandMode getHandMode() {
        return HandMode.DEFAULT;
    }


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
    default <T extends Player, M extends EntityModel<T> & ArmedModel & HeadedModel> boolean renderThirdPersonItem(
            M parentModel, LivingEntity entity, ItemStack stack,
            HumanoidArm humanoidArm, PoseStack poseStack, MultiBufferSource bufferSource, int light) {
        return false;
    }

    enum HandMode {
        DEFAULT,
        TWO_HANDED,
        SINGLE_HANDED
    }

}
