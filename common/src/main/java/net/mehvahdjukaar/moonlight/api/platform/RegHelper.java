package net.mehvahdjukaar.moonlight.api.platform;

import com.google.common.collect.ImmutableSet;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.moonlight.api.block.ModStairBlock;
import net.mehvahdjukaar.moonlight.api.misc.RegSupplier;
import net.mehvahdjukaar.moonlight.api.misc.Registrator;
import net.mehvahdjukaar.moonlight.api.misc.TriFunction;
import net.mehvahdjukaar.moonlight.api.trades.ItemListingManager;
import net.mehvahdjukaar.moonlight.api.trades.ModItemListing;
import net.mehvahdjukaar.moonlight.api.util.DispenserHelper;
import net.minecraft.Util;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.entity.schedule.Schedule;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.*;

/**
 * Helper class dedicated to platform independent registration methods
 */
public class RegHelper {

    @ExpectPlatform
    public static <T, E extends T> RegSupplier<E> register(
            ResourceLocation name, Supplier<E> supplier, ResourceKey<? extends Registry<T>> regKey) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static <T> void registerInBatch(Registry<T> reg, Consumer<Registrator<T>> eventListener) {
        throw new AssertionError();
    }

    /**
     * Registers stuff immediately on fabric. Normal behavior for forge
     */
    @ExpectPlatform
    public static <T, E extends T> RegSupplier<E> registerAsync(ResourceLocation name, Supplier<E> supplier, ResourceKey<? extends Registry<T>> regKey) {
        throw new AssertionError();
    }

    public static <T extends Block> RegSupplier<T> registerBlock(ResourceLocation name, Supplier<T> block) {
        return register(name, block, Registries.BLOCK);
    }

    //helpers
    public static <T extends Block> RegSupplier<T> registerBlockWithItem(ResourceLocation name, Supplier<T> blockFactory) {
        return registerBlockWithItem(name, blockFactory, new Item.Properties());
    }

    public static <T extends Block> RegSupplier<T> registerBlockWithItem(ResourceLocation name, Supplier<T> blockFactory, Item.Properties properties) {
        RegSupplier<T> block = registerBlock(name, blockFactory);
        registerItem(name, () -> new BlockItem(block.get(), properties));
        return block;
    }

    public static <T extends SimpleCriterionTrigger<?>> RegSupplier<T> registerTriggerType(ResourceLocation name, Supplier<T> instance) {
        return register(name, instance, Registries.TRIGGER_TYPE);
    }

    public static <T extends PlacementModifierType<?>> RegSupplier<T> registerPlacementModifier(ResourceLocation name, Supplier<T> instance) {
        return register(name, instance, Registries.PLACEMENT_MODIFIER_TYPE);
    }

    public static <T extends LootPoolEntryContainer> RegSupplier<LootPoolEntryType> registerLootPoolEntry(ResourceLocation name,
                                                                                                          Supplier<MapCodec<T>> instance) {
        return register(name, () -> new LootPoolEntryType(instance.get()), Registries.LOOT_POOL_ENTRY_TYPE);
    }

    public static <T extends LootItemCondition> RegSupplier<LootItemConditionType> registerLootCondition(ResourceLocation name,
                                                                                                         Supplier<MapCodec<T>> instance) {
        return register(name, () -> new LootItemConditionType(instance.get()), Registries.LOOT_CONDITION_TYPE);
    }

    public static <T> Supplier<DataComponentType<T>> registerDataComponent(ResourceLocation name,
                                                                           Supplier<DataComponentType<T>> component) {
        return register(name, component, Registries.DATA_COMPONENT_TYPE);
    }

    public static RegSupplier<PoiType> registerPOI(ResourceLocation name, Supplier<PoiType> poi) {
        return register(name, poi, Registries.POINT_OF_INTEREST_TYPE);
    }

    public static RegSupplier<PoiType> registerPOI(ResourceLocation name, int searchDistance, int maxTickets, Block... blocks) {
        return registerPOI(name, () -> {
            ImmutableSet.Builder<BlockState> builder = ImmutableSet.builder();
            for (Block block : blocks) {
                builder.addAll(block.getStateDefinition().getPossibleStates());
            }
            return new PoiType(builder.build(), searchDistance, maxTickets);
        });
    }

    public static RegSupplier<PoiType> registerPOI(ResourceLocation name, int searchDistance, int maxTickets, Supplier<Block>... blocks) {
        return registerPOI(name, () -> {
            ImmutableSet.Builder<BlockState> builder = ImmutableSet.builder();
            for (var block : blocks) {
                builder.addAll(block.get().getStateDefinition().getPossibleStates());
            }
            return new PoiType(builder.build(), searchDistance, maxTickets);
        });
    }

