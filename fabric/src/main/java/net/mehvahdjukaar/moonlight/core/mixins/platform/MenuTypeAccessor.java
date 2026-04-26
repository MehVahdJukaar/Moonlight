package net.mehvahdjukaar.moonlight.core.mixins.platform;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

//for container
@Mixin(MenuType.class)
public interface MenuTypeAccessor<T extends AbstractContainerMenu> {

    @Accessor("constructor")
    MenuType.MenuSupplier<T> getConstructor();

}
