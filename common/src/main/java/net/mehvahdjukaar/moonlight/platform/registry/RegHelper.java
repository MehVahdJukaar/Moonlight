package net.mehvahdjukaar.moonlight.platform.registry;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.moonlight.impl.ModStairBlock;
import net.mehvahdjukaar.moonlight.impl.blocks.VerticalSlabBlock;
import net.mehvahdjukaar.moonlight.platform.PlatformHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.Locale;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class RegHelper {

    @ExpectPlatform
    public static <T, E extends T> Supplier<E> register(ResourceLocation name, Supplier<E> supplier, Registry<T> reg) {
        throw new AssertionError();
    }

    public static <T, E extends T> Supplier<E> register(String name, Supplier<E> supplier, Registry<T> reg) {
        return register(new ResourceLocation(name), supplier, reg);
    }

    public static <T extends Block> Supplier<T> registerBlock(ResourceLocation name, Supplier<T> block) {
        return register(name, block, Registry.BLOCK);
    }

    public static <T extends Block> Supplier<T> registerBlock(String name, Supplier<T> block) {
        return register(name, block, Registry.BLOCK);
    }

    public static <T extends Item> Supplier<T> registerItem(ResourceLocation name, Supplier<T> item) {
        return register(name, item, Registry.ITEM);
    }

    public static <T extends Item> Supplier<T> registerItem(String name, Supplier<T> item) {
        return register(name, item, Registry.ITEM);
    }

    public static <T extends Feature<?>> Supplier<T> registerFeature(ResourceLocation name, Supplier<T> feature) {
        return register(name, feature, Registry.FEATURE);
    }

    public static <T extends Feature<?>> Supplier<T> registerFeature(String name, Supplier<T> feature) {
        return register(name, feature, Registry.FEATURE);
    }

    public static <T extends MobEffect> Supplier<T> registerEffect(ResourceLocation name, Supplier<T> effect) {
        return register(name, effect, Registry.MOB_EFFECT);
    }

    public static <T extends MobEffect> Supplier<T> registerEffect(String name, Supplier<T> effect) {
        return register(name, effect, Registry.MOB_EFFECT);
    }

    public static <T extends Enchantment> Supplier<T> registerEnchantment(ResourceLocation name, Supplier<T> enchantment) {
        return register(name, enchantment, Registry.ENCHANTMENT);
    }

    public static <T extends Enchantment> Supplier<T> registerEnchantment(String name, Supplier<T> enchantment) {
        return register(name, enchantment, Registry.ENCHANTMENT);
    }

    public static <T extends RecipeSerializer<?>> Supplier<T> registerRecipeSerializer(ResourceLocation name, Supplier<T> recipe) {
        return register(name, recipe, Registry.RECIPE_SERIALIZER);
    }

    public static <T extends RecipeSerializer<?>> Supplier<T> registerRecipeSerializer(String name, Supplier<T> recipe) {
        return register(name, recipe, Registry.RECIPE_SERIALIZER);
    }

    public static <T extends BlockEntityType<E>, E extends BlockEntity> Supplier<T> registerBlockEntityType(ResourceLocation name, Supplier<T> blockEntity) {
        return register(name, blockEntity, Registry.BLOCK_ENTITY_TYPE);
    }
    public static <T extends BlockEntityType<E>, E extends BlockEntity> Supplier<T> registerBlockEntityType(String name, Supplier<T> blockEntity) {
        return register(new ResourceLocation(name), blockEntity, Registry.BLOCK_ENTITY_TYPE);
    }

    @ExpectPlatform
    public static Supplier<SimpleParticleType> registerParticle(ResourceLocation name) {
        throw new AssertionError();
    }

    public static Supplier<SimpleParticleType> registerParticle(String name) {
        return registerParticle(new ResourceLocation(name));
    }

    public static <T extends Entity> Supplier<EntityType<T>> registerEntityType(ResourceLocation name, EntityType.EntityFactory<T> factory,
                                                                                MobCategory category, float width, float height) {
        return registerEntityType(name, factory, category, width, height, 5);
    }

    public static <T extends Entity> Supplier<EntityType<T>> registerEntityType(String name, EntityType.EntityFactory<T> factory,
                                                                                MobCategory category, float width, float height) {
        return registerEntityType(new ResourceLocation(name), factory, category, width, height, 5);
    }


    public static <T extends Entity> Supplier<EntityType<T>> registerEntityType(ResourceLocation name, EntityType.EntityFactory<T> factory,
                                                                                MobCategory category, float width,
                                                                                float height, int clientTrackingRange) {
        return registerEntityType(name, factory, category, width, height, clientTrackingRange, 3);
    }

    public static <T extends Entity> Supplier<EntityType<T>> registerEntityType(String name, EntityType.EntityFactory<T> factory,
                                                                                MobCategory category, float width,
                                                                                float height, int clientTrackingRange) {
        return registerEntityType(new ResourceLocation(name), factory, category, width, height, clientTrackingRange, 3);
    }

    public static <T extends Entity> Supplier<EntityType<T>> registerEntityType(String name, EntityType.EntityFactory<T> factory,
                                                                                MobCategory category, float width, float height,
                                                                                int clientTrackingRange, int updateInterval) {
        return registerEntityType(new ResourceLocation(name), factory, category, width, height,clientTrackingRange,updateInterval);
    }

    @ExpectPlatform
    public static <T extends Entity> Supplier<EntityType<T>> registerEntityType(ResourceLocation name, EntityType.EntityFactory<T> factory,
                                                                                MobCategory category, float width, float height,
                                                                                int clientTrackingRange, int updateInterval) {
        throw new AssertionError();
    }


    @ExpectPlatform
    public static <T extends BlockEntity> BlockEntityType<T> createBlockEntityType(BlockEntitySupplier<T> blockEntitySupplier, Block... validBlocks) {
        throw new AssertionError();
    }

    @FunctionalInterface
    public interface BlockEntitySupplier<T extends BlockEntity> {
        @NotNull T create(BlockPos pos, BlockState state);
    }
    @ExpectPlatform
    public static void registerCompostable(ItemLike name, float chance) {
        throw new AssertionError();
    }


    @ExpectPlatform //fabric
    public static void registerItemBurnTime(Item item, int burnTime) {
        throw new AssertionError();
    }

    @ExpectPlatform //fabric
    public static void registerBlockFlammability(Block item, int fireSpread, int flammability) {
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

        private Block create(Block parent) {
            return this.constructor.apply(()->parent, BlockBehaviour.Properties.copy(parent));
        }
    }

    /**
     * Utility to register a full block set
     * @return registry object map
     */
    public static EnumMap<VariantType, Supplier<Block>> registerFullBlockSet(ResourceLocation baseName,
                                                                                   Block parentBlock, boolean isHidden) {

        EnumMap<VariantType, Supplier<Block>> map = new EnumMap<>(VariantType.class);
        for (VariantType type : VariantType.values()) {
            String modId = baseName.getNamespace();
            String name = baseName.getPath();
            if (!type.equals(VariantType.BLOCK)) name += "_" + type.name().toLowerCase(Locale.ROOT);
            Supplier<Block> block = registerBlock(new ResourceLocation(modId, name), () -> type.create(parentBlock));
            CreativeModeTab tab = switch (type) {
                case VERTICAL_SLAB -> !isHidden && PlatformHelper.isModLoaded("quark") ? CreativeModeTab.TAB_BUILDING_BLOCKS : null;
                case WALL -> !isHidden ? CreativeModeTab.TAB_DECORATIONS : null;
                default -> !isHidden ? CreativeModeTab.TAB_BUILDING_BLOCKS : null;
            };
            registerItem(new ResourceLocation(modId, name), () -> new BlockItem(block.get(), (new Item.Properties()).tab(tab)));
            map.put(type, block);
        }
        return map;
    }

}



