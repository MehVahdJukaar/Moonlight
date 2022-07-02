package net.mehvahdjukaar.moonlight.platform.network.fabric;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.mehvahdjukaar.moonlight.platform.network.Message;
import net.mehvahdjukaar.moonlight.platform.network.ChannelHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public class FabricClientNetwork {
    public static <M extends Message> void register(
            ResourceLocation res,
            Function<FriendlyByteBuf,M> decoder) {

        ClientPlayNetworking.registerGlobalReceiver(
                res, (client, h, buf, r) -> client.execute(() -> decoder.apply(buf)
                        .handle(new FabricChannelHandler.Wrapper(client.player, ChannelHandler.NetworkDir.PLAY_TO_CLIENT))));
    }
}
