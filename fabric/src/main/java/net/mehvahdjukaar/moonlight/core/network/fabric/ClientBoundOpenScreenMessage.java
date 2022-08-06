package net.mehvahdjukaar.moonlight.core.network.fabric;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.mehvahdjukaar.moonlight.api.block.ItemDisplayTile;
import net.mehvahdjukaar.moonlight.api.client.fabric.IFabricMenuType;
import net.mehvahdjukaar.moonlight.api.platform.network.ChannelHandler;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.moonlight.core.mixins.fabric.MenuScreensAccessor;
import net.mehvahdjukaar.moonlight.core.mixins.fabric.MenuTypeAccessor;
import net.mehvahdjukaar.moonlight.core.mixins.fabric.ServerPlayerAccessor;
import net.mehvahdjukaar.moonlight.core.network.ModMessages;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

import java.util.function.Consumer;

public class ClientBoundOpenScreenMessage implements Message {

    private final int containerId;
    private final MenuType<?> type;
    private final Component title;
    private final FriendlyByteBuf additionalData;

    public ClientBoundOpenScreenMessage(int i, MenuType<?> menuType, Component component, FriendlyByteBuf additionalData) {
        this.containerId = i;
        this.type = menuType;
        this.title = component;
        this.additionalData = additionalData;
    }

    public ClientBoundOpenScreenMessage(FriendlyByteBuf buf) {
        this.containerId = buf.readVarInt();
        this.type = buf.readById(Registry.MENU);
        this.title = buf.readComponent();
        this.additionalData = new FriendlyByteBuf(Unpooled.wrappedBuffer(buf.readByteArray(32600)));
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buf) {
        buf.writeVarInt(this.containerId);
        buf.writeId(Registry.MENU, this.type);
        buf.writeComponent(this.title);
        buf.writeByteArray(this.additionalData.readByteArray());
    }

    @Override
    public void handle(ChannelHandler.Context context) {
        clientHandle();
    }

    @Environment(EnvType.CLIENT)
    private void clientHandle() {
        try {
            var constructor = MenuScreensAccessor.invokeGetConstructor(this.type);

            Inventory inventory = Minecraft.getInstance().player.getInventory();

            AbstractContainerMenu menu;

            var containerConstructor = ((MenuTypeAccessor<?>)type).getConstructor();

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


    public static void openMenu(ServerPlayer player, MenuProvider menuProvider, Consumer<FriendlyByteBuf> extraDataWriter) {
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
            FriendlyByteBuf extraData = new FriendlyByteBuf(Unpooled.buffer());
            extraDataWriter.accept(extraData);
            extraData.readerIndex(0);
            FriendlyByteBuf output = new FriendlyByteBuf(Unpooled.buffer());
            output.writeVarInt(extraData.readableBytes());
            output.writeBytes(extraData);
            ModMessages.CHANNEL.sendToClientPlayer(player, new ClientBoundOpenScreenMessage(containerMenu.containerId,
                    containerMenu.getType(), menuProvider.getDisplayName(), output));
            p.invokeInitMenu(containerMenu);
            player.containerMenu = containerMenu;
             
        }
    }

}
