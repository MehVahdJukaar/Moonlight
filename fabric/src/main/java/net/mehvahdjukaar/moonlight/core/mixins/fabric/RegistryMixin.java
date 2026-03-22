package net.mehvahdjukaar.moonlight.core.mixins.fabric;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.serialization.DataResult;
import net.mehvahdjukaar.moonlight.api.platform.fabric.RegistryQueue;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Registry.class)
public interface RegistryMixin {

    @Inject(method = "safeCastToReference", at = @At(value = "HEAD"))
    default <T> void ml$addHolderDelegate(Holder<T> value,
                                                 CallbackInfoReturnable<DataResult<Holder.Reference<T>>> cir,
                                                 @Local(argsOnly = true) LocalRef<Holder<T>> holderRef) {
        if (value instanceof RegistryQueue.RegEntryHolder<?> ro) {
            holderRef.set((Holder<T>) ro.getDelegate());
        }
    }
}
