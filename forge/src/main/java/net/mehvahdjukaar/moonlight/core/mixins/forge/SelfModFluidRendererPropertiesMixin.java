package net.mehvahdjukaar.moonlight.core.mixins.forge;

import net.mehvahdjukaar.moonlight.api.client.ModFluidRenderProperties;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import org.spongepowered.asm.mixin.Mixin;

//implementing is enough. they have same signature
@Mixin(ModFluidRenderProperties.class)
public abstract class SelfModFluidRendererPropertiesMixin implements IClientFluidTypeExtensions {
}
