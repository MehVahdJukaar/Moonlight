package net.mehvahdjukaar.moonlight.core.mixins.forge;

import net.mehvahdjukaar.moonlight.api.entity.IExtraClientSpawnData;
import net.neoforged.neoforge.entity.IEntityAdditionalSpawnData;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(IExtraClientSpawnData.class)
public interface SelfEntitySpawnDataMixin extends IEntityAdditionalSpawnData {

}
