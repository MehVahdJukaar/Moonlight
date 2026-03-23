package net.mehvahdjukaar.moonlight.core.mixins.accessor;

import net.minecraft.world.entity.ai.village.poi.PoiType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Mixin(PoiType.class)
public interface PoiTypeAccessor {

    @Accessor("matchingStates")
    @Mutable
    void setMatchingStates(Set<?> blockStates);
}
