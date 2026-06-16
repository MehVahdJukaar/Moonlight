package net.mehvahdjukaar.moonlight.api.client.platform;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

public interface IFabricMenuType<T> {

    // Passthrough codec: carries the raw extra-data buffer to the client so we can keep the
    // FriendlyByteBuf-based Factory API while routing through Fabric's extended screen handler flow.
    StreamCodec<RegistryFriendlyByteBuf, FriendlyByteBuf> EXTRA_DATA_CODEC = new StreamCodec<>() {
        @Override
        public FriendlyByteBuf decode(RegistryFriendlyByteBuf buf) {
            return new FriendlyByteBuf(Unpooled.wrappedBuffer(buf.readByteArray()));
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, FriendlyByteBuf data) {
            byte[] bytes = new byte[data.readableBytes()];
            data.getBytes(data.readerIndex(), bytes);
            buf.writeByteArray(bytes);
        }
    };

    static <T extends AbstractContainerMenu> MenuType<T> create(Factory<T> factory) {
        // ExtendedScreenHandlerType extends MenuType<T>, so menus open through vanilla
        // player.openMenu(...) with correct open/content packet ordering handled by Fabric.
        return new ExtendedScreenHandlerType<T, FriendlyByteBuf>(
                (syncId, inventory, data) -> factory.create(syncId, inventory, data),
                EXTRA_DATA_CODEC);
    }


    T create(int i, Inventory arg, FriendlyByteBuf arg2);


    interface Factory<T extends AbstractContainerMenu> extends MenuType.MenuSupplier<T> {
        T create(int i, Inventory inventory, FriendlyByteBuf buffer);

        default T create(int i, Inventory inventory) {
            return this.create(i, inventory, null);
        }
    }

}
