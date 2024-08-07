package net.mehvahdjukaar.moonlight.core.mixins.fabric;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

//for container
@Mixin(MenuScreens.class)
public interface MenuScreensAccessor {

    @Invoker("getConstructor")
    static <T extends AbstractContainerMenu> MenuScreens.ScreenConstructor<T, ?> invokeGetConstructor(MenuType<T> type) {
        throw new AssertionError();
    }
}
