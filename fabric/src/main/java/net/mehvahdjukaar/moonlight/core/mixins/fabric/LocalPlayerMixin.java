package net.mehvahdjukaar.moonlight.core.mixins.fabric;

import com.mojang.authlib.GameProfile;
import net.mehvahdjukaar.moonlight.api.entity.IControllableEntity;
import net.mehvahdjukaar.moonlight.api.entity.IControllableVehicle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.ProfilePublicKey;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends AbstractClientPlayer {

    @Shadow
    public Input input;

    @Shadow @Final protected Minecraft minecraft;

    protected LocalPlayerMixin(ClientLevel clientLevel, GameProfile gameProfile) {
        super(clientLevel, gameProfile);
    }

    @Inject(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/Input;tick(ZF)V",
            shift = At.Shift.AFTER))
    public void onMovementInputUpdate(CallbackInfo ci) {
        Entity riddenEntity = this.getVehicle();
        if (riddenEntity instanceof IControllableEntity listener) {
            listener.onInputUpdate(this.input.left, input.right,
                    input.up, input.down,
                    this.minecraft.options.keySprint.isDown(), input.jumping);
        }

    }
}
