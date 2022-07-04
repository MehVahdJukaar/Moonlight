package net.mehvahdjukaar.moonlight.platform.network.fabric;

import net.mehvahdjukaar.moonlight.platform.network.ChannelHandler;
import net.minecraft.resources.ResourceLocation;

public class NetworkHelperImpl {
    public static ChannelHandler createChannel(ResourceLocation channelMame) {
        return new ChannelHandlerImpl(channelMame);
    }
}
