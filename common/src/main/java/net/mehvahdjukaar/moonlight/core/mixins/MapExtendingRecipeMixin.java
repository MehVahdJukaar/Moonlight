package net.mehvahdjukaar.moonlight.core.mixins;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.MapExtendingRecipe;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(MapExtendingRecipe.class)
public abstract class MapExtendingRecipeMixin {

    @Inject(method = "matches(Lnet/minecraft/world/inventory/CraftingContainer;Lnet/minecraft/world/level/Level;)Z",
            locals = LocalCapture.CAPTURE_FAILEXCEPTION,
            cancellable = true,
            at = @At(value = "INVOKE",
                    shift = At.Shift.BEFORE,
                    target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z"))
    private void preventsExpandingCustomExplorationMaps(CraftingContainer inv, Level level, CallbackInfoReturnable<Boolean> cir, ItemStack itemStack) {
        CompoundTag tag = itemStack.getTag();
        if (tag != null && tag.contains("CustomDecorations", 9)) {
            cir.setReturnValue(false);
        }
    }

}