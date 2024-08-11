package net.mehvahdjukaar.moonlight.api.platform.neoforge;

import com.google.common.base.Preconditions;
import net.mehvahdjukaar.moonlight.api.fluids.ModFlowingFluid;
import net.mehvahdjukaar.moonlight.api.misc.RegSupplier;
import net.mehvahdjukaar.moonlight.api.misc.Registrator;
import net.mehvahdjukaar.moonlight.api.misc.TriFunction;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.api.resources.recipe.neoforge.OptionalRecipeCondition;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.neoforge.MoonlightForge;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.javafmlmod.FMLModContainer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.crafting.CompoundIngredient;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.LootTableLoadEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;


public class RegHelperImpl {

    public record EntryWrapper<T>(DeferredHolder<T, ? extends T> registryObject) implements RegSupplier<T> {
        @Override
        public T get() {
            return registryObject.get();
        }

        @Override
        public ResourceLocation getId() {
            return registryObject.getId();
        }

        @Override
        public ResourceKey<T> getKey() {
            return registryObject.getKey();
        }

        @Override
        public Holder<T> getHolder() {
            return registryObject;
        }
    }

    //this might be accessed on multiple threads
    private static final Map<ResourceKey<? extends Registry<?>>, Map<String, DeferredRegister<?>>> REGISTRIES = new ConcurrentHashMap<>();

    public static <T, E extends T> RegSupplier<E> register(
            ResourceLocation name, Supplier<E> supplier, Registry<T> reg) {
        return register(name, supplier, reg.key());
    }

    @SuppressWarnings("unchecked")
    public static <T, E extends T> RegSupplier<E> register(
            ResourceLocation name, Supplier<E> supplier, ResourceKey<? extends Registry<T>> regKey) {
        if (supplier == null) {
            throw new IllegalArgumentException("Registry entry Supplier for " + name + " can't be null");
        }
        if (name.getNamespace().equals("minecraft")) {
            throw new IllegalArgumentException("Registering under minecraft namespace is not supported");
        }

        var m = REGISTRIES.computeIfAbsent(regKey, h -> new ConcurrentHashMap<>());
        String modId = name.getNamespace();
        DeferredRegister<T> registry = (DeferredRegister<T>) m.computeIfAbsent(modId, c -> {
            Moonlight.addDependent(modId);

            DeferredRegister<T> r = DeferredRegister.create(regKey, modId);
            var bus = getModEventBus(modId);
            r.register(bus);
            return r;
        });
        //forge we don't care about mod id since it's always the active container one
        DeferredHolder<T, E> register = registry.register(name.getPath(), () -> {
            //super hack for mod fluids auto registering of fluid types
            var obj = supplier.get();
            if (regKey.equals(Registries.FLUID) && obj instanceof ModFlowingFluid fluid) {
                register(name, fluid::getFluidType, NeoForgeRegistries.Keys.FLUID_TYPES);
            }
            return obj;
        });
        return (RegSupplier<E>) new EntryWrapper<>(register);
    }

    private static IEventBus getModEventBus(String modId) {
        ModList modList = ModList.get();
        //hack for condition bridge
        if (modId.equals("fabric") || modId.equals("neoforge")) modId = MoonlightForge.MOD_ID;
        Preconditions.checkNotNull(modList, "ModList was null. This means that some mod registry classes were loaded way too early, likely by mixins");
        var cont = modList.getModContainerById(modId).get();
        IEventBus bus;
        if (!(cont instanceof FMLModContainer container)) {
            Moonlight.LOGGER.warn("Failed to get mod container for mod {}", modId);
            bus = MoonlightForge.getCurrentBus();
        } else bus = container.getEventBus();
        return bus;
    }

    public static <T, E extends T> RegSupplier<E> registerAsync(ResourceLocation name, Supplier<E> supplier, ResourceKey<? extends Registry<T>> reg) {
        return register(name, supplier, reg);
    }

    public static <T> void registerInBatch(Registry<T> reg, Consumer<Registrator<T>> eventListener) {
        Consumer<RegisterEvent> eventConsumer = event -> {
            if (event.getRegistry() == reg) {
                eventListener.accept((r, o) -> Registry.register(reg, r, o));
            }
        };
        MoonlightForge.getCurrentBus().addListener(eventConsumer);
    }

