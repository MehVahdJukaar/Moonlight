package net.mehvahdjukaar.moonlight.mixins.accessor;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.data.loot.BlockLoot;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(CriteriaTriggers.class)
public interface CriteriaTriggerAccessor {

    @Invoker("register")
    static <T extends CriterionTrigger<?>> T invokeRegister(T criterion) {
        throw new AssertionError();
    }
}
