package net.mehvahdjukaar.moonlight.api.util;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;

/** An interaction result plus the stack that replaces the one acted upon. */
public record ItemStackResult(InteractionResult result, ItemStack stack) {

    public static ItemStackResult success(ItemStack stack) {
        return new ItemStackResult(InteractionResult.SUCCESS, stack);
    }

    public static ItemStackResult fail(ItemStack stack) {
        return new ItemStackResult(InteractionResult.FAIL, stack);
    }

    public static ItemStackResult pass(ItemStack stack) {
        return new ItemStackResult(InteractionResult.PASS, stack);
    }

    public boolean consumesAction() {
        return this.result.consumesAction();
    }
}
