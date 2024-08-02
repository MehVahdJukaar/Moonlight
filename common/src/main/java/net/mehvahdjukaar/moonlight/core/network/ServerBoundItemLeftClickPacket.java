package net.mehvahdjukaar.moonlight.core.network;

import net.mehvahdjukaar.moonlight.api.item.ILeftClickReact;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public record ServerBoundItemLeftClickPacket(InteractionHand hand) implements Message {

    public static final TypeAndCodec<RegistryFriendlyByteBuf, ServerBoundItemLeftClickPacket> TYPE =
            Message.makeType(Moonlight.res("c2s_item_left_click"), ServerBoundItemLeftClickPacket::new);

    public ServerBoundItemLeftClickPacket(RegistryFriendlyByteBuf buf) {
        this(buf.readEnum(InteractionHand.class));
    }

    @Override
    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeEnum(this.hand);
    }

    @Override
    public void handle(Context context) {
        // server level
        Player player = context.getPlayer();

        ItemStack stack = player.getItemInHand(hand);
        if(stack.getItem() instanceof ILeftClickReact lr){
            lr.onLeftClick(stack, player, hand);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE.type();
    }
}
