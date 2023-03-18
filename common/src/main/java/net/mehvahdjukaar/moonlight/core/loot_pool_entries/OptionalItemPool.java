package net.mehvahdjukaar.moonlight.core.loot_pool_entries;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class OptionalItemPool extends LootPoolSingletonContainer {
    @Nullable
    private final Item item;
    private final String res;

    OptionalItemPool(String res, int i, int j, LootItemCondition[] lootItemConditions, LootItemFunction[] lootItemFunctions) {
        super(i, j, disableIfInvalid(res, lootItemConditions), lootItemFunctions);
        this.item = getOptional(res);
        this.res = res;
    }

    @Nullable
    private static Item getOptional(String res) {
        if (res.startsWith("#")) {
            TagKey<Item> key = TagKey.create(Registries.ITEM, new ResourceLocation(res.substring(1)));
            for (var v : BuiltInRegistries.ITEM.getTagOrEmpty(key)) return v.value();
            return null;
        }
        return BuiltInRegistries.ITEM.getOptional(new ResourceLocation(res)).orElse(null);
    }

    //hacky
    private static LootItemCondition[] disableIfInvalid(String res, LootItemCondition[] lootItemConditions) {
        if (getOptional(res) == null) {
            List<LootItemCondition> newCond = new ArrayList<>();
            newCond.add(LootItemRandomChanceCondition.randomChance(0).build()); //always false
            newCond.addAll(List.of(lootItemConditions));
            return newCond.toArray(new LootItemCondition[0]);
        }
        return lootItemConditions;
    }

    public LootPoolEntryType getType() {
        return  ModLootPoolEntries.LAZY_ITEM.get();
    }

    public void createItemStack(Consumer<ItemStack> stackConsumer, LootContext lootContext) {
        if (this.item != null) {
            stackConsumer.accept(new ItemStack(this.item));
        } else {
            Moonlight.LOGGER.warn("Tried to add an item from a disabled OptionalLootPoolEntry");
        }
    }

    public static Builder<?> lootTableOptionalItem(String itemRes) {
        return simpleBuilder((i, j, lootItemConditions, lootItemFunctions) -> new OptionalItemPool(itemRes, i, j, lootItemConditions, lootItemFunctions));
    }

    public static class Serializer extends LootPoolSingletonContainer.Serializer<OptionalItemPool> {

        @Override
        public void serializeCustom(JsonObject object, OptionalItemPool context, JsonSerializationContext conditions) {
            super.serializeCustom(object, context, conditions);
            object.addProperty("name", context.res.toString());
        }

        protected OptionalItemPool deserialize(
                JsonObject object, JsonDeserializationContext context, int weight, int quality, LootItemCondition[] conditions, LootItemFunction[] functions
        ) {
            String item = getItemLocation(object, "name");
            return new OptionalItemPool(item, weight, quality, conditions, functions);
        }

        private static String getItemLocation(JsonObject json, String memberName) {
            if (json.has(memberName)) {
                return GsonHelper.getAsString(json, memberName);
            } else {
                throw new JsonSyntaxException("Missing " + memberName + ", expected to find an item");
            }
        }
    }
}