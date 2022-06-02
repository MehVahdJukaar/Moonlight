package net.mehvahdjukaar.selene.mixins;

import net.minecraft.world.item.CreativeModeTab;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

//just fixed a mojang bug. Recipe builder calls this function and throws it in a resource location si it cannot contain characters like :
// and guess what, it by defaults returns its translation string which does contain just those. idk why this is even needed for advancement res loc
@Mixin(CreativeModeTab.class)
public abstract class CreativeModeTabMixin {

    //freaking Mojang
    @Inject(method = "getRecipeFolderName", at = @At("RETURN"), cancellable = true)
    public void fixId(CallbackInfoReturnable<String> cir){
        if(cir.getReturnValue().contains(":"))cir.setReturnValue("extra");
    }
}
