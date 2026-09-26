package net.mehvahdjukaar.moonlight.core.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.mehvahdjukaar.moonlight.api.util.CartographyTableHelper;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CartographyTableMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CartographyTableMenu.class)
public abstract class CartographyTableMenuMixin extends AbstractContainerMenu {

    protected CartographyTableMenuMixin(@Nullable MenuType<?> menuType, int containerId) {
        super(menuType, containerId);
    }

    @WrapOperation(method = "quickMoveStack", at = @At(value = "INVOKE", ordinal = 0,
            target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z"))
    private boolean moonlight$shiftClickIntoMapSlot(ItemStack stack, Item item, Operation<Boolean> original) {
        if (original.call(stack, item)) return true;
        return !this.slots.get(CartographyTableMenu.MAP_SLOT).hasItem()
                && CartographyTableHelper.isAllowed(CartographyTableMenu.MAP_SLOT, stack);
    }

    @WrapOperation(method = "quickMoveStack", at = @At(value = "INVOKE", ordinal = 1,
            target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z"))
    private boolean moonlight$shiftClickIntoAdditionalSlot(ItemStack stack, Item item, Operation<Boolean> original) {
        return original.call(stack, item) || CartographyTableHelper.isAllowed(CartographyTableMenu.ADDITIONAL_SLOT, stack);
    }
}