    @ExpectPlatform
    public static <T extends Fluid> RegSupplier<T> registerFluid(ResourceLocation name, Supplier<T> fluid) {
        throw new AssertionError();
    }

    public static <T extends Item> RegSupplier<T> registerItem(ResourceLocation name, Supplier<T> item) {
        return register(name, item, Registries.ITEM);
    }

    public static <T extends Feature<?>> RegSupplier<T> registerFeature(ResourceLocation name, Supplier<T> feature) {
        return register(name, feature, Registries.FEATURE);
    }

    public static <T extends StructureType<?>> RegSupplier<T> registerStructure(ResourceLocation name, Supplier<T> feature) {
        //TODO: this causes issues on fabric and its very random as might be on only with some random unrelated mods. best to lave it like this
        // return register(name, feature, Registry.STRUCTURE_TYPES);
        return registerAsync(name, feature, Registries.STRUCTURE_TYPE);
    }


    public static <T extends SoundEvent> RegSupplier<T> registerSound(ResourceLocation name, Supplier<T> sound) {
        return register(name, sound, Registries.SOUND_EVENT);
    }

    public static RegSupplier<SoundEvent> registerSound(ResourceLocation name) {
        return registerSound(name, () -> SoundEvent.createVariableRangeEvent(name));
    }

    public static RegSupplier<SoundEvent> registerSound(ResourceLocation name, float fixedRange) {
        return registerSound(name, () -> SoundEvent.createFixedRangeEvent(name, fixedRange));
    }

    @ExpectPlatform
    public static <C extends AbstractContainerMenu> RegSupplier<MenuType<C>> registerMenuType(
            ResourceLocation name,
            TriFunction<Integer, Inventory, FriendlyByteBuf, C> containerFactory) {
        throw new AssertionError();
    }

    public static <T extends MobEffect> RegSupplier<T> registerEffect(ResourceLocation name, Supplier<T> effect) {
        return register(name, effect, Registries.MOB_EFFECT);
    }

    public static <T extends Enchantment> RegSupplier<T> registerEnchantment(ResourceLocation name, Supplier<T> enchantment) {
        return register(name, enchantment, Registries.ENCHANTMENT);
    }

    public static <T extends SensorType<? extends Sensor<?>>> RegSupplier<T> registerSensor(ResourceLocation name, Supplier<T> sensorType) {
        return register(name, sensorType, Registries.SENSOR_TYPE);
    }

    public static <T extends Sensor<?>> RegSupplier<SensorType<T>> registerSensorI(ResourceLocation name, Supplier<T> sensor) {
        return register(name, () -> new SensorType<>(sensor), Registries.SENSOR_TYPE);
    }

    public static <T extends Activity> RegSupplier<T> registerActivity(ResourceLocation name, Supplier<T> activity) {
        return register(name, activity, Registries.ACTIVITY);
    }

    public static RegSupplier<Activity> registerActivity(ResourceLocation name) {
        return registerActivity(name, () -> new Activity(name.getPath()));
    }

    public static <T extends Schedule> RegSupplier<T> registerSchedule(ResourceLocation name, Supplier<T> schedule) {
        return register(name, schedule, Registries.SCHEDULE);
    }

    public static <T extends MemoryModuleType<?>> RegSupplier<T> registerMemoryModule(ResourceLocation name, Supplier<T> memory) {
        return register(name, memory, Registries.MEMORY_MODULE_TYPE);
    }

    public static <U> RegSupplier<MemoryModuleType<U>> registerMemoryModule(ResourceLocation name, @Nullable Codec<U> codec) {
        return register(name, () -> new MemoryModuleType<>(Optional.ofNullable(codec)), Registries.MEMORY_MODULE_TYPE);
    }

    public static <T extends RecipeSerializer<?>> RegSupplier<T> registerRecipeSerializer(ResourceLocation name, Supplier<T> recipe) {
        return register(name, recipe, Registries.RECIPE_SERIALIZER);
    }

    @ExpectPlatform
    public static <T extends CraftingRecipe> RegSupplier<RecipeSerializer<T>> registerSpecialRecipe(ResourceLocation name, SimpleCraftingRecipeSerializer.Factory<T> factory) {
        throw new AssertionError();
    }

    public static <T extends Recipe<?>> Supplier<RecipeType<T>> registerRecipeType(ResourceLocation name) {
        return RegHelper.register(name, () -> {
            String id = name.toString();
            return new RecipeType<T>() {
                @Override
                public String toString() {
                    return id;
                }
            };
        }, Registries.RECIPE_TYPE);
    }

