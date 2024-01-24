package net.mehvahdjukaar.moonlight.api.platform;

import com.google.common.collect.ImmutableSet;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.serialization.Codec;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.moonlight.api.block.ModStairBlock;
import net.mehvahdjukaar.moonlight.api.item.FuelBlockItem;
import net.mehvahdjukaar.moonlight.api.misc.QuadConsumer;
import net.mehvahdjukaar.moonlight.api.misc.RegSupplier;
import net.mehvahdjukaar.moonlight.api.misc.Registrator;
import net.mehvahdjukaar.moonlight.api.misc.TriFunction;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
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
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.entity.schedule.Schedule;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.material.Fluid;
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
        return registerBlockWithItem(name, blockFactory, 0);
    }

    public static <T extends Block> RegSupplier<T> registerBlockWithItem(ResourceLocation name, Supplier<T> blockFactory, int burnTime) {
        return registerBlockWithItem(name, blockFactory, new Item.Properties(), burnTime);
    }

    public static <T extends Block> RegSupplier<T> registerBlockWithItem(ResourceLocation name, Supplier<T> blockFactory, Item.Properties properties, int burnTime) {
        RegSupplier<T> block = registerBlock(name, blockFactory);
        registerItem(name, () -> {
            if (burnTime == 0) return new BlockItem(block.get(), properties);
            else return new FuelBlockItem(block.get(), properties, () -> burnTime);
        });
        return block;
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

    public static <T extends PaintingVariant> RegSupplier<T> registerPainting(ResourceLocation name, Supplier<T> painting) {
        return register(name, painting, Registries.PAINTING_VARIANT);
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

    public static <T extends BlockEntityType<E>, E extends BlockEntity> RegSupplier<T> registerBlockEntityType(ResourceLocation name, Supplier<T> blockEntity) {
        return register(name, blockEntity, Registries.BLOCK_ENTITY_TYPE);
    }

    public static <T extends BlockEntityType<E>, E extends BlockEntity> RegSupplier<T> registerBlockEntityType(
            ResourceLocation name, BiFunction<BlockPos, BlockState, E> blockEntitySupplier, Block... block) {
        return (RegSupplier<T>) registerBlockEntityType(name, () -> PlatHelper.newBlockEntityType(blockEntitySupplier::apply, block));
    }

    public static RegSupplier<SimpleParticleType> registerParticle(ResourceLocation name) {
        return register(name, PlatHelper::newParticle, Registries.PARTICLE_TYPE);
    }

    public static RegSupplier<BannerPattern> registerBannerPattern(ResourceLocation name, String patternId) {
        return register(name, () -> new BannerPattern(patternId), Registries.BANNER_PATTERN);
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

    public static void registerCompostable(ItemLike name, float chance) {
        ComposterBlock.COMPOSTABLES.put(name, chance);
    }

    @ExpectPlatform //fabric
    public static void registerItemBurnTime(Item item, int burnTime) {
        throw new AssertionError();
    }

    @ExpectPlatform //Works on both. On forge, however, consider using block method overrides
    public static void registerBlockFlammability(Block item, int fireSpread, int flammability) {
        throw new AssertionError();
    }

    @Deprecated(forRemoval = true)
    @ExpectPlatform
    public static void registerVillagerTrades(VillagerProfession profession, int level, Consumer<List<VillagerTrades.ItemListing>> factories) {
        throw new AssertionError();
    }

    @Deprecated(forRemoval = true)
    @ExpectPlatform
    public static void registerWanderingTraderTrades(int level, Consumer<List<VillagerTrades.ItemListing>> factories) {
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

    public record ItemToTabEvent(
            QuadConsumer<ResourceKey<CreativeModeTab>, @Nullable Predicate<ItemStack>, Boolean, Collection<ItemStack>> action) {


        public void add(ResourceKey<CreativeModeTab> tab, ItemLike... items) {
            addAfter(tab, null, items);
        }

        public void add(ResourceKey<CreativeModeTab> tab, ItemStack... items) {
            addAfter(tab, null, items);
        }

        public void addAfter(ResourceKey<CreativeModeTab> tab, Predicate<ItemStack> target, ItemLike... items) {
            List<ItemStack> stacks = new ArrayList<>();
            for (var i : items) {
                if (i.asItem().getDefaultInstance().isEmpty()) {
                    if (PlatHelper.isDev())
                        throw new IllegalStateException("Attempted to add empty item " + i + " to item tabs");
                } else stacks.add(i.asItem().getDefaultInstance());
            }
            action.accept(tab, target, true, stacks);
        }

        public void addAfter(ResourceKey<CreativeModeTab> tab, Predicate<ItemStack> target, ItemStack... items) {
            action.accept(tab, target, true, java.util.List.of(items));
        }

        public void addBefore(ResourceKey<CreativeModeTab> tab, Predicate<ItemStack> target, ItemLike... items) {
            List<ItemStack> stacks = new ArrayList<>();
            for (var i : items) {
                if (i.asItem().getDefaultInstance().isEmpty()) {
                    if (PlatHelper.isDev())
                        throw new IllegalStateException("Attempted to add empty item " + i + " to item tabs");
                } else stacks.add(i.asItem().getDefaultInstance());
            }
            action.accept(tab, target, false, stacks);
        }

        public void addBefore(ResourceKey<CreativeModeTab> tab, Predicate<ItemStack> target, ItemStack... items) {
            action.accept(tab, target, false, java.util.List.of(items));
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
        <T extends Entity> void register(EntityType<T> entityType, SpawnPlacements.Type decoratorType,
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
        return registerBaseBlockSet(baseName, BlockBehaviour.Properties.copy(parentBlock));
    }

    /**
     * Registers block, slab and vertical slab
     */
    public static EnumMap<VariantType, Supplier<Block>> registerBaseBlockSet(
            ResourceLocation baseName, BlockBehaviour.Properties properties) {
        return registerBlockSet(new VariantType[]{VariantType.BLOCK, VariantType.SLAB}, baseName, properties);
    }

    public static EnumMap<VariantType, Supplier<Block>> registerReducedBlockSet(ResourceLocation baseName, Block parentBlock) {
        return registerReducedBlockSet(baseName, BlockBehaviour.Properties.copy(parentBlock));
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
        return registerFullBlockSet(baseName, BlockBehaviour.Properties.copy(parentBlock));
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
            ResourceLocation blockId = new ResourceLocation(modId, name);
            var block = registerBlock(blockId, () ->
                    type.create(BlockBehaviour.Properties.copy(baseBlock.get()), baseBlock::get));
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


    // Animal food stuff

    public static void registerChickenFood(ItemLike... food) {
        List<ItemStack> chickenFood = new ArrayList<>(List.of(Chicken.FOOD_ITEMS.getItems()));
        Arrays.stream(food).forEach(f -> chickenFood.add(f.asItem().getDefaultInstance()));
        Chicken.FOOD_ITEMS = Ingredient.of(chickenFood.stream());
    }

    public static void registerHorseFood(ItemLike... food) {
        List<ItemStack> horseFood = new ArrayList<>(List.of(AbstractHorse.FOOD_ITEMS.getItems()));
        Arrays.stream(food).forEach(f -> horseFood.add(f.asItem().getDefaultInstance()));
        AbstractHorse.FOOD_ITEMS = Ingredient.of(horseFood.stream());
    }

    public static void registerParrotFood(ItemLike... food) {
        Arrays.stream(food).forEach(f -> Parrot.TAME_FOOD.add(f.asItem()));
    }

    // Only relevant on forge
    @ExpectPlatform
    public static void registerFireworkRecipe(FireworkRocketItem.Shape shape, Item ingredient) {
        throw new AssertionError();
    }

}



