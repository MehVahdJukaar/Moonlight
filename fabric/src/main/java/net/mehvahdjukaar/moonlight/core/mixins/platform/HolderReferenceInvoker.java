package net.mehvahdjukaar.moonlight.core.mixins.platform;

import net.minecraft.core.Holder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Holder.Reference.class)
public interface HolderReferenceInvoker {

    @Invoker("bindValue")
    void invokeBindValue(Object value);
}