    public static <T extends BlockEntityType<E>, E extends BlockEntity> RegSupplier<T> registerBlockEntityType(ResourceLocation name,
                                                                                                               Supplier<T> blockEntity) {
        return register(name, blockEntity, Registries.BLOCK_ENTITY_TYPE);
    }

    public static <E extends BlockEntity> RegSupplier<BlockEntityType<E>> registerBlockEntityType(
            ResourceLocation name, BiFunction<BlockPos, BlockState, E> blockEntitySupplier, Block... blocks) {
        return registerBlockEntityType(name, () -> PlatHelper.newBlockEntityType(blockEntitySupplier::apply, blocks));
    }

    public static <E extends BlockEntity> RegSupplier<BlockEntityType<E>> registerBlockEntityType(
            ResourceLocation name, BiFunction<BlockPos, BlockState, E> blockEntitySupplier, Supplier<Block>... blocks) {
        return registerBlockEntityType(name, () -> PlatHelper.newBlockEntityType(blockEntitySupplier::apply,
                Arrays.stream(blocks).map(Supplier::get).toArray(Block[]::new)));
    }

    public static RegSupplier<SimpleParticleType> registerParticle(ResourceLocation name) {
        return register(name, PlatHelper::newParticle, Registries.PARTICLE_TYPE);
    }


    public static <T extends Entity> RegSupplier<EntityType<T>> registerEntityType(ResourceLocation name, EntityType.EntityFactory<T> factory,
                                                                                   MobCategory category, float width, float height) {
        return registerEntityType(name, factory, category, width, height, 5);
    }

    //not needed?
    public static <T extends Entity> RegSupplier<EntityType<T>> registerEntityType(ResourceLocation name, EntityType.EntityFactory<T> factory,
                                                                                   MobCategory category, float width,
                                                                                   float height, int clientTrackingRange) {
        return registerEntityType(name, factory, category, width, height, clientTrackingRange, 3);
    }

    @ExpectPlatform
    public static <T extends Entity> RegSupplier<EntityType<T>> registerEntityType(ResourceLocation name, EntityType.EntityFactory<T> factory,
                                                                                   MobCategory category, float width, float height,
                                                                                   int clientTrackingRange, int updateInterval) {
        throw new AssertionError();
    }

    public static <T extends Entity> RegSupplier<EntityType<T>> registerEntityType(ResourceLocation name, Supplier<EntityType<T>> type) {
        return register(name, type, Registries.ENTITY_TYPE);
    }

    public static RegSupplier<JukeboxSong> registerJukeboxSong(ResourceLocation name, Supplier<Holder<SoundEvent>> soundEvent,
                                                               float lengthInSeconds, int comparatorOutput) {
        return register(name, () -> new JukeboxSong(soundEvent.get(),
                Component.translatable(Util.makeDescriptionId("jukebox_song", name)),
                lengthInSeconds, comparatorOutput), Registries.JUKEBOX_SONG);
    }

    public static void registerCompostable(ItemLike itemLike, float chance) {
        ComposterBlock.COMPOSTABLES.put(itemLike.asItem(), chance);
    }

    @ExpectPlatform //fabric
    public static void registerItemBurnTime(Item item, int burnTime) {
        throw new AssertionError();
    }

    @ExpectPlatform //Works on both. On forge, however, consider using block method overrides
    public static void registerBlockFlammability(Block item, int fireSpread, int flammability) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void registerSimpleRecipeCondition(ResourceLocation id, Predicate<String> predicate) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static RegSupplier<CreativeModeTab> registerCreativeModeTab(
            ResourceLocation name,
            boolean searchBar,
            List<ResourceLocation> afterTabs, List<ResourceLocation> beforeTabs, Consumer<CreativeModeTab.Builder> configurator
    ) {
        throw new AssertionError();
    }

    private static final List<ResourceLocation> DEFAULT_AFTER_ENTRIES = java.util.List.of(CreativeModeTabs.SPAWN_EGGS.location());

    public static RegSupplier<CreativeModeTab> registerCreativeModeTab(ResourceLocation name, Consumer<CreativeModeTab.Builder> configurator) {
        return registerCreativeModeTab(name, false, configurator);
    }

    public static RegSupplier<CreativeModeTab> registerCreativeModeTab(ResourceLocation name, boolean searchBar, Consumer<CreativeModeTab.Builder> configurator) {
        return registerCreativeModeTab(name, searchBar, DEFAULT_AFTER_ENTRIES, List.of(), configurator);
    }

    @ExpectPlatform
    public static void addItemsToTabsRegistration(Consumer<ItemToTabEvent> event) {
        throw new AssertionError();
    }


