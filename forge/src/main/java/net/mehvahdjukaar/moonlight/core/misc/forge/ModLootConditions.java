package net.mehvahdjukaar.moonlight.core.misc.forge;

import com.google.gson.*;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.client.KeyMapping;
import net.minecraft.core.Registry;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.Serializer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ModLootConditions {

    public static void register(){
    }

    public static final Supplier<LootItemConditionType> ICONDITION_LOOT_CONDITION = RegHelper.register(Moonlight.res("iconditions"),
            () -> new LootItemConditionType(new IConditionLootCondition.ConditionSerializer()), Registry.LOOT_CONDITION_TYPE);
    
    public record IConditionLootCondition(List<ICondition> conditions) implements LootItemCondition {
        @Override
        public boolean test(LootContext lootContext) {
            for (var c : conditions) {
                if (!c.test(ICondition.IContext.EMPTY)) return false;
            }
            return true;
        }

        @Nonnull
        @Override
        public LootItemConditionType getType() {
            return ICONDITION_LOOT_CONDITION.get();
        }

        public record ConditionSerializer() implements Serializer<IConditionLootCondition> {
            @Override
            public void serialize(@Nonnull JsonObject json, @Nonnull IConditionLootCondition value, @Nonnull JsonSerializationContext context) {
                JsonArray ja = new JsonArray();
                for (var c : value.conditions) {
                    ja.add(CraftingHelper.serialize(c));
                }
                json.add("values", ja);
            }

            @Nonnull
            @Override
            public IConditionLootCondition deserialize(@Nonnull JsonObject json, @Nonnull JsonDeserializationContext context) {
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
}
