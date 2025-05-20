package net.mehvahdjukaar.moonlight.api.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.mehvahdjukaar.moonlight.api.misc.TileOrEntityTarget;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.moonlight.core.network.ClientBoundOpenScreenPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Implement in your blocks(deprecated) or tiles or entities. Just for utility method
 */
public interface IScreenProvider {

    @Environment(EnvType.CLIENT)
    default void openScreen(Level level, Player player, Direction direction) {
    }

    default void sendOpenGuiPacket(Player player, @Nullable Direction hitFace) {
        NetworkHelper.sendToClientPlayer((ServerPlayer) player,
                new ClientBoundOpenScreenPacket(getScreenProviderTarget(), hitFace));
    }

    @NotNull TileOrEntityTarget getScreenProviderTarget();

}
