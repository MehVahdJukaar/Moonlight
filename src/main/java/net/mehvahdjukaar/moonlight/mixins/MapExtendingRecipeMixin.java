package net.mehvahdjukaar.moonlight.mixins;

import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.MapExtendingRecipe;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MapExtendingRecipe.class)
public abstract class MapExtendingRecipeMixin {


    @Redirect(method = "matches*",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z",
                    ordinal = 1))
    private boolean matches(ItemStack original, CraftingContainer inventory, Level world) {
        CompoundTag tag = original.getTag();
        if (tag != null && tag.contains("CustomDecorations", 9)) {
            return true;
        }
        return original.isEmpty();
    }

}