package net.mehvahdjukaar.moonlight.api.platform.platform;

import com.google.common.base.Preconditions;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import net.mehvahdjukaar.moonlight.api.misc.IAttachmentType;
import net.mehvahdjukaar.moonlight.api.misc.RegSupplier;
import net.mehvahdjukaar.moonlight.api.misc.Registrator;
import net.mehvahdjukaar.moonlight.api.misc.TriFunction;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.api.resources.recipe.platform.OptionalRecipeCondition;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.misc.AttachmentBuilderImpl;
import net.mehvahdjukaar.moonlight.platform.MoonlightForge;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
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
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;


public class RegHelperImpl {

    public record Wrapper<T>(DeferredHolder<T, ? extends T> registryObject) implements RegSupplier<T> {

        @Override
        public T get() {
            return registryObject.get();
        }

        @Override
        public Identifier getId() {
            return registryObject.getId();
        }

        @Override
        public @Nullable ResourceKey<T> getKey() {
            return registryObject.getKey();
        }

        @Override
        public T value() {
            return registryObject.get();
        }

        @Override
        public boolean isBound() {
            return registryObject.isBound();
        }

        @Override
        public boolean is(Identifier location) {
            return registryObject.is(location);
        }

        @Override
        public boolean is(ResourceKey<T> resourceKey) {
            return registryObject.is(resourceKey);
        }

        @Override
        public boolean is(Predicate<ResourceKey<T>> predicate) {
            return registryObject.is(predicate);
        }

        @Override
        public boolean is(TagKey<T> tagKey) {
            return registryObject.is(tagKey);
        }

        @Override
        public boolean is(Holder<T> holder) {
            return registryObject.is(holder);
        }

        @Override
        public Stream<TagKey<T>> tags() {
            return registryObject.tags();
        }

        @Override
        public Either<ResourceKey<T>, T> unwrap() {
            return registryObject.unwrap();
        }

        @Override
        public Optional<ResourceKey<T>> unwrapKey() {
            return registryObject.unwrapKey();
        }

        @Override
        public Kind kind() {
            return registryObject.kind();
        }

        @Override
        public boolean canSerializeIn(HolderOwner<T> owner) {
            return registryObject.canSerializeIn(owner);
        }

        @Override
        public boolean areComponentsBound() {
            return registryObject.areComponentsBound();
        }

        @Override
        public DataComponentMap components() {
            return registryObject.components();
        }



        @SuppressWarnings("all")
        @Override
        public boolean equals(Object obj) {
            return registryObject.equals(obj);
        }

        @Override
        public int hashCode() {
            return registryObject.hashCode();
        }

        @Override
        public Holder<T> getDelegate() {
            return registryObject.getDelegate();
        }

        @Override
        public String toString() {
            return registryObject.toString();
        }

        @Override
        public <T1> @Nullable T1 getData(DataMapType<T, T1> type) {
            return registryObject.getData(type);
        }
    }

    //this might be accessed on multiple threads
    private static final Map<ResourceKey<? extends Registry<?>>, Map<String, DeferredRegister<?>>> REGISTRIES = new ConcurrentHashMap<>();

    public static <T, E extends T> RegSupplier<E> register(
            Identifier name, Supplier<E> supplier, Registry<T> reg) {
        return register(name, supplier, reg.key());
    }

