package net.mehvahdjukaar.moonlight.core.mixins.platform;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

//for container
@Mixin(MenuScreens.class)
public interface MenuScreensAccessor {

    @Invoker("getConstructor")
    static <T extends AbstractContainerMenu> MenuScreens.ScreenConstructor<T, ?> invokeGetConstructor(MenuType<T> type) {
        throw new AssertionError();
    }
}
