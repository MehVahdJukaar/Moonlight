package net.mehvahdjukaar.moonlight.api.platform.setup;

import com.mojang.brigadier.CommandDispatcher;
import net.mehvahdjukaar.moonlight.api.misc.QuadConsumer;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.levelgen.Heightmap;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface IDeferredCommonSetup {

    default void setup() {
    }

    default void asyncSetup(){

    }

    default void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context,
                                  Commands.CommandSelection selection) {
    }

    default void registerAttributes(AttributeEvent event) {
    }

    @FunctionalInterface
    interface AttributeEvent {
        void register(EntityType<? extends LivingEntity> type, AttributeSupplier.Builder builder);
    }


    default void registerSpawnPlacements(SpawnPlacementEvent event) {
    }

    @FunctionalInterface
    interface SpawnPlacementEvent {
        <T extends Entity> void register(EntityType<T> entityType, SpawnPlacements.Type decoratorType,
                                         Heightmap.Types heightMapType, SpawnPlacements.SpawnPredicate<T> decoratorPredicate);
    }


    default void registerServerReloadListener(ReloadListenerEvent event) {
    }

    @FunctionalInterface
    interface ReloadListenerEvent {
        void register(Supplier<PreparableReloadListener> listener, ResourceLocation location);
    }

    default void registerBuiltinDataPack(DataPackEvent event) {
    }

    @FunctionalInterface
    interface DataPackEvent {
        void register(Supplier<Pack> packSupplier);
    }

    default void addItemsToTabs(ItemToTabEvent event) {
    }

    record ItemToTabEvent(
            QuadConsumer<ResourceKey<CreativeModeTab>, @Nullable Predicate<ItemStack>, Boolean, Collection<ItemStack>> action) {


        public void add(ResourceKey<CreativeModeTab> tab, ItemLike... items) {
            addAfter(tab, null, items);
        }

        public void add(ResourceKey<CreativeModeTab> tab, ItemStack... items) {
            addAfter(tab, null, items);
        }

        public void addAfter(ResourceKey<CreativeModeTab> tab, Predicate<ItemStack> target, ItemLike... items) {
            action.accept(tab, target, true, Arrays.stream(items).map(i -> i.asItem().getDefaultInstance()).toList());
        }

        public void addAfter(ResourceKey<CreativeModeTab> tab, Predicate<ItemStack> target, ItemStack... items) {
            action.accept(tab, target, true, List.of(items));
        }

        public void addBefore(ResourceKey<CreativeModeTab> tab, Predicate<ItemStack> target, ItemLike... items) {
            action.accept(tab, target, false, Arrays.stream(items).map(i -> i.asItem().getDefaultInstance()).toList());
        }

        public void addBefore(ResourceKey<CreativeModeTab> tab, Predicate<ItemStack> target, ItemStack... items) {
            action.accept(tab, target, false, List.of(items));
        }
    }

    interface LootInjectEvent {
        ResourceLocation getTable();
        void addTableReference(ResourceLocation injected);
    }

    /**
     * This uses fabric loot modify event and something equivalent to the old forge loot modift event.
     * It simply adds a loot table reference pool to the target table
     */
    default void addLootTableInjects(LootInjectEvent event) {
    }
}
