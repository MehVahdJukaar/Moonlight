package net.mehvahdjukaar.moonlight.core.network.fabric;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.mehvahdjukaar.moonlight.api.client.fabric.IFabricMenuType;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.mixins.fabric.MenuScreensAccessor;
import net.mehvahdjukaar.moonlight.core.mixins.fabric.MenuTypeAccessor;
import net.mehvahdjukaar.moonlight.core.mixins.fabric.ServerPlayerAccessor;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

import java.util.function.Consumer;

public class ClientBoundOpenCustomMenuMessage implements Message {

    public static final TypeAndCodec<RegistryFriendlyByteBuf, ClientBoundOpenCustomMenuMessage> TYPE = Message.makeType(
            Moonlight.res("s2c_open_menu"), ClientBoundOpenCustomMenuMessage::new);

    private final int containerId;
    private final MenuType<?> type;
    private final Component title;
    private final FriendlyByteBuf additionalData;

    public ClientBoundOpenCustomMenuMessage(int i, MenuType<?> menuType, Component component, FriendlyByteBuf additionalData) {
        this.containerId = i;
        this.type = menuType;
        this.title = component;
        this.additionalData = additionalData;
    }

    //replace with fabric screen handler
    public ClientBoundOpenCustomMenuMessage(RegistryFriendlyByteBuf buf) {
        this.containerId = buf.readVarInt();
        this.type = buf.readById(BuiltInRegistries.MENU::byId);
        this.title = ComponentSerialization.STREAM_CODEC.decode(buf);

        this.additionalData = new FriendlyByteBuf(Unpooled.wrappedBuffer(buf.readByteArray(32600)));
    }

    @Override
    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(this.containerId);
        buf.writeById(BuiltInRegistries.MENU::getId, this.type);
        ComponentSerialization.STREAM_CODEC.encode(buf, this.title);

        buf.writeByteArray(this.additionalData.readByteArray());
    }

    @Override
    public void handle(Context context) {
        clientHandle();
    }

    //TODO: use fabric api stuff
    @Environment(EnvType.CLIENT)
    private void clientHandle() {
        try {
            var constructor = MenuScreensAccessor.invokeGetConstructor(this.type);

            Inventory inventory = Minecraft.getInstance().player.getInventory();

            AbstractContainerMenu menu;

            var containerConstructor = ((MenuTypeAccessor<?>) type).getConstructor();

            if (containerConstructor instanceof IFabricMenuType.Factory customFactory) {
                menu = customFactory.create(containerId, inventory, additionalData);
            } else {
                menu = type.create(containerId, Minecraft.getInstance().player.getInventory());
            }
            @SuppressWarnings("unchecked")
            Screen screen = ((MenuScreens.ScreenConstructor<AbstractContainerMenu, ?>) constructor).create(menu, inventory, title);

            Minecraft.getInstance().player.containerMenu = ((MenuAccess<?>) screen).getMenu();
            Minecraft.getInstance().setScreen(screen);

        } finally {
            this.additionalData.release();
        }
    }


    public static void openMenu(ServerPlayer player, MenuProvider menuProvider, Consumer<RegistryFriendlyByteBuf> extraDataWriter) {
        if (player.containerMenu != player.inventoryMenu) {
            player.closeContainer();
        }
        var p = ((ServerPlayerAccessor) player);

        p.invokeNextContainerCounter();
        AbstractContainerMenu containerMenu = menuProvider.createMenu(p.getContainerCounter(), player.getInventory(), player);
        if (containerMenu == null) {
            if (player.isSpectator()) {
                player.displayClientMessage(Component.translatable("container.spectatorCantOpen").withStyle(ChatFormatting.RED), true);
            }
        } else {
            RegistryFriendlyByteBuf extraData = new RegistryFriendlyByteBuf(Unpooled.buffer(), player.level().registryAccess());
            extraDataWriter.accept(extraData);
            extraData.readerIndex(0);
            FriendlyByteBuf output = new FriendlyByteBuf(Unpooled.buffer());
            output.writeVarInt(extraData.readableBytes());
            output.writeBytes(extraData);

            NetworkHelper.sendToClientPlayer(player, new ClientBoundOpenCustomMenuMessage(containerMenu.containerId,
                    containerMenu.getType(), menuProvider.getDisplayName(), output));

            p.invokeInitMenu(containerMenu);
            player.containerMenu = containerMenu;

        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE.type();
    }
}
