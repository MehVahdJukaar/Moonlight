package net.mehvahdjukaar.moonlight.mixins.accessor;

import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.DispenserBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(DispenserBlock.class)
public interface DispenserBlockAccessor {

    @Accessor("DISPENSER_REGISTRY")
    public static Map<Item, DispenseItemBehavior> getDispenserRegistry() {
        throw new AssertionError();
    }

}
