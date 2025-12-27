package net.mehvahdjukaar.moonlight.core.mixins.compat;

import com.bawnorton.mixinsquared.TargetHandler;
import net.mehvahdjukaar.moonlight.api.misc.OptionalMixin;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.misc.FakeServerLevel;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * This exception here is thrown by VS when it detects a ServerLevel initialized in a phase it doesn't like.
 * This is essentially a forced crash, a restriction added by it to prevent certain issues with their implementation.
 * It remains a forced *arbitrary* constrain which it add, instead of actually fixing the underlying implementation issue.
 * Since our ServerLevels are dummy levels are are fine just ignoring them.
 * I have reported this thousand of times and they have refused to even acknowledge the issue...
 * Still we cant just "fix" this for them for free, why is my time less precious than theirs after all? This is why a log line redirecting to the issue page is created
 * About the injection point, this is hacky, injecting where the issue is thrown is a lot trickier so we prevent the level from being tarcked in the first place
 */
@OptionalMixin(value = "org.valkyrienskies.mod.mixin.server.world.MixinServerLevel")
@Mixin(value = ServerLevel.class, priority = 1500)
public class VSYeetArbitraryCrash {

    @TargetHandler(
            mixin = "org.valkyrienskies.mod.mixin.server.world.MixinServerLevel",
            name = "onInit"
    )
    @Inject(
            method = "@MixinSquared:Handler",
            require = 0,
            at = @At(value = "HEAD"),
            cancellable = true)
    private void ml$cockBlockArbitraryCrash(CallbackInfo originalCi, CallbackInfo ci) {
        if ((Object) this instanceof FakeServerLevel) {

            Moonlight.LOGGER.error("Moonlight Lib applied some ungodly hacks to prevent a forced arbitrary crash from the mod Valkyrien Skies! \n" +
                    "See https://github.com/ValkyrienSkies/Valkyrien-Skies-2/issues/1488 for more info.");
            ci.cancel();
        }

    }
}
