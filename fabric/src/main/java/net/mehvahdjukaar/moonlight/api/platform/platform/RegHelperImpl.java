package net.mehvahdjukaar.moonlight.api.platform.platform;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityDataRegistry;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.mehvahdjukaar.candlelight.api.PlatformImpl;
import net.mehvahdjukaar.moonlight.api.client.platform.IFabricMenuType;
import net.mehvahdjukaar.moonlight.api.misc.IAttachmentType;
import net.mehvahdjukaar.moonlight.api.misc.RegSupplier;
import net.mehvahdjukaar.moonlight.api.misc.Registrator;
import net.mehvahdjukaar.moonlight.api.misc.TriFunction;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.api.resources.recipe.platform.OptionalRecipeCondition;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.misc.AttachmentBuilderImpl;
import net.mehvahdjukaar.moonlight.core.mixins.platform.PackRepositoryAccessor;
import net.mehvahdjukaar.moonlight.core.mixins.platform.PoiTypeAccessor;
import net.mehvahdjukaar.moonlight.core.set.platform.BlockSetInternalImpl;
import net.mehvahdjukaar.moonlight.platform.MoonlightFabric;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class RegHelperImpl {

    public static final Map<ResourceKey<? extends Registry<?>>, Map<String, RegistryQueue<?>>> REGISTRIES = new LinkedHashMap<>();

    public static final List<ResourceKey<? extends Registry<?>>> REG_PRIORITY;

    //order is important here
    static {
        List<ResourceKey<? extends Registry<?>>> list = new ArrayList<>();
        //corrected forge registry order. same that forge does
        list.add(Registries.ATTRIBUTE);
        list.add(Registries.DATA_COMPONENT_TYPE);
        list.addAll(BuiltInRegistries.LOADERS.keySet().stream()
                .map(ResourceKey::createRegistryKey).toList());
        REG_PRIORITY = list;
        REG_PRIORITY.forEach(e -> REGISTRIES.put(e, new LinkedHashMap<>()));
    }

    //call from mod setup
    @ApiStatus.Internal
    public static void lateRegisterEntries() {
        for (var entry : REGISTRIES.entrySet()) {
            var map = entry.getValue();
            //freaking fabric just runs mod initializers in random order. hate this. we run in deterministic manner here
            var sorted = map.keySet().stream().sorted().toList();
            for (var s : sorted) {
                try {
                    map.get(s).initializeEntries();
                } catch (Exception e) {
                    throw new RuntimeException("Failed to initialize registry objects for namespace [" + s + "]", e);
                }
            }
            if (entry.getKey() == Registries.ITEM) {
                //dynamic block registration after all blocks
                BlockSetInternalImpl.initializeBlockSets();
            }
        }
        BlockSetInternalImpl.registerDynamicOrdered(REGISTRIES.keySet().stream().toList());

        REGISTRIES.clear();
    }

    static class SpawnPlacementsImpl implements RegHelper.SpawnPlacementEvent {

        @Override
        public <T extends Mob> void register(EntityType<T> entityType, SpawnPlacementType decoratorType, Heightmap.Types heightMapType, SpawnPlacements.SpawnPredicate<T> decoratorPredicate) {
            try {
                SpawnPlacements.register(entityType, decoratorType, heightMapType, decoratorPredicate);
            } catch (Exception e) {
                Moonlight.LOGGER.warn("Skipping placement registration for {} as its not of Mob type", entityType);
            }
        }
    }

    public static void finishRegistration(String modId) {
        for (var entry : REGISTRIES.entrySet()) {
            var map = entry.getValue();
            var v = map.get(modId);
            if (v != null) {
                v.initializeEntries();
                map.remove(modId);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static <T, E extends T> RegSupplier<E> register(
            Identifier name, Supplier<E> supplier, ResourceKey<? extends Registry<T>> reg) {
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
                    Moonlight.addDependent(modId);

                    return new RegistryQueue<>(reg);
                });
        if (reg.equals(Registries.POINT_OF_INTEREST_TYPE)) {
            PlatHelper.addCommonSetup(() -> {
                var holder = BuiltInRegistries.POINT_OF_INTEREST_TYPE
                        .getOrThrow(ResourceKey.create(Registries.POINT_OF_INTEREST_TYPE, name));
                PoiTypes.registerBlockStates(holder, holder.value().matchingStates());
            });
        }
        return (RegSupplier<E>) registry.add((Supplier<T>) supplier::get, name);
    }

    public static <T, E extends T> RegSupplier<E> registerAsync(Identifier name, Supplier<E> supplier, ResourceKey<?  extends Registry<T>> reg) {
        RegistryQueue.RegEntryHolder<T> entry = new RegistryQueue.RegEntryHolder<>(name,
                supplier::get, (ResourceKey) reg);
        entry.initialize(true);
        return (RegSupplier<E>) entry;
    }

    public static <T> void registerInBatch(Registry<T> reg, Consumer<Registrator<T>> eventListener) {
        var m = REGISTRIES.computeIfAbsent(reg.key(), h -> new LinkedHashMap<>());
        RegistryQueue<T> registry = (RegistryQueue<T>) m.computeIfAbsent("a", c -> new RegistryQueue<>(reg.key()));
        registry.add(eventListener);
    }


    public static <T extends Fluid> RegSupplier<T> registerFluid(Identifier name, Supplier<T> fluid) {
        return register(name, fluid, Registries.FLUID);
    }

    public static <C extends AbstractContainerMenu> RegSupplier<MenuType<C>> registerMenuType(
            Identifier name,
            TriFunction<Integer, Inventory, FriendlyByteBuf, C> containerFactory) {
        return register(name, () -> IFabricMenuType.create(containerFactory::apply), Registries.MENU);
    }

    public static <C extends AbstractContainerMenu> RegSupplier<MenuType<C>> registerSimpleMenuType(
            Identifier name,
            MenuType.MenuSupplier<C> containerFactory) {
        return register(name, () -> IFabricMenuType.createSimple(containerFactory), Registries.MENU);
    }

    public static void registerBlockFlammability(Block item, int igniteOddsSpread, int burnOdds) {
        FlammableBlockRegistry.getDefaultInstance().add(item, burnOdds, igniteOddsSpread);
    }

    public static void addAttributeRegistration(Consumer<RegHelper.AttributeEvent> eventListener) {
        Moonlight.assertInitPhase();

        MoonlightFabric.PRE_SETUP_WORK.add(() -> eventListener.accept(FabricDefaultAttributeRegistry::register));
    }

    public static void addSpawnPlacementsRegistration(Consumer<RegHelper.SpawnPlacementEvent> eventListener) {
        Moonlight.assertInitPhase();

        MoonlightFabric.PRE_SETUP_WORK.add(() -> eventListener.accept(new SpawnPlacementsImpl()));
    }

    public static void addCommandRegistration(RegHelper.CommandRegistration eventListener) {
        Moonlight.assertInitPhase();

        CommandRegistrationCallback.EVENT.register(eventListener::accept);
    }

    public static void registerSimpleRecipeCondition(Identifier id, Predicate<String> predicate) {
        Moonlight.assertInitPhase();
        var codec = OptionalRecipeCondition.createCodec(id, predicate);
        ResourceConditionType<OptionalRecipeCondition> type = ResourceConditionType.create(id, codec);
        ResourceConditions.register(type);
    }

    public static <T> void registerDataPackRegistry(ResourceKey<Registry<T>> registryKey, Codec<T> codec, @Nullable Codec<T> networkCodec) {
        if (networkCodec != null) {
            DynamicRegistries.registerSynced(registryKey,
                    codec,
                    networkCodec,
                    DynamicRegistries.SyncOption.SKIP_WHEN_EMPTY
            );
        } else {
            DynamicRegistries.register(registryKey, codec);
        }

    }

    public static void addItemsToTabsRegistration(Consumer<RegHelper.ItemToTabEvent> eventListener) {
        Moonlight.assertInitPhase();
        CreativeModeTabEvents.MODIFY_OUTPUT_ALL.register((creativeModeTab, output) -> {
            ResourceKey<CreativeModeTab> tabKey = BuiltInRegistries.CREATIVE_MODE_TAB.getResourceKey(creativeModeTab).get();
            eventListener.accept(new RegHelper.ItemToTabEvent() {
                @Override
                public CreativeModeTab.ItemDisplayParameters getParameters() {
                    return output.getContext();
                }

                @Override
                public CreativeModeTab getTab() {
                    return creativeModeTab;
                }

                @Override
                public void remove(ResourceKey<CreativeModeTab> tab, Predicate<ItemStack> condition) {
                    if (tab != tabKey) return;
                    output.getDisplayStacks().removeIf(condition);
                    output.getSearchTabStacks().removeIf(condition);
                }

                @Override
                public void addItems(ResourceKey<CreativeModeTab> tab, @Nullable Predicate<ItemStack> target, boolean after, List<ItemStack> items) {
                    if (tab != tabKey) return;
                    if (target == null) {
                        output.acceptAll(items);
                    } else {
                        if (after) {
                            output.insertAfter(target, items, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
                        } else {
                            output.insertBefore(target, items, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
                        }
                    }
                }
            });
        });
    }

    public static RegSupplier<CreativeModeTab> registerCreativeModeTab(Identifier name,
                                                                       boolean search,
                                                                       List<Identifier> beforeEntries,
                                                                       List<Identifier> afterEntries,
                                                                       Consumer<CreativeModeTab.Builder> configurator) {
        return register(name, () -> {
            var builder = FabricCreativeModeTab.builder();
            configurator.accept(builder);
            return builder.build();
        }, Registries.CREATIVE_MODE_TAB);
    }

    public static void addLootTableInjects(Consumer<RegHelper.LootInjectEvent> eventListener) {
        Moonlight.assertInitPhase();

        LootTableEvents.MODIFY.register(
                (key, tableBuilder, source, registries) -> {
                    eventListener.accept(new RegHelper.LootInjectEvent() {
                        @Override
                        public Identifier getTable() {
                            return key.identifier();
                        }

                        @Override
                        public void addTableReference(Identifier targetId) {
                            LootPool pool = LootPool.lootPool().add(NestedLootTable.lootTableReference(
                                    ResourceKey.create(Registries.LOOT_TABLE, targetId))).build();
                            tableBuilder.pool(pool);
                        }
                    });
                }
        );
    }

    public static void startRegisteringFor(Object bus) {

    }

    public static <T> Supplier<EntityDataSerializer<T>> registerEntityDataSerializer(Identifier name, Supplier<EntityDataSerializer<T>> serializer) {
        var value = serializer.get();
        FabricEntityDataRegistry.register(name, value);
        return () -> value;
    }

    public static <A> Registry<A> registerRegistry(ResourceKey<Registry<A>> key, boolean sync) {
        var b = FabricRegistryBuilder.create(key)
                .attribute(RegistryAttribute.MODDED);
        if (sync) {
            b = b.attribute(RegistryAttribute.SYNCED);
        }
        return b.buildAndRegister();
    }

    public static void addExtraPOIStatesRegistration(Consumer<RegHelper.ExtraPOIStatesEvent> eventListener) {
        PlatHelper.addCommonSetup(() -> {
            eventListener.accept(new RegHelper.ExtraPOIStatesEvent() {
                @Override
                public void addBlock(ResourceKey<PoiType> poi, Block block) {
                    var beehivePOI = BuiltInRegistries.POINT_OF_INTEREST_TYPE.getOrThrow(poi);
                    //add vanilla states if they are mutable
                    Set<BlockState> matchingStates = new HashSet<>(beehivePOI.value().matchingStates());
                    matchingStates.addAll(block.getStateDefinition().getPossibleStates());
                    Set<BlockState> newStates = new HashSet<>(block.getStateDefinition().getPossibleStates());
                    ((PoiTypeAccessor) (Object) beehivePOI.value())
                            .setMatchingStates(matchingStates);

                    PoiTypes.registerBlockStates(beehivePOI, newStates);
                }

                @Override
                public void addStates(ResourceKey<PoiType> poi, Set<BlockState> states) {
                    var beehivePOI = BuiltInRegistries.POINT_OF_INTEREST_TYPE.getOrThrow(poi);
                    //add vanilla states if they are mutable
                    Set<BlockState> matchingStates = new HashSet<>(beehivePOI.value().matchingStates());
                    matchingStates.addAll(states);
                    Set<BlockState> newStates = new HashSet<>(states);
                    ((PoiTypeAccessor) (Object) beehivePOI.value())
                            .setMatchingStates(matchingStates);
                    PoiTypes.registerBlockStates(beehivePOI, newStates);
                }
            });
        });
    }

    public static void registerResourcePackSource(PackType packType, RepositorySource packSource) {
        Moonlight.assertInitPhase();

        //client is already loaded. add immediately, saving hassle
        if (packType == PackType.CLIENT_RESOURCES && PlatHelper.getPhysicalSide().isClient()) {
            if (Minecraft.getInstance().getResourcePackRepository() instanceof PackRepositoryAccessor rep) {
                var newSources = new HashSet<>(rep.getSources());
                newSources.add(packSource);
                rep.setSources(newSources);
            }
        }
        if (packType == PackType.SERVER_DATA) {
            MoonlightFabric.EXTRA_DATA_PACK_SOURCES.add(packSource);
        }
    }


    public static <A, T> IAttachmentType<A, T> registerDataAttachment(
            Identifier id, Supplier<RegHelper.AttachmentBuilder<A>> config, Class<T> targetClass) {
        if (!AttachmentTarget.class.isAssignableFrom(targetClass)) {
            Moonlight.LOGGER.warn("Registering data attachment for invalid class {} that does not implements AttachmentTarget. ", targetClass.getName());
        }
        var obj = makeDataAttachmentBuilder(config).buildAndRegister(id);
        return new AttachmentWrapper<>(obj);
    }

    private static <A> AttachmentRegistry.Builder<A> makeDataAttachmentBuilder(Supplier<RegHelper.AttachmentBuilder<A>> config) {
        var c = (AttachmentBuilderImpl<A>) config.get();
        var b = AttachmentRegistry.<A>builder()
                .initializer(c.initializer);
        if (c.sync != null) {
            b.syncWith(c.sync.getFirst(), (iAttachmentHolder, player) -> c.sync.getSecond()
                    .test(iAttachmentHolder, player));
        }
        if (c.persistentCodec != null) {
            b.persistent(c.persistentCodec);
        }
        if (c.copyOnDeath) {
            b.copyOnDeath();
        }
        return b;
    }

    public static void addExtraBEBlockStatesRegistration(Consumer<RegHelper.ExtraBEStatesEvent> eventListener) {
        //validBlocks may be immutable, swap the set via AW
        PlatHelper.addCommonSetup(() -> eventListener.accept((typeKey, block) -> {
            Set<Block> newBlocks = new HashSet<>(typeKey.validBlocks);
            newBlocks.addAll(Arrays.asList(block));
            typeKey.validBlocks = newBlocks;
        }));
    }

    private record AttachmentWrapper<A, T>(AttachmentType<A> type) implements IAttachmentType<A, T> {

        @Override
        public A getOrCreate(T obj) {
            if (obj instanceof AttachmentTarget h) {
                return h.getAttachedOrCreate(type);
            }
            throw new IllegalArgumentException("Object " + obj + " is not an attachment holder");
        }

        @Nullable
        @Override
        public A getOrNull(T obj) {
            if (obj instanceof AttachmentTarget h) {
                return h.getAttached(type);
            }
            return null;
        }

        @Override
        public void set(T attachmentHolder, @Nullable A data) {
            if (attachmentHolder instanceof AttachmentTarget h) {
                if (data == null) {
                    h.removeAttached(type);
                } else h.setAttached(type, data);
            } else {
                throw new IllegalArgumentException("Object " + attachmentHolder + " is not an attachment holder");
            }
        }

        @Override
        public void sync(T attachmentHolder) {
            if (attachmentHolder instanceof AttachmentTarget h) {
                h.modifyAttached(type, (a) -> a);
            }
        }
    }
}
