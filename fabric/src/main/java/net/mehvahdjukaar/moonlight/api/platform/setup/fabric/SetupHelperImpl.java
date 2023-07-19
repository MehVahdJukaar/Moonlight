package net.mehvahdjukaar.moonlight.api.platform.setup.fabric;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.mehvahdjukaar.moonlight.api.platform.setup.IDeferredCommonSetup;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootTableReference;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

public class SetupHelperImpl {

    private static boolean isValid = true;

    private static final List<Supplier<Pack>> EXTRA_DATA_PACKS = new ArrayList<>();

    private static final Map<String, Queue<Runnable>> SETUP_STEPS = new LinkedHashMap<>();

    @ApiStatus.Internal
    public static void run() {
        isValid = false;
        SETUP_STEPS.values().forEach(q -> q.forEach(Runnable::run));

        SETUP_STEPS.clear();
    }

    public static void deferSetup(IDeferredCommonSetup mod) {
        if (!isValid) {
            throw new IllegalStateException("Defer setup must be called in mod init");
        }


        //listener
        mod.registerServerReloadListener((listener, name) -> {
            ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new IdentifiableResourceReloadListener() {
                final PreparableReloadListener instance = listener.get();

                @Override
                public ResourceLocation getFabricId() {
                    return name;
                }

                @Override
                public CompletableFuture<Void> reload(PreparationBarrier preparationBarrier, ResourceManager resourceManager, ProfilerFiller preparationsProfiler, ProfilerFiller reloadProfiler, Executor backgroundExecutor, Executor gameExecutor) {
                    return instance.reload(preparationBarrier, resourceManager, preparationsProfiler, reloadProfiler, backgroundExecutor, gameExecutor);
                }
            });
        });

        //attribute
        SETUP_STEPS.computeIfAbsent("attributes", a -> new ArrayDeque<>())
                .add(() -> mod.registerAttributes(FabricDefaultAttributeRegistry::register));

        //spawn placement
        SETUP_STEPS.computeIfAbsent("spawns", a -> new ArrayDeque<>())
                .add(() -> mod.registerSpawnPlacements(new SpawnPlacementsImpl()));

        //command
        CommandRegistrationCallback.EVENT.register(mod::registerCommands);

        //datapack
        mod.registerBuiltinDataPack(EXTRA_DATA_PACKS::add);

        //loot injects
        if (hasImpl(mod, "addLootTableInjects")) {
            LootTableEvents.MODIFY.register((resourceManager, lootManager, id, tableBuilder, source) ->
                    mod.addLootTableInjects(new IDeferredCommonSetup.LootInjectEvent() {
                        @Override
                        public ResourceLocation getTable() {
                            return id;
                        }

                        @Override
                        public void addTableReference(ResourceLocation injected) {
                            LootPool pool = LootPool.lootPool().add(LootTableReference.lootTableReference(injected)).build();
                            tableBuilder.pool(pool);
                        }
                    })
            );
        }

        //items to tabs
        SETUP_STEPS.computeIfAbsent("item_to_tabs", a -> new ArrayDeque<>()).add(() -> {
            IDeferredCommonSetup.ItemToTabEvent event = new IDeferredCommonSetup.ItemToTabEvent((tab, target, after, items) -> {
                ItemGroupEvents.modifyEntriesEvent(tab).register(entries -> {
                    if (target == null) {
                        entries.acceptAll(items);
                    } else {
                        if (after) {
                            entries.addAfter(target, items, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
                        } else {
                            entries.addBefore(target, items, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
                        }
                    }
                });
            });
            mod.addItemsToTabs(event);
        });

        var s = SETUP_STEPS.computeIfAbsent("setup", a -> new ArrayDeque<>());
        s.add(mod::setup);
        s.add(mod::asyncSetup);
    }

    static class SpawnPlacementsImpl implements IDeferredCommonSetup.SpawnPlacementEvent {
        @Override
        public <T extends Entity> void register(EntityType<T> entityType, SpawnPlacements.Type decoratorType,
                                                Heightmap.Types heightMapType, SpawnPlacements.SpawnPredicate<T> decoratorPredicate) {
            try {
                SpawnPlacements.register((EntityType<Mob>) entityType, decoratorType, heightMapType, (SpawnPlacements.SpawnPredicate<Mob>) decoratorPredicate);
            } catch (Exception e) {
                Moonlight.LOGGER.warn("Skipping placement registration for {} as its not of Mob type", entityType);
            }
        }
    }


    //just using for expensive stuff to justify the added overhead
    public static boolean hasImpl(IDeferredCommonSetup mod, String name) {
        return Utils.isMethodImplemented(IDeferredCommonSetup.class, mod.getClass(), name);
    }
}
