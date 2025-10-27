package net.mehvahdjukaar.moonlight.core.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.moonlight.api.MoonlightRegistry;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.core.misc.LoaderCondition;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import org.jetbrains.annotations.NotNull;

public record ResourceLootItemCondition(LoaderCondition condition) implements LootItemCondition {

    public static final MapCodec<ResourceLootItemCondition> CODEC = RecordCodecBuilder.mapCodec((i) -> i.group(
            PlatHelper.getConditionCodec().forGetter(o -> o.condition)
    ).apply(i, ResourceLootItemCondition::new));

    @Override
    public boolean test(LootContext lootContext) {
        return condition.test(lootContext.getLevel().registryAccess());
    }

    @NotNull
    @Override
    public LootItemConditionType getType() {
        return MoonlightRegistry.RESOURCE_CONDITION_LOOT_ITEM_CONDITION.get();
    }
}
