package net.mehvahdjukaar.moonlight.core.mixins.fabric;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

//for container
@Mixin(ServerPlayer.class)
public interface ServerPlayerAccessor {

    @Accessor("containerCounter")
    int getContainerCounter();

    @Invoker("nextContainerCounter")
    void invokeNextContainerCounter();

    @Invoker("initMenu")
    void invokeInitMenu(AbstractContainerMenu abstractContainerMenu);
}
