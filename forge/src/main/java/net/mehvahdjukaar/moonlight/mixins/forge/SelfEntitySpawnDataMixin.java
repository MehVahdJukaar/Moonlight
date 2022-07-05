package net.mehvahdjukaar.moonlight.mixins.forge;

import net.mehvahdjukaar.moonlight.network.IExtraClientSpawnData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(IExtraClientSpawnData.class)
public abstract class SelfEntitySpawnDataMixin implements IEntityAdditionalSpawnData {

}
