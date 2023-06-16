package net.mehvahdjukaar.moonlight.core.mixins.forge;

import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

//sorry GLM are too cumbersome and i cant get a decent cross loader system so since we always have jsons anyways we use this.
//instead im forging to only use table references
@Mixin(LootTable.class)
public interface LootTableHackMixin {

    @Accessor("f_79109_")
    List<LootPool> getPools();
}
