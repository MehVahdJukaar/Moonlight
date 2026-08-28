package net.mehvahdjukaar.moonlight.api.client.platform;

import net.mehvahdjukaar.moonlight.core.network.platform.ClientBoundOpenExtendedMenuMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

public interface IFabricMenuType<T> {

    static <T extends AbstractContainerMenu> MenuType<T> create(Factory<T> factory) {
        // extra data arrives in ClientBoundOpenExtendedMenuMessage right before the open screen packet
        return new MenuType<>((i, inventory) -> factory.create(i, inventory,
                ClientBoundOpenExtendedMenuMessage.consumePendingData()), FeatureFlags.DEFAULT_FLAGS);
    }

    // For menus that don't sync any extra data: a plain vanilla MenuType is enough, so menus open
    // straight through player.openMenu(provider) without the extra data payload.
    static <T extends AbstractContainerMenu> MenuType<T> createSimple(MenuType.MenuSupplier<T> factory) {
        return new MenuType<>(factory, FeatureFlags.DEFAULT_FLAGS);
    }


    T create(int i, Inventory arg, FriendlyByteBuf arg2);


    interface Factory<T extends AbstractContainerMenu> extends MenuType.MenuSupplier<T> {
        T create(int i, Inventory inventory, FriendlyByteBuf buffer);

        default T create(int i, Inventory inventory) {
            return this.create(i, inventory, null);
        }
    }

}
