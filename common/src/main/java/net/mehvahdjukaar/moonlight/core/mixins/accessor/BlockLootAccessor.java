package net.mehvahdjukaar.moonlight.core.mixins.accessor;

import net.minecraft.data.loot.BlockLoot;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BlockLoot.class)
public interface BlockLootAccessor {

    @Invoker("createSingleItemTable")
    static LootTable.Builder invokeCreateSingleItemTable(ItemLike item) {
        throw new AssertionError();
    }
}
