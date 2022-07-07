package net.mehvahdjukaar.moonlight.core.mixins.forge;

import net.mehvahdjukaar.moonlight.api.entity.IExtraClientSpawnData;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(IExtraClientSpawnData.class)
public abstract interface SelfEntitySpawnDataMixin extends IEntityAdditionalSpawnData {

}
