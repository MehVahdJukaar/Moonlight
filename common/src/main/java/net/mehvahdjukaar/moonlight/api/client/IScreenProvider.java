package net.mehvahdjukaar.moonlight.api.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.mehvahdjukaar.moonlight.core.network.ClientBoundOpenScreenPacket;
import net.mehvahdjukaar.moonlight.core.network.ModMessages;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Implement in your blocks or tiles. Just for utility method
 */
public interface IScreenProvider {

    @Deprecated(forRemoval = true)
    @Environment(EnvType.CLIENT)
    void openScreen(Level level, BlockPos pos, Player player);

    @Environment(EnvType.CLIENT)
    default void openScreen(Level level, BlockPos pos, Player player, Direction direction){
        openScreen(level, pos, player);
    }

    @Deprecated(forRemoval = true)
    default void sendOpenGuiPacket(Level level, BlockPos pos, Player player) {
        sendOpenGuiPacket(level, pos, player, Direction.NORTH);
    }

    default void sendOpenGuiPacket(Level level, BlockPos pos, Player player, Direction hitFace) {
        ModMessages.CHANNEL.sendToClientPlayer((ServerPlayer) player,
                new ClientBoundOpenScreenPacket(pos, hitFace));
    }
}
