package net.mehvahdjukaar.moonlight.core.mixins.accessor;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DispenserBlockEntity.class)
public interface DispenserBlockEntityAccessor {

    @Accessor("items")
    NonNullList<ItemStack> getItems();

}
