package net.mehvahdjukaar.moonlight.core.mixins.compat;

import net.mehvahdjukaar.moonlight.api.block.IPistonMotionReact;
import net.mehvahdjukaar.moonlight.api.misc.OptionalMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.violetmoon.quark.api.IMagnetMoveAction;

@OptionalMixin(classLoaded = true, value = "org.violetmoon.quark.api.IMagnetMoveAction")
@Mixin(IPistonMotionReact.class)
public interface PistonReactQuarkMixin extends IMagnetMoveAction {
}
