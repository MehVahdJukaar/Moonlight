package net.mehvahdjukaar.moonlight.api.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.mehvahdjukaar.moonlight.api.misc.TileOrEntityTarget;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.moonlight.core.network.ClientBoundOpenScreenMessage;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Implement in your blocks(deprecated) or tiles or entities. Just for utility method
 */
public interface IScreenProvider {

    @Deprecated(forRemoval = true)
    @Environment(EnvType.CLIENT)
    default void openScreen(Level level, Player player, Direction direction) {
    }

    @Environment(EnvType.CLIENT)
    default void openScreen(Level level, Player player, Direction direction, Vec3 hitPos) {
        openScreen(level, player, direction);
    }

    @Deprecated(forRemoval = true)
    default void sendOpenGuiPacket(ServerPlayer player, @Nullable Direction hitFace) {
        sendOpenGuiPacket(player, hitFace, null);
    }

    default void sendOpenGuiPacket(ServerPlayer player, @Nullable Direction hitFace, @Nullable Vec3 hitPos) {
        TileOrEntityTarget target;
        if (this instanceof BlockEntity be) {
            target = TileOrEntityTarget.of(be);
        } else if (this instanceof Player entity) {
            target = TileOrEntityTarget.of(entity);
        } else {
            throw new IllegalStateException("IScreenProvider must be a BlockEntity or Entity");
        }
        if (hitPos == null) {
            hitPos = new Vec3(0.5, 0.5, 0.5);
        }
        NetworkHelper.sendToClientPlayer(player, new ClientBoundOpenScreenMessage(target, hitFace, hitPos));
    }

}
