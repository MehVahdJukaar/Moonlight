package net.mehvahdjukaar.moonlight.mixins.forge;

import net.mehvahdjukaar.moonlight.network.IExtraClientSpawnData;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(IExtraClientSpawnData.class)
public abstract interface SelfEntitySpawnDataMixin extends IEntityAdditionalSpawnData {

}