    public static <C extends AbstractContainerMenu> RegSupplier<MenuType<C>> registerMenuType(
            ResourceLocation name,
            TriFunction<Integer, Inventory, FriendlyByteBuf, C> containerFactory) {
        return register(name, () -> IMenuTypeExtension.create(containerFactory::apply), Registries.MENU);
    }

    public static <T extends
            Entity> RegSupplier<EntityType<T>> registerEntityType(ResourceLocation name, EntityType.EntityFactory<T> factory, MobCategory category,
                                                                  float width, float height, int clientTrackingRange, int updateInterval) {
        return register(name, () -> EntityType.Builder.of(factory, category)
                .sized(width, height).build(name.toString()), Registries.ENTITY_TYPE);
    }

    public static <T extends Fluid> RegSupplier<T> registerFluid(ResourceLocation name, Supplier<T> fluid) {
        var f = register(name, fluid, Registries.FLUID);
        //register fluid type
        //register(name, () -> f.get().getFluidType(), NeoForgeRegistries.FLUID_TYPES);
        return f;
    }

    public static <T extends CraftingRecipe> RegSupplier<RecipeSerializer<T>> registerSpecialRecipe(ResourceLocation name, SimpleCraftingRecipeSerializer.Factory<T> factory) {
        return RegHelper.registerRecipeSerializer(name, () -> new SimpleCraftingRecipeSerializer<>(factory));
    }


    public static RegSupplier<CreativeModeTab> registerCreativeModeTab(ResourceLocation name,
                                                                       boolean hasSearchBar,
                                                                       List<ResourceLocation> afterEntries,
                                                                       List<ResourceLocation> beforeEntries,
                                                                       Consumer<CreativeModeTab.Builder> configurator) {
        return register(name, () -> {
            var b = CreativeModeTab.builder();
            configurator.accept(b);
            if (!beforeEntries.isEmpty()) {
                b.withTabsBefore(beforeEntries.toArray(ResourceLocation[]::new));
            }
            if (!afterEntries.isEmpty()) {
                b.withTabsBefore(afterEntries.toArray(ResourceLocation[]::new));
            }
            if (hasSearchBar) b.withSearchBar();
            return b.build();
        }, Registries.CREATIVE_MODE_TAB);
    }


    public static void registerItemBurnTime(Item item, int burnTime) {
    }

    public static void registerBlockFlammability(Block item, int fireSpread, int flammability) {
        ((FireBlock) Blocks.FIRE).setFlammable(item, fireSpread, flammability);
    }

    public static void addAttributeRegistration(Consumer<RegHelper.AttributeEvent> eventListener) {
        Moonlight.assertInitPhase();

        Consumer<EntityAttributeCreationEvent> eventConsumer = event -> {
            eventListener.accept((e, b) -> event.put(e, b.build()));
        };
        MoonlightForge.getCurrentBus().addListener(eventConsumer);
    }

    public static void addCommandRegistration(RegHelper.CommandRegistration eventListener) {
        Moonlight.assertInitPhase();

        Consumer<RegisterCommandsEvent> eventConsumer = event -> {
            eventListener.accept(event.getDispatcher(), event.getBuildContext(), event.getCommandSelection());
        };
        NeoForge.EVENT_BUS.addListener(eventConsumer);
    }

    record PlacementEventImpl(RegisterSpawnPlacementsEvent event) implements RegHelper.SpawnPlacementEvent {
        @Override
        public <T extends Mob> void register(EntityType<T> entityType, SpawnPlacementType decoratorType,
                                             Heightmap.Types heightMapType, SpawnPlacements.SpawnPredicate<T> decoratorPredicate) {
            event.register(entityType, decoratorType, heightMapType, decoratorPredicate, RegisterSpawnPlacementsEvent.Operation.AND);
        }
    }

    public static void addSpawnPlacementsRegistration(Consumer<RegHelper.SpawnPlacementEvent> eventListener) {
        Moonlight.assertInitPhase();

        Consumer<RegisterSpawnPlacementsEvent> eventConsumer = event -> {
            RegHelper.SpawnPlacementEvent spawnPlacementEvent = new PlacementEventImpl(event);
            eventListener.accept(spawnPlacementEvent);
        };
        MoonlightForge.getCurrentBus().addListener(eventConsumer);
    }

