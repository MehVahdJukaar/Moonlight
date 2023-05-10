package net.mehvahdjukaar.moonlight.api.platform.fabric;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.mehvahdjukaar.moonlight.api.client.fabric.IFabricMenuType;
import net.mehvahdjukaar.moonlight.api.misc.RegSupplier;
import net.mehvahdjukaar.moonlight.api.misc.Registrator;
import net.mehvahdjukaar.moonlight.api.misc.TriFunction;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.misc.AntiRepostWarning;
import net.mehvahdjukaar.moonlight.core.set.fabric.BlockSetInternalImpl;
import net.mehvahdjukaar.moonlight.fabric.MLFabricSetupCallbacks;
import net.mehvahdjukaar.moonlight.fabric.ResourceConditionsBridge;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class RegHelperImpl {

    public static final Map<ResourceKey<? extends Registry<?>>, Map<String, RegistryQueue<?>>> REGISTRIES = new LinkedHashMap<>();

    private static final List<Consumer<RegHelper.AttributeEvent>> ATTRIBUTE_REGISTRATIONS = new ArrayList<>();
    private static final List<Consumer<RegHelper.SpawnPlacementEvent>> SPAWN_PLACEMENT_REGISTRATIONS = new ArrayList<>();


    public static final List<ResourceKey<? extends Registry<?>>> REG_PRIORITY = List.of(
            Registries.SOUND_EVENT, Registries.FLUID, Registries.BLOCK, Registries.PARTICLE_TYPE,
            Registries.ENTITY_TYPE, Registries.ITEM,
            Registries.BLOCK_ENTITY_TYPE, Registries.PLACEMENT_MODIFIER_TYPE, Registries.STRUCTURE_TYPE,
            Registries.STRUCTURE_PIECE, Registries.FEATURE, Registries.CONFIGURED_FEATURE,
            Registries.PLACED_FEATURE
    );

    //order is important here
    static {
        REG_PRIORITY.forEach(e -> REGISTRIES.put(e, new LinkedHashMap<>()));
    }

    //call from mod setup
    @ApiStatus.Internal
    public static void lateRegisterEntries() {
        for (var m : REGISTRIES.entrySet()) {
            var v = m.getValue();
            //freaking fabric just runs mod initializers in random order. hate this. we run in deterministic manner here
            var sorted = v.keySet().stream().sorted().toList();
            for (var s : sorted) {
                try {
                    v.get(s).initializeEntries();
                } catch (Exception e) {
                    throw new RuntimeException("Failed to initialize registry objects for namespace [" + s + "]", e);
                }
            }
            if (m.getKey() == Registries.BLOCK) {
                //dynamic block registration after all blocks
                BlockSetInternalImpl.registerEntries();
            }
        }
        //register entities attributes now
        ATTRIBUTE_REGISTRATIONS.forEach(e -> e.accept(FabricDefaultAttributeRegistry::register));
        SPAWN_PLACEMENT_REGISTRATIONS.forEach(e -> e.accept(new SpawnPlacementsImpl()));
    }

    static class SpawnPlacementsImpl implements RegHelper.SpawnPlacementEvent {
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

    public static void finishRegistration(String modId) {
        for (var r : REGISTRIES.entrySet()) {
            var m = r.getValue();
            var v = m.get(modId);
            if (v != null) {
                v.initializeEntries();
                m.remove(modId);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static <T, E extends T> RegSupplier<E> register(ResourceLocation name, Supplier<E> supplier, ResourceKey<? extends Registry<T>> reg) {
        if (supplier == null) {
            throw new IllegalArgumentException("Registry entry Supplier for " + name + " can't be null");
        }
        if (name.getNamespace().equals("minecraft")) {
            throw new IllegalArgumentException("Registering under minecraft namespace is not supported");
        }
        String modId = name.getNamespace();
        var m = REGISTRIES.computeIfAbsent(reg, h -> new LinkedHashMap<>());
        RegistryQueue<T> registry = (RegistryQueue<T>) m.computeIfAbsent(modId,
                c -> {
                    if (PlatHelper.getPhysicalSide().isClient()) AntiRepostWarning.addMod(modId);

                    return new RegistryQueue<>(reg);
                });
        return registry.add(supplier, name);
    }

    public static <T, E extends T> RegSupplier<E> registerAsync(ResourceLocation name, Supplier<E> supplier, ResourceKey<? extends Registry<T>> reg) {
        RegistryQueue.EntryWrapper<E, T> entry = new RegistryQueue.EntryWrapper<>(name, supplier, reg);
        entry.initialize();
        return entry;
    }

    public static <T> void registerInBatch(Registry<T> reg, Consumer<Registrator<T>> eventListener) {
        var m = REGISTRIES.computeIfAbsent(reg.key(), h -> new LinkedHashMap<>());
        RegistryQueue<T> registry = (RegistryQueue<T>) m.computeIfAbsent("a", c -> new RegistryQueue<>(reg.key()));
        registry.add(eventListener);
    }

    public static <C extends AbstractContainerMenu> RegSupplier<MenuType<C>> registerMenuType(
            ResourceLocation name,
            TriFunction<Integer, Inventory, FriendlyByteBuf, C> containerFactory) {
        return register(name, () -> IFabricMenuType.create(containerFactory::apply), Registries.MENU);
    }

    public static <T extends Entity> RegSupplier<EntityType<T>> registerEntityType(ResourceLocation name, EntityType.EntityFactory<T> factory, MobCategory category, float width, float height, int clientTrackingRange, int updateInterval) {
        Supplier<EntityType<T>> s = () -> EntityType.Builder.of(factory, category).sized(width, height).build(name.toString());
        return register(name, s, Registries.ENTITY_TYPE);
    }

    public static void registerItemBurnTime(Item item, int burnTime) {
        FuelRegistry.INSTANCE.add(item, burnTime);
    }

    public static void registerBlockFlammability(Block item, int fireSpread, int flammability) {
        FlammableBlockRegistry.getDefaultInstance().add(item, fireSpread, flammability);
    }

    public static void registerVillagerTrades(VillagerProfession profession, int level, Consumer<List<VillagerTrades.ItemListing>> factories) {
        MLFabricSetupCallbacks.BEFORE_COMMON_SETUP.add(() -> TradeOfferHelper.registerVillagerOffers(profession, level, factories));
    }

    public static void registerWanderingTraderTrades(int level, Consumer<List<VillagerTrades.ItemListing>> factories) {
        //this just runs immediately... needs to run on mod setup instead
        MLFabricSetupCallbacks.BEFORE_COMMON_SETUP.add(() -> TradeOfferHelper.registerWanderingTraderOffers(level, factories));
    }

    public static void addAttributeRegistration(Consumer<RegHelper.AttributeEvent> eventListener) {
        ATTRIBUTE_REGISTRATIONS.add(eventListener);
    }

    public static void addSpawnPlacementsRegistration(Consumer<RegHelper.SpawnPlacementEvent> eventListener) {
        SPAWN_PLACEMENT_REGISTRATIONS.add(eventListener);
    }

    public static void addCommandRegistration(RegHelper.CommandRegistration eventListener) {
        CommandRegistrationCallback.EVENT.register(eventListener::accept);
    }


    public static void registerSimpleRecipeCondition(ResourceLocation id, Predicate<String> predicate) {
        ResourceConditionsBridge.registerSimple(id, predicate);
    }


    public static <T extends CraftingRecipe> RegSupplier<RecipeSerializer<T>> registerSpecialRecipe(ResourceLocation name, SimpleCraftingRecipeSerializer.Factory<T> factory) {
        return RegHelper.registerRecipeSerializer(name, () -> new SimpleCraftingRecipeSerializer<>(factory));
    }

    public static <T extends Fluid> RegSupplier<T> registerFluid(ResourceLocation name, Supplier<T> fluid) {
        return register(name, fluid, Registries.FLUID);
    }


    public static void addItemsToTabsRegistration(Consumer<RegHelper.ItemToTabEvent> eventListener) {
        MLFabricSetupCallbacks.BEFORE_COMMON_SETUP.add(() -> {
            RegHelper.ItemToTabEvent event = new RegHelper.ItemToTabEvent((tab, target, after, items) -> {
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
            eventListener.accept(event);
        });
    }

    public static Supplier<CreativeModeTab> registerCreativeModeTab(ResourceLocation name, List<Object> beforeEntries, List<Object> afterEntries, Consumer<CreativeModeTab.Builder> configurator) {
        var builder = FabricItemGroup.builder(name);
        configurator.accept(builder);
        CreativeModeTab tab = builder.build();
        return () -> tab;
    }

    public static void addItemsToTab(CreativeModeTab tab, @Nullable Predicate<ItemStack> addAfter, ItemStack... items) {
        ItemGroupEvents.modifyEntriesEvent(tab).register(entries -> {
            if (addAfter == null) {
                entries.acceptAll(List.of(items));
            } else {
                entries.addAfter(addAfter, Arrays.stream(items).toList(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            }
        });
    }


}
