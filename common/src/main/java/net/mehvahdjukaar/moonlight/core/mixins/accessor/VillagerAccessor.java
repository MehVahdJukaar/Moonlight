package net.mehvahdjukaar.moonlight.core.mixins.accessor;

import com.google.common.collect.ImmutableList;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Villager.class)
public interface  VillagerAccessor {

    //TODO: move to access wideners
    @Accessor("MEMORY_TYPES")
    static ImmutableList<MemoryModuleType<?>> getMemoryTypes() {
        throw new AssertionError();
    }

    @Accessor("MEMORY_TYPES")
    static void setMemoryTypes(ImmutableList<MemoryModuleType<?>> biomes) {
        throw new AssertionError();
    }
}