    public static void registerSimpleRecipeCondition(ResourceLocation id, Predicate<String> predicate) {
        register(id, () -> OptionalRecipeCondition.createCodec(id, predicate), NeoForgeRegistries.Keys.CONDITION_CODECS);
    }

    public static void addItemsToTabsRegistration(Consumer<RegHelper.ItemToTabEvent> eventListener) {
        Moonlight.assertInitPhase();

        Consumer<BuildCreativeModeTabContentsEvent> eventConsumer = event -> {
            RegHelper.ItemToTabEvent itemToTabEvent = new ItemToTabEventImpl(event);
            eventListener.accept(itemToTabEvent);
        };
        MoonlightForge.getCurrentBus().addListener(EventPriority.LOW, eventConsumer);
    }

    private record ItemToTabEventImpl(BuildCreativeModeTabContentsEvent event) implements RegHelper.ItemToTabEvent {

        public void removeItems(ResourceKey<CreativeModeTab> tab, Predicate<ItemStack> target) {
            event.getParentEntries().removeIf(target);
            event.getSearchEntries().removeIf(target);
        }

        @Override
        public void addItems(ResourceKey<CreativeModeTab> tab, @Nullable Predicate<ItemStack> target, boolean after, List<ItemStack> items) {
            if (event.getTabKey() != tab) return;
            if (target != null) {
                if (after) {
                    ItemStack last = findLast(event, target);
                    if (!last.isEmpty()) {
                        for (int j = items.size(); j > 0; j--) {
                            event.insertAfter(last, items.get(j - 1), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
                        }
                        return;
                    } else {
                        Moonlight.crashIfInDev();
                    }
                } else {
                    ItemStack first = findFirst(event, target);
                    if (!first.isEmpty()) {
                        for (var s : items) {
                            event.insertBefore(first, s, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
                        }
                        return;
                    } else {
                        Moonlight.crashIfInDev();
                    }
                }
            }
            event.acceptAll(items);
        }

        private ItemStack findFirst(BuildCreativeModeTabContentsEvent event, Predicate<ItemStack> target) {
            for (var s : event.getParentEntries()) {
                if (target.test(s)) {
                    return s;
                }
            }
            return ItemStack.EMPTY;
        }

        private ItemStack findLast(BuildCreativeModeTabContentsEvent event, Predicate<ItemStack> target) {
            boolean foundOne = false;
            ItemStack previous = ItemStack.EMPTY;
            for (var s : event.getParentEntries()) {
                if (target.test(s)) {
                    foundOne = true;
                    previous = s;
                } else {
                    if (foundOne) return previous;
                }
            }
            return previous;
        }
    }


    public static void addLootTableInjects(Consumer<RegHelper.LootInjectEvent> eventListener) {
        Moonlight.assertInitPhase();

        Consumer<LootTableLoadEvent> eventConsumer = event ->
                eventListener.accept(new RegHelper.LootInjectEvent() {
                    @Override
                    public ResourceLocation getTable() {
                        return event.getName();
                    }

                    @Override
                    public void addTableReference(ResourceLocation targetId) {
                        LootPool pool = LootPool.lootPool().add(NestedLootTable.lootTableReference(
                                ResourceKey.create(Registries.LOOT_TABLE, targetId))).build();
                        event.getTable().addPool(pool);
                    }
                });
        NeoForge.EVENT_BUS.addListener(eventConsumer);
    }

    public static void registerFireworkRecipe(FireworkExplosion.Shape shape, Item ingredient) {
        FireworkStarRecipe.SHAPE_BY_ITEM = new HashMap<>(FireworkStarRecipe.SHAPE_BY_ITEM);
        FireworkStarRecipe.SHAPE_BY_ITEM.put(ingredient, shape);
        FireworkStarRecipe.SHAPE_INGREDIENT = CompoundIngredient.of(
                FireworkStarRecipe.SHAPE_INGREDIENT,
                Ingredient.of(ingredient));
    }

    public static void startRegisteringFor(Object bus) {
        if (bus instanceof IEventBus b) {
            MoonlightForge.startRegistering(b);
        } else {
            throw new IllegalArgumentException("Invalid bus type. Must be of IEventBus type: " + bus);
        }
    }
}
