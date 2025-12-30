package net.mehvahdjukaar.moonlight.core.mixins.fabric;

import net.minecraft.world.entity.ai.village.poi.PoiType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Mixin(PoiType.class)
public interface PoiTypeAccessor {

    @Mutable
    @Accessor("matchingStates")
    void setMatchingStates(Set<?> blockStates);
}
