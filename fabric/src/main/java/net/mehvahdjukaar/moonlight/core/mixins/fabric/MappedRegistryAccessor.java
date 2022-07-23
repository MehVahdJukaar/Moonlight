package net.mehvahdjukaar.moonlight.core.mixins.fabric;

import net.minecraft.core.MappedRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MappedRegistry.class)
public interface MappedRegistryAccessor {

    @Accessor("frozen")
    void setFrozen(boolean freeze);
}
