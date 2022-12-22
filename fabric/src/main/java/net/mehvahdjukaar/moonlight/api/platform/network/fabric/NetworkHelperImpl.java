package net.mehvahdjukaar.moonlight.api.platform.network.fabric;

import net.mehvahdjukaar.moonlight.api.platform.network.ChannelHandler;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class NetworkHelperImpl {
    public static ChannelHandler createChannel(ResourceLocation channelMame) {
        return new ChannelHandlerImpl(channelMame);
    }
}