    @SuppressWarnings("unchecked")
    public static <T, E extends T> RegSupplier<E> register(
            Identifier name, Supplier<E> supplier, ResourceKey<? extends Registry<T>> regKey) {
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
        DeferredHolder<T, E> register = registry.register(name.getPath(), supplier);
        return (RegSupplier<E>) new Wrapper<>(register);
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

    public static <T, E extends T> RegSupplier<E> registerAsync(Identifier name, Supplier<E> supplier, ResourceKey<? extends Registry<T>> reg) {
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
            Identifier name,
            TriFunction<Integer, Inventory, FriendlyByteBuf, C> containerFactory) {
        return register(name, () -> IMenuTypeExtension.create(containerFactory::apply), Registries.MENU);
    }

    public static <C extends AbstractContainerMenu> RegSupplier<MenuType<C>> registerSimpleMenuType(
            Identifier name,
            MenuType.MenuSupplier<C> containerFactory) {
        return register(name, () -> new MenuType<>(containerFactory, FeatureFlags.DEFAULT_FLAGS), Registries.MENU);
    }

    public static <T extends Fluid> RegSupplier<T> registerFluid(Identifier name, Supplier<T> fluid) {
        var f = register(name, fluid, Registries.FLUID);
        //register fluid type
        //register(name, () -> f.get().getFluidType(), NeoForgeRegistries.FLUID_TYPES);
        return f;
    }

    public static RegSupplier<CreativeModeTab> registerCreativeModeTab(Identifier name,
                                                                       boolean hasSearchBar,
                                                                       List<Identifier> afterEntries,
                                                                       List<Identifier> beforeEntries,
                                                                       Consumer<CreativeModeTab.Builder> configurator) {
        return register(name, () -> {
            var b = CreativeModeTab.builder();
            configurator.accept(b);
            if (!beforeEntries.isEmpty()) {
                b.withTabsBefore(beforeEntries.toArray(Identifier[]::new));
            }
            if (!afterEntries.isEmpty()) {
                b.withTabsBefore(afterEntries.toArray(Identifier[]::new));
            }
            if (hasSearchBar) b.withSearchBar();
            return b.build();
        }, Registries.CREATIVE_MODE_TAB);
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

    public static void registerSimpleRecipeCondition(Identifier id, Predicate<String> predicate) {
        register(id, () -> OptionalRecipeCondition.createCodec(id, predicate), NeoForgeRegistries.Keys.CONDITION_CODECS);
    }

    public static <A> Registry<A> registerRegistry(ResourceKey<Registry<A>> key, boolean synced) {
        String modId = key.identifier().getNamespace();
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

        @Override
        public CreativeModeTab.ItemDisplayParameters getParameters() {
            return event.getParameters();
        }

        public CreativeModeTab getTab() {
            return event.getTab();
        }

        public void remove(ResourceKey<CreativeModeTab> tab, Predicate<ItemStack> target) {
            if (event.getTabKey() != tab) return;
            //TODO: add when neoforge updates
            //event.remove(target, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        }

        @Override
        public void addItems(ResourceKey<CreativeModeTab> tab, @Nullable Predicate<ItemStack> target, boolean after, List<ItemStack> items) {
            if (event.getTabKey() != tab) return;
            if (target != null) {
                if (after) {
                    ItemStack last = findLast(event, target);
                    if (!last.isEmpty()) {
                        CreativeModeTab.TabVisibility vis = getTabVisibility(last);
                        for (int j = items.size(); j > 0; j--) {
                            event.insertAfter(last, items.get(j - 1), vis);
                        }
                        return;
                    } else {
                        Moonlight.logIfInDev("Failed to find target item before for items: " + items);
                    }
                } else {
                    ItemStack first = findFirst(event, target);
                    if (!first.isEmpty()) {
                        CreativeModeTab.TabVisibility vis = getTabVisibility(first);
                        for (var s : items) {
                            event.insertBefore(first, s, vis);
                        }
                        return;
                    } else {
                        Moonlight.logIfInDev("Failed to find target item after for items: " + items);
                    }
                }
            }
            event.acceptAll(items);
        }

        private CreativeModeTab.@NotNull TabVisibility getTabVisibility(ItemStack first) {
            CreativeModeTab.TabVisibility vis;
            if (event.getSearchEntries().contains(first)) {
                vis = CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS;
            } else {
                vis = CreativeModeTab.TabVisibility.PARENT_TAB_ONLY;
                Moonlight.LOGGER.warn("Found an item that was in parent tab but not in search tab. This might be a bug? {}", first);
            }
            return vis;
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
                    public Identifier getTable() {
                        return event.getName();
                    }

                    @Override
                    public void addTableReference(Identifier targetId) {
                        LootPool pool = LootPool.lootPool().add(NestedLootTable.lootTableReference(
                                ResourceKey.create(Registries.LOOT_TABLE, targetId))).build();
                        event.getTable().addPool(pool);
                    }
                });
        NeoForge.EVENT_BUS.addListener(eventConsumer);
    }

    public static <T> Supplier<EntityDataSerializer<T>> registerEntityDataSerializer(Identifier name, Supplier<EntityDataSerializer<T>> serializer) {
        return RegHelper.register(name, serializer, NeoForgeRegistries.Keys.ENTITY_DATA_SERIALIZERS);
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

    public static void registerResourcePackSource(PackType packType, RepositorySource packSource) {
        Moonlight.assertInitPhase();
        IEventBus bus = MoonlightForge.getCurrentBus();
        Consumer<AddPackFindersEvent> consumer = event -> {
            if (event.getPackType() == packType) {
                event.addRepositorySource(packSource);
            }
        };
        bus.addListener(consumer);
    }

    public static <A, T> IAttachmentType<A, T> registerDataAttachment(
            Identifier id, Supplier<RegHelper.AttachmentBuilder<A>> config, Class<T> targetClass) {
        if (!IAttachmentHolder.class.isAssignableFrom(targetClass)) {
            Moonlight.LOGGER.warn("Registering data attachment for invalid class {} that does not implements IAttachmentHolder. ", targetClass.getName());
        }
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
            //neoforge wants a MapCodec, our API takes arbitrary codecs
            b.serialize(c.persistentCodec.fieldOf("value"));
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

    private record AttachmentWrapper<A, T>(Supplier<AttachmentType<A>> typeSupplier) implements IAttachmentType<A, T> {

        @Override
        public A getOrCreate(T attachmentHolder) {
            if (attachmentHolder instanceof IAttachmentHolder h) {
                return h.getData(typeSupplier);
            }
            throw new IllegalArgumentException("Object " + attachmentHolder + " is not an attachment holder");
        }

        @Override
        public A getOrNull(T attachmentHolder) {
            if (attachmentHolder instanceof IAttachmentHolder h) {
                return h.getExistingDataOrNull(typeSupplier);
            }
            return null;
        }

        public void set(T attachmentHolder, @Nullable A data) {
            if (attachmentHolder instanceof IAttachmentHolder h) {
                if (data == null) {
                    h.removeData(typeSupplier);
                } else h.setData(typeSupplier, data);
            } else {
                throw new IllegalArgumentException("Object " + attachmentHolder + " is not an attachment holder");
            }
        }

        public void sync(T attachmentHolder) {
            if (attachmentHolder instanceof IAttachmentHolder h) {
                h.syncData(typeSupplier);
            } else {
                throw new IllegalArgumentException("Object " + attachmentHolder + " is not an attachment holder");
            }
        }
    }

}
