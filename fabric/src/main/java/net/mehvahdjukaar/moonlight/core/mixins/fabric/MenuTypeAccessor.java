package net.mehvahdjukaar.moonlight.core.mixins.fabric;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

//for container
@Mixin(MenuType.class)
public interface MenuTypeAccessor<T extends AbstractContainerMenu> {

    @Accessor("constructor")
    MenuType.MenuSupplier<T> getConstructor();

}
