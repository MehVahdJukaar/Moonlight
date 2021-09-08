package net.mehvahdjukaar.selene.api;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;

import javax.annotation.Nullable;

/**
 * Implement in an item to allow it to be displayed with a custom animation using provided method callback
 * Will be called before the item actually gets rendered
 */
public interface IFirstPersonAnimationProvider {

    void animateItemFirstPerson(final LivingEntity entity, final ItemStack stack, final Hand hand, final MatrixStack matrixStack,
                                float partialTicks, float pitch, float attackAnim, float handHeight);
}
