package net.mehvahdjukaar.moonlight.api.platform.forge;

import com.google.common.collect.ImmutableMap;
import com.mojang.brigadier.CommandDispatcher;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.data.models.blockstates.PropertyDispatch;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
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
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.event.village.WandererTradesEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegisterEvent;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;


public class RegHelperImpl {

    public static final Map<Registry<?>, Map<String, DeferredRegister<?>>> REGISTRIES = new HashMap<>();

    private static final Map<ResourceKey<? extends Registry<?>>, IForgeRegistry<?>> REG_TO_FR;
    static {
        var builder = ImmutableMap.<ResourceKey<? extends Registry<?>>, IForgeRegistry<?>>builder();
        for (Field f : ForgeRegistries.class.getDeclaredFields()) {
            try {
                if (IForgeRegistry.class.isAssignableFrom(f.getType())) {
                    IForgeRegistry<?> reg = (IForgeRegistry<?>) f.get(null);
                  builder.put(reg.getRegistryKey(), reg);
                }
            } catch (Exception ignored) {
            }
        }
        REG_TO_FR = builder.build();
    }

    @SuppressWarnings("unchecked")
    public static <T, E extends
            T> Supplier<E> register(ResourceLocation name, Supplier<E> supplier, Registry<T> reg) {

        var m = REGISTRIES.computeIfAbsent(reg, h -> new HashMap<>());
        String modId = ModLoadingContext.get().getActiveContainer().getModId();
        DeferredRegister<T> registry = (DeferredRegister<T>) m.computeIfAbsent(modId, c -> {

            var forgeReg = REG_TO_FR.get(reg.key());
            if (forgeReg == null) throw new UnsupportedOperationException("Registry " + reg + " not supported");
            DeferredRegister<T> r = (DeferredRegister<T>) DeferredRegister.create(forgeReg, modId);
            var bus = FMLJavaModLoadingContext.get().getModEventBus();
            r.register(bus);
            return r;
        });
        //forge we don't care about mod id since it's always the active container one
        return registry.register(name.getPath(), supplier);
    }

    public static <T, E extends
            T> Supplier<E> registerAsync(ResourceLocation name, Supplier<E> supplier, Registry<T> reg) {
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

    public static <T extends
            Entity> Supplier<EntityType<T>> registerEntityType(ResourceLocation name, EntityType.EntityFactory<T> factory, MobCategory category,
                                                               float width, float height, int clientTrackingRange, int updateInterval) {
        return register(name, () -> EntityType.Builder.of(factory, category)
                .sized(width, height).build(name.toString()), Registry.ENTITY_TYPE);
    }

    public static void registerItemBurnTime(Item item, int burnTime) {
    }

    public static void registerBlockFlammability(Block item, int fireSpread, int flammability) {
    }

    public static void registerVillagerTrades(VillagerProfession profession, int level, Consumer<
            List<VillagerTrades.ItemListing>> factories) {
        Consumer<VillagerTradesEvent> eventConsumer = event -> {
            if (event.getType() == profession) {
                var list = event.getTrades().get(level);
                factories.accept(list);
            }
        };
        MinecraftForge.EVENT_BUS.register(eventConsumer);
    }

    public static void registerWanderingTraderTrades(int level, Consumer<List<VillagerTrades.ItemListing>>
            factories) {
        //0 = common, 1 = rare
        Consumer<WandererTradesEvent> eventConsumer = event -> {
            if (level == 0) {
                factories.accept(event.getGenericTrades());
            } else {
                factories.accept(event.getRareTrades());
            }
        };
        MinecraftForge.EVENT_BUS.register(eventConsumer);
    }

    public static void addAttributeRegistration(Consumer<RegHelper.AttributeEvent> eventListener) {
        Consumer<EntityAttributeCreationEvent> eventConsumer = event -> {
            eventListener.accept((e, b) -> event.put(e, b.build()));
        };
        FMLJavaModLoadingContext.get().getModEventBus().register(eventConsumer);
    }

    public static void addMiscRegistration(Runnable eventListener) {
        Consumer<RegisterEvent> eventConsumer = event -> {
            if (event.getRegistryKey() == Registry.ENTITY_TYPE_REGISTRY) {
                eventListener.run();
            }
        };
        FMLJavaModLoadingContext.get().getModEventBus().register(eventConsumer);
    }

    public static void addCommandRegistration(Consumer<CommandDispatcher<CommandSourceStack>> eventListener) {
        Consumer<RegisterCommandsEvent> eventConsumer = event -> {
            eventListener.accept(event.getDispatcher());
        };
        MinecraftForge.EVENT_BUS.register(eventConsumer);
    }

}
