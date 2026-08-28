package net.mehvahdjukaar.moonlight.api.block;

import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface IOwnerProtected {

    @Nullable
    UUID getOwner();

    void setOwner(@Nullable UUID owner);

    default void saveOwner(CompoundTag tag){
        UUID owner = this.getOwner();
        if(owner != null){
            tag.store("Owner", UUIDUtil.CODEC, owner);
        }
    }

    default void loadOwner(CompoundTag tag) {
        tag.read("Owner", UUIDUtil.CODEC).ifPresent(this::setOwner);
    }

    default boolean isOwnedBy(Player player) {
        UUID id = this.getOwner();
        return (id != null && id.equals(player.getUUID()));
    }

    default boolean isPublic() {
        return this.getOwner() == null;
    }

    default boolean isAccessibleBy(Player player){
        return this.isPublic() || this.isOwnedBy(player);
    }

    default boolean isNotOwnedBy(Player player) {
        UUID id = this.getOwner();
        return (id != null && !id.equals(player.getUUID()));
    }

}
