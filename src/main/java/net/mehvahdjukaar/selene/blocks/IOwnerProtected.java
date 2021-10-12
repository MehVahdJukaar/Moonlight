package net.mehvahdjukaar.selene.blocks;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;

import javax.annotation.Nullable;
import java.util.UUID;

public interface IOwnerProtected {

    @Nullable
    UUID getOwner();

    void setOwner(@Nullable UUID owner);

    default void saveOwner(CompoundNBT tag){
        UUID owner = this.getOwner();
        if(owner != null){
            tag.putUUID("Owner", owner);
        }
    }

    default void loadOwner(CompoundNBT tag) {
        if (tag.contains("Owner")){
            this.setOwner(tag.getUUID("Owner"));
        }
    }

    default boolean isOwnedBy(PlayerEntity player) {
        UUID id = this.getOwner();
        return (id != null && id.equals(player.getUUID()));
    }

    default boolean isPublic() {
        return this.getOwner() == null;
    }

    default boolean isAccessibleBy(PlayerEntity player){
        return this.isPublic() || this.isOwnedBy(player);
    }

    default boolean isNotOwnedBy(PlayerEntity player) {
        UUID id = this.getOwner();
        return (id != null && !id.equals(player.getUUID()));
    }

}
