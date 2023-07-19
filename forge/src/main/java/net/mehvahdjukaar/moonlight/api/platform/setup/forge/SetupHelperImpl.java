package net.mehvahdjukaar.moonlight.api.platform.setup.forge;

import com.google.common.collect.Lists;
import net.mehvahdjukaar.moonlight.api.platform.setup.IDeferredCommonSetup;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootTableReference;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.*;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.ArrayList;
import java.util.function.Consumer;

public class SetupHelperImpl {

    public static void deferSetup(IDeferredCommonSetup mod) {
        IEventBus forgeBus = MinecraftForge.EVENT_BUS;
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        //setup
        Consumer<FMLCommonSetupEvent> setup = event -> {
            event.enqueueWork(mod::setup);
            mod.asyncSetup();
        };
        bus.addListener(setup);

        //listener
        if (hasImpl(mod, "registerServerReloadListener")) {
            Consumer<AddReloadListenerEvent> listener = event -> {
                mod.registerServerReloadListener((s, r) -> event.addListener(s.get()));
            };
            forgeBus.addListener(listener);
        }

        //attribute
        Consumer<EntityAttributeCreationEvent> attribute = event -> {
            mod.registerAttributes((e, b) -> event.put(e, b.build()));
        };
        bus.addListener(attribute);

        //spawn placement
        Consumer<SpawnPlacementRegisterEvent> spawn = event -> {
            mod.registerSpawnPlacements(new PlacementEventImpl(event));
        };
        bus.addListener(spawn);

        //command
        if (hasImpl(mod, "registerCommands")) {
            Consumer<RegisterCommandsEvent> command = event -> {
                mod.registerCommands(event.getDispatcher(), event.getBuildContext(), event.getCommandSelection());
            };
            forgeBus.addListener(command);
        }

        //datapack
        if (hasImpl(mod, "registerBuiltinDataPack")) {
            Consumer<AddPackFindersEvent> packFinder = event -> {
                if (event.getPackType() == PackType.SERVER_DATA) {
                    mod.registerBuiltinDataPack(packSupplier -> {
                        if (packSupplier != null) {
                            event.addRepositorySource(infoConsumer -> infoConsumer.accept(packSupplier.get()));
                        }
                    });
                }
            };
            bus.addListener(packFinder);
        }

        //table injects
        if (hasImpl(mod, "addLootTableInjects")) {
            Consumer<LootTableLoadEvent> lootTable = event -> {
                mod.addLootTableInjects(new IDeferredCommonSetup.LootInjectEvent() {
                    @Override
                    public ResourceLocation getTable() {
                        return event.getName();
                    }

                    @Override
                    public void addTableReference(ResourceLocation injected) {
                        LootPool pool = LootPool.lootPool().add(LootTableReference.lootTableReference(injected)).build();
                        event.getTable().addPool(pool);
                    }
                });
            };
            forgeBus.addListener(lootTable);
        }

        //item tabs
        if (hasImpl(mod, "addItemsToTabs")) {
            Consumer<BuildCreativeModeTabContentsEvent> itemsToTabs = event -> {
                IDeferredCommonSetup.ItemToTabEvent itemToTabEvent = new IDeferredCommonSetup.ItemToTabEvent((tab, target, after, items) -> {
                    if (tab != event.getTabKey()) return;

                    if (target == null) {
                        event.acceptAll(items);
                    } else {

                        var entries = event.getEntries();
                        ItemStack lastValid = null;


                        for (var e : entries) {
                            ItemStack item = e.getKey();

                            if (!item.isItemEnabled(event.getFlags())) continue;

                            boolean isValid = target.test(item);
                            if (after && lastValid != null && !isValid) {
                                var rev = Lists.reverse(new ArrayList<>(items));
                                for (var ni : rev) {
                                    entries.putAfter(lastValid, ni, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
                                }
                                return;
                            }

                            if (isValid) {
                                lastValid = item;
                            }

                            if (!after && isValid) {
                                items.forEach(ni -> entries.putBefore(item, ni, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS));
                                return;
                            }
                        }
                        //add at the end if it fails
                        for (var ni : items) {
                            entries.put(ni, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
                        }
                    }
                });
                mod.addItemsToTabs(itemToTabEvent);
            };
            FMLJavaModLoadingContext.get().getModEventBus().addListener(EventPriority.LOW, itemsToTabs);
        }
    }

    record PlacementEventImpl(SpawnPlacementRegisterEvent event) implements IDeferredCommonSetup.SpawnPlacementEvent {
        @Override
        public <T extends Entity> void register(EntityType<T> entityType, SpawnPlacements.Type decoratorType,
                                                Heightmap.Types heightMapType, SpawnPlacements.SpawnPredicate<T> decoratorPredicate) {
            event.register(entityType, decoratorType, heightMapType, decoratorPredicate, SpawnPlacementRegisterEvent.Operation.AND);
        }
    }

    //just using for expensive stuff to justify the added overhead
    public static boolean hasImpl(IDeferredCommonSetup mod, String name) {
        return Utils.isMethodImplemented(IDeferredCommonSetup.class, mod.getClass(), name);
    }


}
