package net.mehvahdjukaar.moonlight.api.platform;

import com.mojang.brigadier.CommandDispatcher;
import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.architectury.injectables.annotations.PlatformOnly;
import net.mehvahdjukaar.moonlight.api.block.ModStairBlock;
import net.mehvahdjukaar.moonlight.api.block.VerticalSlabBlock;
import net.mehvahdjukaar.moonlight.api.misc.RegSupplier;
import net.mehvahdjukaar.moonlight.api.misc.Registrator;
import net.mehvahdjukaar.moonlight.api.misc.TriFunction;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.entity.schedule.Schedule;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.structure.StructureType;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.*;

/**
 * Helper class dedicated to platform independent registration methods
 */
public class RegHelper {

    /**
     * Call at the end of your mod init on fabric to register all your entries. Has no effect on forge. Its optional
     */
    @PlatformOnly(PlatformOnly.FABRIC)
    @ExpectPlatform
    public static void registerModEntries(String modId){
    }

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

    public static <C extends FeatureConfiguration, F extends Feature<C>> RegSupplier<PlacedFeature> registerPlacedFeature(
            ResourceLocation name, RegSupplier<ConfiguredFeature<C, F>> feature, Supplier<List<PlacementModifier>> modifiers) {
        return registerPlacedFeature(name, () -> new PlacedFeature(hackyErase(feature.getHolder()), modifiers.get()));
    }

    static <T> Holder<T> hackyErase(Holder<? extends T> holder) {
        return (Holder<T>) holder;
    }

    public static RegSupplier<PlacedFeature> registerPlacedFeature(ResourceLocation name, Supplier<PlacedFeature> featureSupplier) {
        return register(name, featureSupplier, Registries.PLACED_FEATURE);
    }

    public static <C extends FeatureConfiguration, F extends Feature<C>> RegSupplier<ConfiguredFeature<C, F>> registerConfiguredFeature(
            ResourceLocation name, Supplier<F> feature, Supplier<C> featureConfiguration) {
        return registerConfiguredFeature(name, () -> new ConfiguredFeature<>(feature.get(), featureConfiguration.get()));
    }

    public static <C extends FeatureConfiguration, F extends Feature<C>> RegSupplier<ConfiguredFeature<C, F>> registerConfiguredFeature(
            ResourceLocation name, Supplier<ConfiguredFeature<C, F>> featureSupplier) {
        return register(name, featureSupplier, Registries.CONFIGURED_FEATURE);
    }

    public static <T extends SoundEvent> RegSupplier<T> registerSound(ResourceLocation name, Supplier<T> sound) {
        return register(name, sound, Registries.SOUND_EVENT);
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

    public static <T extends Activity> RegSupplier<T> registerActivity(ResourceLocation name, Supplier<T> activity) {
        return register(name, activity, Registries.ACTIVITY);
    }

    public static <T extends Schedule> RegSupplier<T> registerSchedule(ResourceLocation name, Supplier<T> schedule) {
        return register(name, schedule, Registries.SCHEDULE);
    }

    public static <T extends MemoryModuleType<?>> RegSupplier<T> registerMemoryModule(ResourceLocation name, Supplier<T> memory) {
        return register(name, memory, Registries.MEMORY_MODULE_TYPE);
    }

    public static <T extends RecipeSerializer<?>> RegSupplier<T> registerRecipeSerializer(ResourceLocation name, Supplier<T> recipe) {
        return register(name, recipe, Registries.RECIPE_SERIALIZER);
    }

    public static <T extends BlockEntityType<E>, E extends BlockEntity> RegSupplier<T> registerBlockEntityType(ResourceLocation name, Supplier<T> blockEntity) {
        return register(name, blockEntity, Registries.BLOCK_ENTITY_TYPE);
    }

    public static RegSupplier<SimpleParticleType> registerParticle(ResourceLocation name) {
        return register(name, PlatformHelper::newParticle, Registries.PARTICLE_TYPE);
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

    @ExpectPlatform //fabric
    public static void registerBlockFlammability(Block item, int fireSpread, int flammability) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void registerVillagerTrades(VillagerProfession profession, int level, Consumer<List<VillagerTrades.ItemListing>> factories) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void registerWanderingTraderTrades(int level, Consumer<List<VillagerTrades.ItemListing>> factories) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void registerSimpleRecipeCondition(ResourceLocation id, Predicate<String> predicate) {
        throw new AssertionError();
    }

    @FunctionalInterface
    public interface AttributeEvent {
        void register(EntityType<? extends LivingEntity> type, AttributeSupplier.Builder builder);
    }

    @ExpectPlatform
    public static void addAttributeRegistration(Consumer<AttributeEvent> eventListener) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void addCommandRegistration(Consumer<CommandDispatcher<CommandSourceStack>> eventListener) {
        throw new AssertionError();
    }

    public enum VariantType {
        BLOCK(Block::new),
        SLAB(SlabBlock::new),
        VERTICAL_SLAB(VerticalSlabBlock::new),
        WALL(WallBlock::new),
        STAIRS(ModStairBlock::new);
        private final BiFunction<Supplier<Block>, BlockBehaviour.Properties, Block> constructor;

        VariantType(BiFunction<Supplier<Block>, BlockBehaviour.Properties, Block> constructor) {
            this.constructor = constructor;
        }

        VariantType(Function<BlockBehaviour.Properties, Block> constructor) {
            this.constructor = (b, p) -> constructor.apply(p);
        }

        private Block create(BlockBehaviour.Properties properties, @Nullable Supplier<Block> parent) {
            return this.constructor.apply(parent, properties);
        }
    }

    public static Map<VariantType, Supplier<Block>> registerFullBlockSet(ResourceLocation baseName,
                                                                         Block parentBlock) {
        return registerFullBlockSet(baseName, BlockBehaviour.Properties.copy(parentBlock));
    }

    /**
     * Utility to register a full block set
     *
     * @return registry object map
     */
    public static Map<VariantType, Supplier<Block>> registerFullBlockSet(
            ResourceLocation baseName, BlockBehaviour.Properties properties) {

        Map<VariantType, Supplier<Block>> map = new EnumMap<>(VariantType.class);
        for (VariantType type : VariantType.values()) {
            String modId = baseName.getNamespace();
            String name = baseName.getPath();
            if (!type.equals(VariantType.BLOCK)) name += "_" + type.name().toLowerCase(Locale.ROOT);
            Supplier<Block> base = type != VariantType.BLOCK ? map.get(VariantType.BLOCK) : null;
            Supplier<Block> block = registerBlock(new ResourceLocation(modId, name), () -> type.create(properties, base));
            registerItem(new ResourceLocation(modId, name), () -> new BlockItem(block.get(), (new Item.Properties())));
            map.put(type, block);
        }
        return map;
    }



}



