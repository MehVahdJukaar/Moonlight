package net.mehvahdjukaar.moonlight.core.mixins;

import net.mehvahdjukaar.moonlight.api.item.ILeftClickReact;
import net.mehvahdjukaar.moonlight.core.network.ModMessages;
import net.mehvahdjukaar.moonlight.core.network.ServerBoundItemLeftClickPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    @Shadow
    @Nullable
    public LocalPlayer player;

    //cancel rope slide down sound
    @Inject(method = "startAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/HitResult;getType()Lnet/minecraft/world/phys/HitResult$Type;",
            shift = At.Shift.BEFORE), cancellable = true)
    private void ml$switchLunchBoxMode(CallbackInfoReturnable<Boolean> cir) {
        InteractionHand hand = InteractionHand.MAIN_HAND;
        ItemStack stack = this.player.getItemInHand(hand);
        if (stack.getItem() instanceof ILeftClickReact lr) {
            boolean cancel = lr.onLeftClick(stack, this.player, hand);
            ModMessages.CHANNEL.sendToServer(new ServerBoundItemLeftClickPacket(hand));
            if (cancel) {
                this.player.swing(InteractionHand.MAIN_HAND);
                cir.setReturnValue(false);
            }
        }
    }

}
