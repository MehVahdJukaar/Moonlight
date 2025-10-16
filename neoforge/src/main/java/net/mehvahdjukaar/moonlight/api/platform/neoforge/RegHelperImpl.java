package net.mehvahdjukaar.moonlight.api.platform.neoforge;

import com.google.common.base.Preconditions;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import net.mehvahdjukaar.moonlight.api.fluids.ModFlowingFluid;
import net.mehvahdjukaar.moonlight.api.misc.IAttachmentType;
import net.mehvahdjukaar.moonlight.api.misc.RegSupplier;
import net.mehvahdjukaar.moonlight.api.misc.Registrator;
import net.mehvahdjukaar.moonlight.api.misc.TriFunction;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.api.resources.recipe.neoforge.OptionalRecipeCondition;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.misc.AttachmentBuilderImpl;
import net.mehvahdjukaar.moonlight.neoforge.MoonlightForge;
import net.minecraft.client.searchtree.SearchTree;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.village.poi.PoiType;
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
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.javafmlmod.FMLModContainer;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.crafting.CompoundIngredient;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.common.world.poi.ExtendPoiTypesEvent;
import net.neoforged.neoforge.event.*;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.registries.*;
import org.jetbrains.annotations.Nullable;

import java.util.*;
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
            doWithBus(modId, r::register);
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

    private static final List<Pair<String, Consumer<IEventBus>>> RUN_LATER = new ArrayList<>();

    private static void doWithBus(String modId, Consumer<IEventBus> consumer) {
        if (Moonlight.isInitPhase()) {
            consumer.accept(getModEventBus(modId));
        } else {
            RUN_LATER.add(Pair.of(modId, consumer));
        }
    }

    //mega shit. this so we can have statically initialized stuff that gets its bus subscriptions later
    public static void runTasksOnInit() {
        for (var e : RUN_LATER) {
            e.getSecond().accept(getModEventBus(e.getFirst()));
        }
        RUN_LATER.clear();
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

    public static void registerBlockFlammability(Block item, int igniteOdds, int burnOdds) {
        ((FireBlock) Blocks.FIRE).setFlammable(item, igniteOdds, burnOdds);
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

    public static <A> Registry<A> registerRegistry(ResourceKey<Registry<A>> key, boolean synced) {
        String modId = key.location().getNamespace();
        DeferredRegister<A> defer = DeferredRegister.create(key, modId);
        var reg = defer.makeRegistry(
                (b) -> b.sync(synced));
        doWithBus(modId, defer::register);

        return reg;
    }

    public static <T> void registerDataPackRegistry(ResourceKey<Registry<T>> registryKey, Codec<T> codec, @Nullable Codec<T> networkCodec) {
        Moonlight.assertInitPhase();

        Consumer<DataPackRegistryEvent.NewRegistry> eventConsumer = event -> {
            event.dataPackRegistry(registryKey, codec, networkCodec);
        };
        var bus = MoonlightForge.getCurrentBus();
        bus.addListener(eventConsumer);
    }

    public static void addItemsToTabsRegistration(Consumer<RegHelper.ItemToTabEvent> eventListener) {
        Moonlight.assertInitPhase();

        Consumer<BuildCreativeModeTabContentsEvent> eventConsumer = event -> {
            RegHelper.ItemToTabEvent itemToTabEvent = new ItemToTabEventImpl(event);
            eventListener.accept(itemToTabEvent);
        };
        MoonlightForge.getCurrentBus().addListener(EventPriority.LOWEST, eventConsumer);
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
                        Moonlight.logIfInDev("Failed to find target item before for items: " + items);
                    }
                } else {
                    ItemStack first = findFirst(event, target);
                    if (!first.isEmpty()) {
                        for (var s : items) {
                            event.insertBefore(first, s, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
                        }
                        return;
                    } else {
                        Moonlight.logIfInDev("Failed to find target item after for items: " + items);
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

    public static <T> Supplier<EntityDataSerializer<T>> registerEntityDataSerializer(ResourceLocation name, Supplier<EntityDataSerializer<T>> serializer) {
        return RegHelper.register(name, serializer, NeoForgeRegistries.Keys.ENTITY_DATA_SERIALIZERS);
    }

    @Deprecated(forRemoval = true)
    public static void addBlocksToPOI(ResourceKey<PoiType> poi, Iterable<? extends Block> blocks) {
        MoonlightForge.addPoi(poi, blocks);
    }

    public static void addExtraPOIStatesRegistration(Consumer<RegHelper.ExtraPOIStatesEvent> eventListener) {
        Moonlight.assertInitPhase();

        Consumer<ExtendPoiTypesEvent> eventConsumer = event -> {
            eventListener.accept(new RegHelper.ExtraPOIStatesEvent() {
                @Override
                public void addBlock(ResourceKey<PoiType> typeKey, Block block) {
                    event.addBlockToPoi(typeKey, block);
                }

                @Override
                public void addStates(ResourceKey<PoiType> typeKey, Set<BlockState> states) {
                    event.addStatesToPoi(typeKey, states);
                }
            });
        };
        MoonlightForge.getCurrentBus().addListener(eventConsumer);
    }

    public static void registerResourcePack(PackType packType, @Nullable Supplier<Pack> packSupplier) {
        Moonlight.assertInitPhase();

        if (packSupplier == null) return;
        var bus = MoonlightForge.getCurrentBus();
        Consumer<AddPackFindersEvent> consumer = event -> {
            if (event.getPackType() == packType) {
                var p = packSupplier.get();
                if (p != null) {
                    event.addRepositorySource(infoConsumer -> infoConsumer.accept(packSupplier.get()));
                }
            }
        };
        bus.addListener(consumer);
    }

    public static <A> IAttachmentType<A> regDataAttachment(ResourceLocation id, Supplier<RegHelper.AttachmentBuilder<A>> config) {
        var attachment = RegHelper.register(id,
                () -> makeDataAttachmentBuilder(config).build(),
                NeoForgeRegistries.Keys.ATTACHMENT_TYPES);
        return new AttachmentWrapper<>(attachment);
    }

    private static <A> AttachmentType.Builder<A> makeDataAttachmentBuilder(Supplier<RegHelper.AttachmentBuilder<A>> config) {
        var c = (AttachmentBuilderImpl<A>) config.get();
        var b = AttachmentType.builder(c.initializer);
        if (c.sync != null) {
            b.sync((iAttachmentHolder, player) -> c.sync.getSecond()
                    .test(iAttachmentHolder, player), c.sync.getFirst());
        }
        if (c.persistentCodec != null) {
            b.serialize(c.persistentCodec);
        }
        if (c.copyOnDeath) {
            b.copyOnDeath();
        }
        return b;
    }

    public static void addExtraBEBlockStatesRegistration(Consumer<RegHelper.ExtraBEStatesEvent> eventListener) {
        Moonlight.assertInitPhase();
        Consumer<BlockEntityTypeAddBlocksEvent> eventConsumer = event -> {
            eventListener.accept(event::modify);
        };
        MoonlightForge.getCurrentBus().addListener(eventConsumer);
    }

    private record AttachmentWrapper<A>(Supplier<AttachmentType<A>> typeSupplier) implements IAttachmentType<A> {

        @Override
        public A getOrCreate(Object attachmentHolder) {
            if (attachmentHolder instanceof IAttachmentHolder h) {
                return h.getData(typeSupplier);
            }
            throw new IllegalArgumentException("Object " + attachmentHolder + " is not an attachment holder");
        }

        @Override
        public A getOrNull(Object attachmentHolder) {
            if (attachmentHolder instanceof IAttachmentHolder h) {
                return h.getExistingDataOrNull(typeSupplier);
            }
            return null;
        }
    }

}
