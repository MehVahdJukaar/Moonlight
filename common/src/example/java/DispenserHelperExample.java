import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.api.util.DispenserHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.DispenserBlock;

// This is used so we can have tag dependant dispenser behaviors
public class DispenserHelperExample {

    // Call this during mod initialization
    public static void init(){
        RegHelper.addDispenserBehaviorRegistration(DispenserHelperExample::registerDynamic);
    }

    // This will run on datapack reload so you can access tags!
    public static void registerDynamic(DispenserHelper.Event event) {
        // Dispenser helper is useful as it wraps all existing behaviors,
        // guaranteeing compatibility with other mods that do the same

        // Place block behavior for emerald
        event.registerPlaceBlock(Items.EMERALD_BLOCK);

        event.register(new SpawnDragonBehavior(Items.DRAGON_EGG));
    }

    // Extend the main wrapper class for your custom behaviors
    public static class SpawnDragonBehavior extends DispenserHelper.AdditionalDispenserBehavior {

        protected SpawnDragonBehavior(Item item) {
            super(item);
        }

        @Override
        protected InteractionResultHolder<ItemStack> customBehavior(BlockSource source, ItemStack stack) {
            BlockPos frontPos = source.getPos().relative(source.getBlockState().getValue(DispenserBlock.FACING));
            if (source.getLevel().getBlockState(frontPos).isAir()) {
                EnderDragon entity = new EnderDragon(EntityType.ENDER_DRAGON, source.getLevel());
                entity.moveTo(source.getPos(), 0, 0);
                source.getLevel().addFreshEntity(entity);
                stack.shrink(1);
                return InteractionResultHolder.success(stack);
            }
            // Pass to fall back to previously registered behavior (or default one)
            return InteractionResultHolder.pass(stack);
        }
    }
}
