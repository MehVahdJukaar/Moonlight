package net.mehvahdjukaar.moonlight.api.platform.registry.forge;

import com.google.common.collect.ImmutableMap;
import net.mehvahdjukaar.moonlight.api.platform.registry.RegHelper;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.data.models.blockstates.PropertyDispatch;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;


public class RegHelperImpl {


    public static final Map<Registry<?>, Map<String, DeferredRegister<?>>> REGISTRIES = new HashMap<>();

    private static final Map<Registry<?>, IForgeRegistry<?>> REG_TO_FR = ImmutableMap.of(
            Registry.BLOCK, ForgeRegistries.BLOCKS,
            Registry.ITEM, ForgeRegistries.ITEMS,
            Registry.ENTITY_TYPE, ForgeRegistries.ENTITIES,
            Registry.BLOCK_ENTITY_TYPE, ForgeRegistries.BLOCK_ENTITIES,
            Registry.RECIPE_SERIALIZER, ForgeRegistries.RECIPE_SERIALIZERS,
            Registry.MOB_EFFECT, ForgeRegistries.MOB_EFFECTS,
            Registry.ENCHANTMENT, ForgeRegistries.ENCHANTMENTS,
            Registry.FEATURE, ForgeRegistries.FEATURES,
            Registry.SCHEDULE, ForgeRegistries.SCHEDULES
    );

    @SuppressWarnings("unchecked")
    public static <T, E extends T> Supplier<E> register(ResourceLocation name, Supplier<E> supplier, Registry<T> reg) {

        var m = REGISTRIES.computeIfAbsent(reg, h -> new HashMap<>());
        String modId = ModLoadingContext.get().getActiveContainer().getModId();
        DeferredRegister<T> registry = (DeferredRegister<T>) m.computeIfAbsent(modId, c -> {

            var forgeReg = REG_TO_FR.get(reg);
            if(forgeReg == null)throw new UnsupportedOperationException("Registry "+reg+" not supported");
            DeferredRegister<T> r = (DeferredRegister<T>) DeferredRegister.create(forgeReg, modId);
            var bus = FMLJavaModLoadingContext.get().getModEventBus();
            r.register(bus);
            return r;
        });
        //forge we don't care about mod id since it's always the active container one
        return registry.register(name.getPath(), supplier);
    }

    public static <T, E extends T> Supplier<E> registerAsync(ResourceLocation name, Supplier<E> supplier, Registry<T> reg) {
        return register(name, supplier, reg);
    }

    public static Supplier<SimpleParticleType> registerParticle(ResourceLocation name) {
        return register(name, () -> new SimpleParticleType(true), Registry.PARTICLE_TYPE);
    }

    public static <C extends AbstractContainerMenu> Supplier<MenuType<C>> registerMenuType(
            ResourceLocation name,
            PropertyDispatch.TriFunction<Integer, Inventory, FriendlyByteBuf, C> containerFactory) {
        return register(name, () -> IForgeMenuType.create(containerFactory::apply), Registry.MENU);
    }

    public static <T extends Entity> Supplier<EntityType<T>> registerEntityType(ResourceLocation name, EntityType.EntityFactory<T> factory, MobCategory category, float width, float height, int clientTrackingRange, int updateInterval) {
        return register(name, () -> EntityType.Builder.of(factory, category)
                .sized(width, height).build(name.toString()), Registry.ENTITY_TYPE);
    }

    public static <T extends BlockEntity> BlockEntityType<T> createBlockEntityType(RegHelper.BlockEntitySupplier<T> blockEntitySupplier, Block... validBlocks) {
        return BlockEntityType.Builder.of(blockEntitySupplier::create, validBlocks).build(null);
    }

    public static void registerItemBurnTime(Item item, int burnTime) {
    }

    public static void registerBlockFlammability(Block item, int fireSpread, int flammability) {
    }

    public static void registerVillagerTrades(VillagerProfession profession, int level, Consumer<List<VillagerTrades.ItemListing>> factories) {
        Consumer<VillagerTradesEvent> eventConsumer = event->{
            if(event.getType() == profession){
                var list = event.getTrades().get(level);
                factories.accept(list);
            }
        };
        MinecraftForge.EVENT_BUS.register(eventConsumer);
    }
    public static void registerWanderingTraderTrades(int level, Consumer<List<VillagerTrades.ItemListing>> factories) {
        Consumer<VillagerTradesEvent> eventConsumer = event->{
            var list = event.getTrades().get(level);
            factories.accept(list);
        };
        MinecraftForge.EVENT_BUS.register(eventConsumer);
    }

}
