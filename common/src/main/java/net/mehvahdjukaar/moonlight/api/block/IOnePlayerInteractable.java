package net.mehvahdjukaar.moonlight.api.block;

import net.mehvahdjukaar.moonlight.api.client.IScreenProvider;
import net.mehvahdjukaar.moonlight.api.misc.TileOrEntityTarget;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

// Interface for tile entities that can be edited by one player at a time like signs and such
@Deprecated(forRemoval = true)
public interface IOnePlayerInteractable {

    void setPlayerWhoMayEdit(@Nullable UUID uuid);

    UUID getPlayerWhoMayEdit();

    //call before access
    default boolean isEditingPlayer(BlockPos myPos, Player player) {
        //player who may edit is a server side concept. here we just check if player is close by
        if (player.level().isClientSide) {
            return isCloseEnoughToEdit(player, myPos);
        }
        validateEditingPlayer(myPos, player.level());
        UUID uuid = this.getPlayerWhoMayEdit();
        return uuid != null && uuid.equals(player.getUUID());
    }

    default boolean isOtherPlayerEditing(BlockPos myPos, Player otherThan) {
        validateEditingPlayer(myPos, otherThan.level());
        UUID uuid = this.getPlayerWhoMayEdit();
        return uuid != null && !uuid.equals(otherThan.getUUID());
    }


    private void validateEditingPlayer(BlockPos myPos, Level level) {
        if (level == null) {
            this.setPlayerWhoMayEdit(null);
            return;
        }
        UUID uuid = this.getPlayerWhoMayEdit();
        if (uuid == null) return;

        Player player = level.getPlayerByUUID(uuid);
        if (player == null || !isCloseEnoughToEdit(player, myPos)) {
            this.setPlayerWhoMayEdit(null);
        }
    }

    private boolean isCloseEnoughToEdit(Player player, BlockPos myPos) {
        return player.canInteractWithBlock(myPos, 8);
    }

    @Deprecated(forRemoval = true)
    default boolean tryOpeningEditGui(ServerPlayer player, BlockPos pos, ItemStack stack, Direction hitFace) {
        return tryOpeningEditGui(player, pos, stack, hitFace, new Vec3(0.5, 0.5, 0.5));
    }

    default boolean tryOpeningEditGui(ServerPlayer player, BlockPos pos, ItemStack stack, Direction hitFace, Vec3 hitPos) {
        //this is likely not needed
        if (Utils.mayPerformBlockAction(player, pos, stack) && !this.isOtherPlayerEditing(pos, player)) {
            // open gui (edit sign with empty hand)
            this.setPlayerWhoMayEdit(player.getUUID());

            if (this instanceof IScreenProvider sp) {
                sp.sendOpenGuiPacket(player, hitFace, hitPos);
                return false;
            }
            if (this instanceof MenuProvider mp && this instanceof BlockEntity be) {
                TileOrEntityTarget target = TileOrEntityTarget.of(be);
                PlatHelper.openCustomMenu(player, mp, target::write);
                return true;
            }
        }
        return false;
    }
}
