package net.mehvahdjukaar.moonlight.api.client.fabric;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

public interface IFabricMenuType<T> {
    static <T extends AbstractContainerMenu> MenuType<T> create(Factory<T> factory) {
        return new MenuType(factory);
    }


    T create(int i, Inventory arg, FriendlyByteBuf arg2);


    interface Factory<T extends AbstractContainerMenu> extends MenuType.MenuSupplier<T> {
        T create(int i, Inventory inventory, FriendlyByteBuf buffer);

        default T create(int i, Inventory inventory) {
            return this.create(i, inventory, null);
        }
    }

}
