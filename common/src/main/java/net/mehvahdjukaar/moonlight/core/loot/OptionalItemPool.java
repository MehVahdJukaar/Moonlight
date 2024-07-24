package net.mehvahdjukaar.moonlight.core.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.moonlight.api.MoonlightRegistry;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class OptionalItemPool extends LootPoolSingletonContainer {

    public static final MapCodec<OptionalItemPool> CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(o -> o.tagOrItemId)
    ).and(singletonFields(instance)).apply(instance, OptionalItemPool::new));

    @Nullable
    private final Item item;
    private final String tagOrItemId;

    OptionalItemPool(String tagOrItemId, int quality, int weight, List<LootItemCondition> lootItemConditions, List<LootItemFunction> lootItemFunctions) {
        super(quality, weight, disableIfInvalid(tagOrItemId, lootItemConditions), lootItemFunctions);
        this.item = getOptional(tagOrItemId);
        this.tagOrItemId = tagOrItemId;
    }

    @Nullable
    private static Item getOptional(String res) {
        if (res.startsWith("#")) {
            TagKey<Item> key = TagKey.create(Registries.ITEM, ResourceLocation.parse(res.substring(1)));
            //gets first valid tagged item
            for (var v : BuiltInRegistries.ITEM.getTagOrEmpty(key)) return v.value();
            return null;
        }
        return BuiltInRegistries.ITEM.getOptional(ResourceLocation.parse(res)).orElse(null);
    }

    //hacky
    private static List<LootItemCondition> disableIfInvalid(String res, List<LootItemCondition> lootItemConditions) {
        if (getOptional(res) == null) {
            List<LootItemCondition> newCond = new ArrayList<>();
            newCond.add(LootItemRandomChanceCondition.randomChance(0).build()); //always false
            newCond.addAll(lootItemConditions);
            return newCond;
        }
        return lootItemConditions;
    }

    @Override
    public LootPoolEntryType getType() {
        return MoonlightRegistry.LAZY_ITEM.get();
    }

    @Override
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

}