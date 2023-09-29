package net.mehvahdjukaar.moonlight.core.misc.forge;

import com.google.gson.*;
import net.mehvahdjukaar.moonlight.api.map.MapDecorationRegistry;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.client.KeyMapping;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.Serializer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;

import org.jetbrains.annotations.NotNull;
import pepjebs.mapatlases.client.screen.PinButton;
import pepjebs.mapatlases.integration.MoonlightCompat;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public class ModLootConditions {

    public static void register(){
    }

    public static final Supplier<LootItemConditionType> ICONDITION_LOOT_CONDITION = RegHelper.register(Moonlight.res("iconditions"),
            () -> new LootItemConditionType(new IConditionLootCondition.ConditionSerializer()), Registries.LOOT_CONDITION_TYPE);
    public static final Supplier<LootItemConditionType> PATTERN_MATCH_CONDITION = RegHelper.register(Moonlight.res("loot_table_id_patter"),
            () -> new LootItemConditionType(new PatternMatchCondition.ConditionSerializer()), Registries.LOOT_CONDITION_TYPE);

    public record IConditionLootCondition(List<ICondition> conditions) implements LootItemCondition {
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

        public record ConditionSerializer() implements Serializer<IConditionLootCondition> {
            @Override
            public void serialize(@NotNull JsonObject json, @NotNull IConditionLootCondition value, @NotNull JsonSerializationContext context) {
                JsonArray ja = new JsonArray();
                for (var c : value.conditions) {
                    ja.add(CraftingHelper.serialize(c));
                }
                json.add("values", ja);
            }

            @NotNull
            @Override
            public IConditionLootCondition deserialize(@NotNull JsonObject json, @NotNull JsonDeserializationContext context) {
                var ja = GsonHelper.getAsJsonArray(json, "values");
                List<ICondition> l = new ArrayList<>();
                for (var c : ja) {
                    if (!c.isJsonObject())
                        throw new JsonSyntaxException("Conditions must be an array of JsonObjects");
                    l.add(CraftingHelper.getCondition(c.getAsJsonObject()));
                }
                return new IConditionLootCondition(l);
            }
        }
    }


    public record PatternMatchCondition(List<Pattern> patterns) implements LootItemCondition {

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

        public record ConditionSerializer() implements Serializer<PatternMatchCondition> {
            @Override
            public void serialize(@Nonnull JsonObject json, @Nonnull PatternMatchCondition value, @Nonnull JsonSerializationContext context) {
                JsonArray ja = new JsonArray();
                for (var c : value.patterns) {
                    ja.add(c.pattern());
                }
                json.add("matches", ja);
            }

            @Nonnull
            @Override
            public PatternMatchCondition deserialize(@Nonnull JsonObject json, @Nonnull JsonDeserializationContext context) {
                var ja = GsonHelper.getAsJsonArray(json, "matches");
                List<Pattern> l = new ArrayList<>();
                for (var c : ja) {
                    l.add(Pattern.compile(c.getAsString()));
                }
                return new PatternMatchCondition(l);
            }
        }
    }


}
