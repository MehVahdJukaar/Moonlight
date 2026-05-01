package net.mehvahdjukaar.moonlight.api.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

// Interface for tile entities that can be edited by one player at a time like signs and such
//TODO: turn into component
public interface IOneUserInteractable {

    void setCurrentUser(@Nullable UUID uuid);

    @Nullable
    UUID getCurrentUser();

    //call before access. if free or claimed by self, returns true
    default boolean canBeUsedBy(BlockPos myPos, Entity player) {
        //player who may edit is a server side concept. here we just check if player is close by
        if (!(player instanceof ServerPlayer sp)) {
            return isCloseEnoughToUse(player, myPos);
        }
        validateClaimer(myPos, sp.serverLevel());
        UUID uuid = this.getCurrentUser();
        if (uuid == null) return true;
        return uuid.equals(player.getUUID());
    }

    private void validateClaimer(BlockPos myPos, ServerLevel level) {
        if (level == null) {
            this.setCurrentUser(null);
            return;
        }
        UUID uuid = this.getCurrentUser();
        if (uuid == null) return;

        Entity player = level.getEntity(uuid);
        if (player == null || !isCloseEnoughToUse(player, myPos)) {
            this.setCurrentUser(null);
        }
    }

    default boolean isCloseEnoughToUse(Entity e, BlockPos myPos) {
        double maxDistance = 8.0;
        if (e instanceof Player p) {
            return p.canInteractWithBlock(myPos, maxDistance);
        } else {
            double currentDist = (new AABB(myPos)).distanceToSqr(e.getEyePosition());
            return currentDist < maxDistance * maxDistance;
        }
    }

}
