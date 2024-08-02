package net.mehvahdjukaar.moonlight.api.platform.network;

import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public interface Context {

    NetworkDir getDirection();

    Player getPlayer();

    void disconnect(Component reason);

    void reply(CustomPacketPayload message);

}
