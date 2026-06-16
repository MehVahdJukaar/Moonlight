package net.mehvahdjukaar.moonlight.core.network.platform;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Wraps a {@link MenuProvider} plus an extra-data writer into a Fabric
 * {@link ExtendedScreenHandlerFactory}, so menus open through vanilla's
 * {@code ServerPlayer.openMenu(...)} flow. This guarantees the open packet and the initial
 * {@code ClientboundContainerSetContentPacket} are ordered correctly (Fabric handles the open
 * payload on the client thread without an extra task hop), unlike a bespoke open-menu packet.
 */
public record ExtraDataMenuProvider(MenuProvider provider,
                                    Consumer<RegistryFriendlyByteBuf> extraDataWriter)
        implements ExtendedScreenHandlerFactory<FriendlyByteBuf> {

    @Override
    public FriendlyByteBuf getScreenOpeningData(ServerPlayer player) {
        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), player.registryAccess());
        extraDataWriter.accept(buf);
        return buf;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return provider.createMenu(containerId, inventory, player);
    }

    @Override
    public Component getDisplayName() {
        return provider.getDisplayName();
    }
}
