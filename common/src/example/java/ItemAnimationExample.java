import net.mehvahdjukaar.moonlight.api.item.ClientAnimationExtension;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.consume_effects.UseAnim;
import net.minecraft.world.entity.LivingEntity;

// The item itself stays common code. The hooks live in a separate class because their signatures mention client
// classes, which don't exist on a dedicated server.
public class ItemAnimationExample extends Item {

    public ItemAnimationExample(Properties properties) {
        super(properties);
        // pass a lambda, not a method reference or a ready made instance: on a dedicated server the supplier is
        // never called, so ItemAnimationExampleRenderer is never loaded there
        ClientAnimationExtension.attach(this, () -> new ItemAnimationExampleRenderer(this));
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        // NONE so we don't get another animation on top of this one.
        // SPYGLASS works too, it has no arm bob
        return UseAnim.NONE;
    }

    @Override
    public int getUseDuration(ItemStack itemStack, LivingEntity livingEntity) {
        return 72000;
    }
}
