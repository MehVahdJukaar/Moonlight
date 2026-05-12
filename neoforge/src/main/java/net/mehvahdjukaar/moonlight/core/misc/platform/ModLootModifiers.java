package net.mehvahdjukaar.moonlight.core.misc.platform;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.platform.MoonlightForge;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntries;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ModLootModifiers {

    public static void register() {
        LOOT_MODIFIERS.register(MoonlightForge.getCurrentBus());
    }

    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> LOOT_MODIFIERS = DeferredRegister.create(
            NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, Moonlight.MOD_ID);

    public static final DeferredHolder<?, ?> ADD_ITEM_GLM =
            LOOT_MODIFIERS.register("add_item", () -> AddItemModifier.CODEC);

    public static final DeferredHolder<?, ?> REPLACE_ITEM_GLM =
            LOOT_MODIFIERS.register("replace_item", () -> ReplaceItemModifier.CODEC);

    public static class AddItemModifier extends LootModifier {

        public static final MapCodec<AddItemModifier> CODEC =
                RecordCodecBuilder.mapCodec(inst -> codecStart(inst).and(
                        ItemStack.CODEC.fieldOf("item").forGetter(m -> m.addedItemStack)
                ).apply(inst, AddItemModifier::new));

        private final ItemStack addedItemStack;


        public AddItemModifier(LootItemCondition[] conditionsIn, ItemStack addedItemStack) {
            super(conditionsIn);
            this.addedItemStack = addedItemStack;
        }

        @NotNull
        @Override
        protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
            ItemStack addedStack = addedItemStack.copy();

            if (addedStack.getCount() < addedStack.getMaxStackSize()) {
                generatedLoot.add(addedStack);
            } else {
                int i = addedStack.getCount();

                while (i > 0) {
                    ItemStack subStack = addedStack.copy();
                    subStack.setCount(Math.min(addedStack.getMaxStackSize(), i));
                    i -= subStack.getCount();
                    generatedLoot.add(subStack);
                }
            }
            return generatedLoot;
        }


        @Override
        public MapCodec<? extends IGlobalLootModifier> codec() {
            return CODEC;
        }
    }

    public static class ReplaceItemModifier extends LootModifier {

        public static final MapCodec<ReplaceItemModifier> CODEC = RecordCodecBuilder.<ReplaceItemModifier>mapCodec(inst -> codecStart(inst)
                .and(
                        inst.group(
                                LootPoolEntries.CODEC.optionalFieldOf("loot_pool").forGetter(m -> Optional.ofNullable(m.lootPool)),
                                ItemStack.CODEC.optionalFieldOf("item").forGetter(m -> Optional.ofNullable(m.itemStack)),
                                Codec.INT.optionalFieldOf("max_matches", Integer.MAX_VALUE).forGetter(m -> m.maxMatches),
                                ItemPredicate.CODEC.optionalFieldOf("target").forGetter(m -> m.itemPredicate)
                        )

                )
                .apply(inst, ReplaceItemModifier::new)

        ).validate(o -> {
            if (o.lootPool == null ^ o.itemStack == null) {
                return DataResult.error(() -> "Only either loot_pool or item field must be filled");
            }
            return DataResult.success(o);
        });

        private @Nullable LootPoolEntryContainer lootPool;
        private @Nullable ItemStack itemStack;
        private final int maxMatches;
        private final Optional<ItemPredicate> itemPredicate;

        protected ReplaceItemModifier(LootItemCondition[] conditionsIn, Optional<LootPoolEntryContainer> lootPool, Optional<ItemStack> addedItemStack, int maxMatches, Optional<ItemPredicate> itemPredicate) {
            super(conditionsIn);
            this.itemStack = addedItemStack.orElse(null);
            this.lootPool = lootPool.orElse(null);
            this.itemPredicate = itemPredicate;
            this.maxMatches = maxMatches;
        }

        @NotNull
        @Override
        protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
            if (!generatedLoot.isEmpty()) {
                int matches = 0;
                List<ItemStack> toAdd = new ArrayList<>();
                for (int i = 0; i < generatedLoot.size(); i++) {
                    ItemStack stack = generatedLoot.get(i);
                    if (itemPredicate.isEmpty() || itemPredicate.get().test(stack)) {
                        matches++;
                        if (itemStack != null) {

                            generatedLoot.set(i, itemStack.copy());
                        } else if (lootPool != null) {
                            generatedLoot.remove(i);
                            lootPool.expand(context, lootPoolEntry ->
                                    lootPoolEntry.createItemStack(toAdd::add, context));
                        }

                        if (matches >= maxMatches) break;
                    }
                }

                generatedLoot.addAll(toAdd);
            }
            return generatedLoot;
        }

        @Override
        public MapCodec<? extends IGlobalLootModifier> codec() {
            return CODEC;
        }
    }

}
