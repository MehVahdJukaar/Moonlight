import net.mehvahdjukaar.moonlight.api.util.DispenserHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.DispenserBlock;

public class DispenserHelperExample {


    // Call this during mod setup after items have been registered
    public static void setup() {
        // Dispenser helper is useful as it wraps all existing behaviors,
        // guaranteeing compatibility with other mods that do the same

        // Place block behavior for emerald
        DispenserHelper.registerPlaceBlockBehavior(Items.EMERALD_BLOCK);

        DispenserHelper.registerCustomBehavior(new SpawnDragonBehavior(Items.DRAGON_EGG));
    }

    // Extend the main wrapper class for your custom behaviors
    public static class SpawnDragonBehavior extends DispenserHelper.AdditionalDispenserBehavior {

        protected SpawnDragonBehavior(Item item) {
            super(item);
        }

        @Override
        protected InteractionResultHolder<ItemStack> customBehavior(BlockSource source, ItemStack stack) {
            BlockPos frontPos = source.pos().relative(source.state().getValue(DispenserBlock.FACING));
            if (source.level().getBlockState(frontPos).isAir()) {
                EnderDragon entity = new EnderDragon(EntityType.ENDER_DRAGON, source.level());
                entity.moveTo(source.pos(), 0, 0);
                source.level().addFreshEntity(entity);
                stack.shrink(1);
                return InteractionResultHolder.success(stack);
            }
            // Pass to fall back to previously registered behavior (or default one)
            return InteractionResultHolder.pass(stack);
        }
    }
}
