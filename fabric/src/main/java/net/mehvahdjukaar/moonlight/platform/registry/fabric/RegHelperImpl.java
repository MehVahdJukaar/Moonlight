package net.mehvahdjukaar.moonlight.platform.registry.fabric;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.mehvahdjukaar.moonlight.platform.registry.RegHelper;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.*;
import java.util.function.Supplier;

public class RegHelperImpl {

    //call from mod setup
    public static void registerEntries() {
        for (var m : REGISTRIES.values()) {
            var list = new ArrayList<>(m.values());
            list.sort(Comparator.comparingInt(a -> REG_PRIORITY.indexOf(a.getRegistry())));
            list.forEach(RegistryQueue::initializeEntries);
        }
    }

    private static final List<Registry<?>> REG_PRIORITY = ImmutableList.of(
            Registry.BLOCK,
            Registry.ITEM,
            Registry.ENTITY_TYPE,
            Registry.BLOCK_ENTITY_TYPE,
            Registry.RECIPE_SERIALIZER,
            Registry.MOB_EFFECT,
            Registry.ENCHANTMENT
    );


    public static final Map<Registry<?>, Map<String, RegistryQueue<?>>> REGISTRIES = new HashMap<>();

    @SuppressWarnings("unchecked")
    public static <T, E extends T> Supplier<E> register(ResourceLocation name, Supplier<E> supplier, Registry<T> reg) {
        String modId = name.getNamespace();
        var m = REGISTRIES.computeIfAbsent(reg, h -> new HashMap<>());
        RegistryQueue<T> registry = (RegistryQueue<T>) m.computeIfAbsent(modId, c -> new RegistryQueue<>(reg));
        return registry.add(supplier, name);
    }

    public static Supplier<SimpleParticleType> registerParticle(ResourceLocation name) {
        return register(name, FabricParticleTypes::simple, Registry.PARTICLE_TYPE);
    }

    public static <T extends Entity> Supplier<EntityType<T>> registerEntityType(ResourceLocation name, EntityType.EntityFactory<T> factory, MobCategory category, float width, float height, int clientTrackingRange, int updateInterval) {
        Supplier<EntityType<T>> s = () -> EntityType.Builder.of(factory, category).sized(width, height).build(name.toString());
        return register(name, s, Registry.ENTITY_TYPE);
    }



    public static <T extends BlockEntity> BlockEntityType<T> createBlockEntityType(RegHelper.BlockEntitySupplier<T> blockEntitySupplier, Block... validBlocks) {
        return FabricBlockEntityTypeBuilder.create(blockEntitySupplier::create, validBlocks).build();
    }


    public static void registerItemBurnTime(Item item, int burnTime) {
        FuelRegistry.INSTANCE.add(item, burnTime);
    }

    public static void registerBlockFlammability(Block item, int fireSpread, int flammability) {
        FlammableBlockRegistry.getDefaultInstance().add(item, fireSpread, flammability);
    }

    public static void registerCompostable(ItemLike item, float chance) {
        ComposterBlock.COMPOSTABLES.put(item, chance);
    }


}
