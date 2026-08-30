import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.moonlight.api.item.IFirstPersonAnimationProvider;
import net.mehvahdjukaar.moonlight.api.item.IThirdPersonAnimationProvider;
import net.mehvahdjukaar.moonlight.api.item.IThirdPersonSpecialItemRenderer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

// One class can fill any number of the hook slots. Attach it with ClientAnimationExtension.attach, which also works
// for items you don't own.
public class ItemAnimationExampleRenderer implements IFirstPersonAnimationProvider, IThirdPersonAnimationProvider,
        IThirdPersonSpecialItemRenderer {

    private final Item item;

    public ItemAnimationExampleRenderer(Item item) {
        this.item = item;
    }

    @Override
    public void animateItemFirstPerson(Player entity, ItemStack stack, InteractionHand hand, HumanoidArm arm,
                                       PoseStack poseStack, float partialTicks, float pitch, float attackAnim,
                                       float handHeight) {
        if (entity.isUsingItem() && entity.getUseItemRemainingTicks() > 0 && entity.getUsedItemHand() == hand) {
            float timeLeft = stack.getUseDuration(entity) - (entity.getUseItemRemainingTicks() - partialTicks + 1.0F);
            poseStack.translate(0, 0, timeLeft * 0.04F);
        }
    }

    @Override
    public Result poseArm(ItemStack stack, HumanoidModel<?> model, HumanoidRenderState state, HumanoidArm arm) {
        if (stack.getItem() != this.item) return Result.PASS;
        // just some random angles. Return HANDLED_BOTH_ARMS instead to pose both arms in one call,
        // like a two handed weapon would
        if (arm == HumanoidArm.RIGHT) model.rightArm.yRot = -10;
        else model.leftArm.yRot = 10;
        return Result.HANDLED;
    }

    @Override
    public void renderThirdPersonItem(HumanoidModel<AvatarRenderState> parentModel, AvatarRenderState state, ItemStack stack,
                                      HumanoidArm arm, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int light) {
        // this is what the spyglass does when it renders on the player face instead of in its hand. Render your item
        // in player space here instead of hand space, e.g. after parentModel.translateToHand(state, arm, poseStack)
    }
}
