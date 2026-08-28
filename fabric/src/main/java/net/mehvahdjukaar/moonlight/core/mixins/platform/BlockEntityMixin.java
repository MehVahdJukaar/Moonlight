package net.mehvahdjukaar.moonlight.core.mixins.platform;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.mehvahdjukaar.moonlight.api.client.model.ExtraModelData;
import net.mehvahdjukaar.moonlight.api.client.model.IExtraModelDataProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// fabric has no per block entity data packet hook, so hook the load and only act in a client level
@Mixin(BlockEntity.class)
public abstract class BlockEntityMixin {

    @Shadow
    public abstract boolean hasLevel();

    @Inject(method = "loadWithComponents", at = @At("HEAD"))
    private void moonlight$storeOldModelData(ValueInput input, CallbackInfo ci,
                                             @Share("oldData") LocalRef<ExtraModelData> oldData) {
        if (this.hasLevel() && this instanceof IExtraModelDataProvider data) {
            oldData.set(data.getExtraModelData());
        }
    }

    @Inject(method = "loadWithComponents", at = @At("TAIL"))
    private void moonlight$callModelDataCallback(ValueInput input, CallbackInfo ci,
                                                 @Share("oldData") LocalRef<ExtraModelData> oldData) {
        ExtraModelData old = oldData.get();
        if (old != null && this instanceof IExtraModelDataProvider data) {
            data.afterDataPacket(old);
        }
    }
}
