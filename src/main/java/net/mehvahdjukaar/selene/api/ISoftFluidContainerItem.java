package net.mehvahdjukaar.selene.api;

import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

import javax.annotation.Nullable;

/**
 * NYI
 * implement this in your item to provide a soft fluid without needing to register its containers to the fluid itself
 */
//TODO: this is useless since this way it won't be able to be taken out.. maybe add other interface for empty container
public interface ISoftFluidContainerItem {

    ResourceLocation getSoftFluid();

    default CompoundTag getFluidNBT(){
        return new CompoundTag();
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
