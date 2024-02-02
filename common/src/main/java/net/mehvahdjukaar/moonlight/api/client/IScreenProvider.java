package net.mehvahdjukaar.moonlight.api.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.moonlight.core.network.ClientBoundOpenScreenPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Implement in your blocks or tiles. Just for utility method
 */
public interface IScreenProvider {

    @Environment(EnvType.CLIENT)
    void openScreen(Level level, BlockPos pos, Player player, Direction direction);

    default void sendOpenGuiPacket(Level level, BlockPos pos, Player player, Direction hitFace) {
        NetworkHelper.sendToClientPlayer((ServerPlayer) player,
                new ClientBoundOpenScreenPacket(pos, hitFace));
    }
}
