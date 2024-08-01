package net.mehvahdjukaar.moonlight.core.misc.forge;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.neoforged.neoforge.common.conditions.ICondition;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public class ModLootConditions {

    public static void register() {
    }

    public static final Supplier<LootItemConditionType> ICONDITION_LOOT_CONDITION = RegHelper.registerLootCondition(
            Moonlight.res("iconditions"), () -> IConditionLootCondition.CODEC);
    public static final Supplier<LootItemConditionType> PATTERN_MATCH_CONDITION = RegHelper.registerLootCondition(
            Moonlight.res("loot_table_id_patter"), () -> PatternMatchCondition.CODEC);

    public record IConditionLootCondition(List<ICondition> conditions) implements LootItemCondition {

        public static final MapCodec<IConditionLootCondition> CODEC = RecordCodecBuilder.mapCodec((i) -> i.group(
                ICondition.LIST_CODEC.fieldOf("values").forGetter(o -> o.conditions)
        ).apply(i, IConditionLootCondition::new));

        @Override
        public boolean test(LootContext lootContext) {
            for (var c : conditions) {
                if (!c.test(ICondition.IContext.EMPTY)) return false;
            }
            return true;
        }

        @NotNull
        @Override
        public LootItemConditionType getType() {
            return ICONDITION_LOOT_CONDITION.get();
        }
    }


    public record PatternMatchCondition(List<Pattern> patterns) implements LootItemCondition {

        public static final MapCodec<PatternMatchCondition> CODEC = RecordCodecBuilder.mapCodec((i) -> i.group(
                ExtraCodecs.PATTERN.listOf().fieldOf("matches").forGetter(o -> o.patterns)
        ).apply(i, PatternMatchCondition::new));

        @Override
        public boolean test(LootContext lootContext) {
            String id = lootContext.getQueriedLootTableId().toString();
            for (var p : patterns) {
                if (id.equals(p.pattern())) return true;
                if (p.matcher(id).find()) return true;
            }
            return false;
        }

        @Nonnull
        @Override
        public LootItemConditionType getType() {
            return PATTERN_MATCH_CONDITION.get();
        }

    }
}
