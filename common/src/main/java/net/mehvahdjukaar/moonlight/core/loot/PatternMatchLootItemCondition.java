package net.mehvahdjukaar.moonlight.core.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.moonlight.api.platform.ForgeHelper;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;
import java.util.regex.Pattern;

public record PatternMatchLootItemCondition(List<Pattern> patterns) implements LootItemCondition {

    public static final MapCodec<PatternMatchLootItemCondition> CODEC = RecordCodecBuilder.mapCodec((i) -> i.group(
            ExtraCodecs.PATTERN.listOf().fieldOf("matches").forGetter(o -> o.patterns)
    ).apply(i, PatternMatchLootItemCondition::new));

    @Override
    public boolean test(LootContext lootContext) {
        String id = ForgeHelper.getQueriedLootTableId(lootContext).toString();
        for (var p : patterns) {
            if (id.equals(p.pattern())) return true;
            if (p.matcher(id).find()) return true;
        }
        return false;
    }

    @Override
    public MapCodec<? extends LootItemCondition> codec() {
        return CODEC;
    }

}