    @FunctionalInterface
    public interface ItemToTabEvent {

        void addItems(ResourceKey<CreativeModeTab> tab, @Nullable Predicate<ItemStack> target, boolean after, List<ItemStack> items);

        default void add(ResourceKey<CreativeModeTab> tab, ItemLike... items) {
            addAfter(tab, null, items);
        }

        default void add(ResourceKey<CreativeModeTab> tab, ItemStack... items) {
            addAfter(tab, null, items);
        }

        default void addAfter(ResourceKey<CreativeModeTab> tab, Predicate<ItemStack> target, ItemLike... items) {
            List<ItemStack> stacks = new ArrayList<>();

            for (var i : items) {
                if (i.asItem().getDefaultInstance().isEmpty()) {
                    throw new IllegalStateException("Attempted to add empty item " + i + " to item tabs");
                } else stacks.add(i.asItem().getDefaultInstance());
            }
            addItems(tab, target, true, stacks);
        }

        default void addAfter(ResourceKey<CreativeModeTab> tab, Predicate<ItemStack> target, ItemStack... items) {
            addItems(tab, target, true, java.util.List.of(items));
        }

        default void addBefore(ResourceKey<CreativeModeTab> tab, Predicate<ItemStack> target, ItemLike... items) {
            List<ItemStack> stacks = new ArrayList<>();
            for (var i : items) {
                if (i.asItem().getDefaultInstance().isEmpty()) {
                    throw new IllegalStateException("Attempted to add empty item " + i + " to item tabs");
                } else stacks.add(i.asItem().getDefaultInstance());
            }
            addItems(tab, target, false, stacks);
        }

        default void addBefore(ResourceKey<CreativeModeTab> tab, Predicate<ItemStack> target, ItemStack... items) {
            addItems(tab, target, false, java.util.List.of(items));
        }

    }

    @FunctionalInterface
    public interface AttributeEvent {
        void register(EntityType<? extends LivingEntity> type, AttributeSupplier.Builder builder);
    }

    @ExpectPlatform
    public static void addAttributeRegistration(Consumer<AttributeEvent> eventListener) {
        throw new AssertionError();
    }

    @FunctionalInterface
    public interface SpawnPlacementEvent {
        <T extends Mob> void register(EntityType<T> entityType, SpawnPlacementType decoratorType,
                                      Heightmap.Types heightMapType, SpawnPlacements.SpawnPredicate<T> decoratorPredicate);
    }

    @ExpectPlatform
    public static void addSpawnPlacementsRegistration(Consumer<SpawnPlacementEvent> eventListener) {
        throw new AssertionError();
    }

    @FunctionalInterface
    public interface CommandRegistration {
        void accept(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context, Commands.CommandSelection selection);
    }

    @ExpectPlatform
    public static void addCommandRegistration(CommandRegistration eventListener) {
        throw new AssertionError();
    }

    public enum VariantType {
        BLOCK(Block::new),
        STAIRS(ModStairBlock::new),
        SLAB(SlabBlock::new),
        WALL(WallBlock::new);
        private final BiFunction<Supplier<Block>, BlockBehaviour.Properties, Block> constructor;

        VariantType(BiFunction<Supplier<Block>, BlockBehaviour.Properties, Block> constructor) {
            this.constructor = constructor;
        }

        VariantType(Function<BlockBehaviour.Properties, Block> constructor) {
            this.constructor = (b, p) -> constructor.apply(p);
        }

        public Block create(BlockBehaviour.Properties properties, @Nullable Supplier<Block> parent) {
            return this.constructor.apply(parent, properties);
        }

        public static void addToTab(ItemToTabEvent event, Map<VariantType, Supplier<Block>> blocks) {
            Map<VariantType, Supplier<Block>> m = new EnumMap<>(blocks);
            event.add(CreativeModeTabs.BUILDING_BLOCKS, m.values().stream().map(Supplier::get).toArray(Block[]::new));
        }

    }

    public static EnumMap<VariantType, Supplier<Block>> registerBaseBlockSet(ResourceLocation baseName, Block parentBlock) {
        return registerBaseBlockSet(baseName, BlockBehaviour.Properties.ofFullCopy(parentBlock));
    }

    /**
     * Registers block, slab and vertical slab
     */
    public static EnumMap<VariantType, Supplier<Block>> registerBaseBlockSet(
            ResourceLocation baseName, BlockBehaviour.Properties properties) {
        return registerBlockSet(new VariantType[]{VariantType.BLOCK, VariantType.SLAB}, baseName, properties);
    }

