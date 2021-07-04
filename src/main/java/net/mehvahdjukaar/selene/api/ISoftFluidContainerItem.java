package net.mehvahdjukaar.selene.api;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;

import javax.annotation.Nullable;

/**
 * implement this in your item to provide a soft fluid without needing to register its containers to the fluid itself
 */
//TODO: this is useless since this way it won't be able to be taken out.. maybe add other interface for empty container
public interface ISoftFluidContainerItem {

    ResourceLocation getSoftFluid();

    default CompoundNBT getFluidNBT(){
        return new CompoundNBT();
    }

    /**
     * @return container capacity
     */
    default int getAmount(){
        return 1;
    }

    ItemStack getEmptyContainer();

    @Nullable
    SoundEvent getEmptySound();
}
