package net.mehvahdjukaar.moonlight.core.misc.forge;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.forge.MoonlightForge;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.NotNull;
import java.util.function.Supplier;

public class ModLootModifiers {

    public static void register() {
        LOOT_MODIFIERS.register(MoonlightForge.getCurrentBus());
    }

    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT_MODIFIERS = DeferredRegister.create(
            NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, Moonlight.MOD_ID);

    public static final DeferredHolder<?,?> ADD_ITEM_GLM =
            LOOT_MODIFIERS.register("add_item", AddItemModifier.CODEC);

    public static final DeferredHolder<?,?> REPLACE_ITEM_GLM =
            LOOT_MODIFIERS.register("replace_item", ReplaceItemModifier.CODEC);

    public static final DeferredHolder<?,?> ADD_TABLE =
            LOOT_MODIFIERS.register("add_loot_table", AddTableModifier.CODEC);

    public static class AddItemModifier extends LootModifier {

        public static final Supplier<Codec<AddItemModifier>> CODEC = Suppliers.memoize(() ->
                RecordCodecBuilder.create(inst -> codecStart(inst).and(
                        ItemStack.CODEC.fieldOf("item").forGetter(m -> m.addedItemStack)
                ).apply(inst, AddItemModifier::new)));

        private final ItemStack addedItemStack;


        protected AddItemModifier(LootItemCondition[] conditionsIn, ItemStack addedItemStack) {
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
        public Codec<? extends IGlobalLootModifier> codec() {
            return CODEC.get();
        }
    }

    public static class ReplaceItemModifier extends LootModifier {

        public static final Supplier<Codec<ReplaceItemModifier>> CODEC = Suppliers.memoize(() -> RecordCodecBuilder.create(inst -> codecStart(inst).and(
                        ItemStack.CODEC.fieldOf("item").forGetter(m -> m.itemStack)
                ).apply(inst, ReplaceItemModifier::new)
        ));

        private final ItemStack itemStack;

        protected ReplaceItemModifier(LootItemCondition[] conditionsIn, ItemStack addedItemStack) {
            super(conditionsIn);
            this.itemStack = addedItemStack;
        }

        @NotNull
        @Override
        protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
            if (!generatedLoot.isEmpty()) {
                generatedLoot.set(0, itemStack.copy());
            }
            return generatedLoot;
        }

        @Override
        public Codec<? extends IGlobalLootModifier> codec() {
            return CODEC.get();
        }
    }


    public static class AddTableModifier extends LootModifier {

        public static final Supplier<Codec<AddTableModifier>> CODEC = Suppliers.memoize(() -> RecordCodecBuilder.create(inst -> codecStart(inst).and(
                        ResourceLocation.CODEC.fieldOf("loot_table").forGetter(m -> m.injectTableId)
                ).apply(inst, AddTableModifier::new)
        ));

        private final ResourceLocation injectTableId;

        protected AddTableModifier(LootItemCondition[] conditionsIn, ResourceLocation addedItemStack) {
            super(conditionsIn);
            this.injectTableId = addedItemStack;
        }

        @NotNull
        @Override
        protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
            LootTable lootTable = context.getLevel().getServer().getLootData().getLootTable(injectTableId);
            lootTable.getRandomItems(context, generatedLoot::add);
            return generatedLoot;
        }

        @Override
        public Codec<? extends IGlobalLootModifier> codec() {
            return CODEC.get();
        }
    }
}
