package net.mehvahdjukaar.moonlight.core.loot_pool_entries;

//TODO: finish
/*
//remove stuff that has no tab
public class TabFilteredPool extends LootPoolSingletonContainer {

    private final LootPool pool;

    TabFilteredPool(LootPool pools, int i, int j, LootItemCondition[] lootItemConditions, LootItemFunction[] lootItemFunctions) {
        super(i, j, lootItemConditions, lootItemFunctions);
        this.pool = pools;
    }

    @Override
    public LootPoolEntryType getType() {
        return  ModLootPoolEntries.FILTERED_POOLS.get();
    }

    @Override
    public void createItemStack(Consumer<ItemStack> stackConsumer, LootContext lootContext) {
    }

    public static Builder<?> lootTableOptionalItem(String itemRes) {
        return simpleBuilder((i, j, lootItemConditions, lootItemFunctions) -> new TabFilteredPool(itemRes, i, j, lootItemConditions, lootItemFunctions));
    }

    public static class Serializer extends LootPoolSingletonContainer.Serializer<TabFilteredPool> {

        @Override
        public void serializeCustom(JsonObject object, TabFilteredPool context, JsonSerializationContext conditions) {
            super.serializeCustom(object, context, conditions);
            object.addProperty("name", context.pool.toString());
        }

        protected TabFilteredPool deserialize(
                JsonObject object, JsonDeserializationContext context, int weight, int quality, LootItemCondition[] conditions, LootItemFunction[] functions
        ) {
            String item = getItemLocation(object, "name");
            return new TabFilteredPool(item, weight, quality, conditions, functions);
        }

        private static String getItemLocation(JsonObject json, String memberName) {
            if (json.has(memberName)) {
                return GsonHelper.getAsString(json, memberName);
            } else {
                throw new JsonSyntaxException("Missing " + memberName + ", expected to find an item");
            }
        }
    }
}*/