    public static EnumMap<VariantType, Supplier<Block>> registerReducedBlockSet(ResourceLocation baseName, Block parentBlock) {
        return registerReducedBlockSet(baseName, BlockBehaviour.Properties.ofFullCopy(parentBlock));
    }

    /**
     * Registers block, slab stairs and vertical slab
     */
    public static EnumMap<VariantType, Supplier<Block>> registerReducedBlockSet(
            ResourceLocation baseName, BlockBehaviour.Properties properties) {
        return registerBlockSet(new VariantType[]{VariantType.BLOCK, VariantType.STAIRS, VariantType.SLAB}, baseName, properties);
    }

    public static EnumMap<VariantType, Supplier<Block>> registerFullBlockSet(ResourceLocation baseName,
                                                                             Block parentBlock) {
        return registerFullBlockSet(baseName, BlockBehaviour.Properties.ofFullCopy(parentBlock));
    }

    /**
     * Utility to register a full block set
     *
     * @return registry object map
     */
    public static EnumMap<VariantType, Supplier<Block>> registerFullBlockSet(
            ResourceLocation baseName, BlockBehaviour.Properties properties) {
        return registerBlockSet(VariantType.values(), baseName, properties);
    }

    public static EnumMap<VariantType, Supplier<Block>> registerBlockSet(
            VariantType[] types, ResourceLocation baseName, BlockBehaviour.Properties properties) {

        if (!new ArrayList<>(List.of(types)).contains(VariantType.BLOCK))
            throw new IllegalStateException("Must contain base variant type");

        var block = registerBlock(baseName, () -> VariantType.BLOCK.create(properties, null));
        registerItem(baseName, () -> new BlockItem(block.get(), (new Item.Properties())));

        var m = registerBlockSet(types, block, baseName.getNamespace());
        m.put(VariantType.BLOCK, block);
        return m;
    }

    public static EnumMap<VariantType, Supplier<Block>> registerBlockSet(
            VariantType[] types, RegSupplier<? extends Block> baseBlock, String modId) {

        ResourceLocation baseName = baseBlock.getId();
        EnumMap<VariantType, Supplier<Block>> map = new EnumMap<>(VariantType.class);
        for (VariantType type : types) {
            if (type.equals(VariantType.BLOCK)) continue;
            String name = baseName.getPath();
            name += "_" + type.name().toLowerCase(Locale.ROOT);
            ResourceLocation blockId = ResourceLocation.fromNamespaceAndPath(modId, name);
            var block = registerBlock(blockId, () ->
                    type.create(BlockBehaviour.Properties.ofFullCopy(baseBlock.get()), baseBlock::get));
            registerItem(blockId, () -> new BlockItem(block.get(), new Item.Properties()));
            map.put(type, block);
        }
        return map;
    }

    public interface LootInjectEvent {
        ResourceLocation getTable();

        void addTableReference(ResourceLocation targetId);
    }

    /**
     * This uses fabric loot modify event and something equivalent to the old forge loot modift event.
     * It simply adds a loot table reference pool to the target table
     *
     * @param eventListener function that takes in the original table id and spits out the table reference id. Return null for no op
     */
    @ExpectPlatform
    public static void addLootTableInjects(Consumer<LootInjectEvent> eventListener) {
        throw new AssertionError();
    }

    // Only relevant on forge
    @ExpectPlatform
    public static void registerFireworkRecipe(FireworkExplosion.Shape shape, Item ingredient) {
        throw new AssertionError();
    }

    /**
     * Very hack solution for forge. Call this as soon as your mod is created in its constructor, offering your mod bus
     */
    @ExpectPlatform
    public static void startRegisteringFor(Object bus) {
        throw new AssertionError();
    }

    public static void addDynamicDispenserBehaviorRegistration(Consumer<DispenserHelper.Event> eventListener) {
        DispenserHelper.addListener(eventListener, DispenserHelper.Priority.NORMAL);
    }

    public static void addDynamicDispenserBehaviorRegistration(Consumer<DispenserHelper.Event> eventListener, DispenserHelper.Priority priority) {
        DispenserHelper.addListener(eventListener, priority);
    }

    /**
     * Call on mod setup. Register a new serializer for your trade
     */
    public static void registerDynamicItemListingSerializer(ResourceLocation id, MapCodec<? extends ModItemListing> trade) {
        ItemListingManager.registerSerializer(id, trade);
    }

    /**
     * Registers a simple special trade
     */
    public static void registerDynamicItemListingSerializer(ResourceLocation id, VillagerTrades.ItemListing instance, int level) {
        ItemListingManager.registerSimple(id, instance, level);
    }

}



