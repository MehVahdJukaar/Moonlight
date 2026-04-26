package net.mehvahdjukaar.moonlight.core.misc.neoforge;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.platform.MoonlightForge;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.NotNull;

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

        public static final MapCodec<ReplaceItemModifier> CODEC = RecordCodecBuilder.mapCodec(inst -> codecStart(inst)
                .and(
                        inst.group(
                                ItemStack.CODEC.fieldOf("item").forGetter(m -> m.itemStack),
                                ItemPredicate.CODEC.optionalFieldOf("target").forGetter(m -> m.itemPredicate)
                        )

                ).apply(inst, ReplaceItemModifier::new)
        );

        private final ItemStack itemStack;
        private final Optional<ItemPredicate> itemPredicate;

        protected ReplaceItemModifier(LootItemCondition[] conditionsIn, ItemStack addedItemStack, Optional<ItemPredicate> itemPredicate) {
            super(conditionsIn);
            this.itemStack = addedItemStack;
            this.itemPredicate = itemPredicate;
        }

        @NotNull
        @Override
        protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
            if (!generatedLoot.isEmpty()) {
                for (int i = 0; i < generatedLoot.size(); i++) {
                    ItemStack stack = generatedLoot.get(i);
                    if (itemPredicate.isEmpty() || itemPredicate.get().test(stack)) {
                        generatedLoot.set(i, itemStack.copy());
                        break;
                    }
                }
            }
            return generatedLoot;
        }

        @Override
        public MapCodec<? extends IGlobalLootModifier> codec() {
            return CODEC;
        }
    }

}
