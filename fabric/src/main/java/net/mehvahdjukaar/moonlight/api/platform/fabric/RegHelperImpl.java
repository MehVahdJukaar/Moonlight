package net.mehvahdjukaar.moonlight.api.platform.fabric;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.mehvahdjukaar.moonlight.api.client.fabric.IFabricMenuType;
import net.mehvahdjukaar.moonlight.api.misc.RegSupplier;
import net.mehvahdjukaar.moonlight.api.misc.Registrator;
import net.mehvahdjukaar.moonlight.api.misc.TriFunction;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.misc.AntiRepostWarning;
import net.mehvahdjukaar.moonlight.core.set.fabric.BlockSetInternalImpl;
import net.mehvahdjukaar.moonlight.fabric.ResourceConditionsBridge;
import net.mehvahdjukaar.moonlight.fabric.FabricSetupCallbacks;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class RegHelperImpl {

    public static final Map<Registry<?>, Map<String, RegistryQueue<?>>> REGISTRIES = new LinkedHashMap<>();
    private static final List<Consumer<RegHelper.AttributeEvent>> ATTRIBUTE_REGISTRATIONS = new ArrayList<>();
    private static final List<Consumer<RegHelper.SpawnPlacementEvent>> SPAWN_PLACEMENT_REGISTRATIONS = new ArrayList<>();

    public static final List<Registry<?>> REG_PRIORITY = List.of(
            Registry.SOUND_EVENT, Registry.FLUID, BuiltInRegistries.BLOCK, Registry.PARTICLE_TYPE,
            Registry.ENTITY_TYPE, BuiltInRegistries.ITEM,
            BuiltInRegistries.BLOCK_ENTITY_TYPE, Registry.PLACEMENT_MODIFIERS, Registry.STRUCTURE_TYPES,
            Registry.STRUCTURE_PIECE, Registry.FEATURE, BuiltInRegistries.CONFIGURED_FEATURE,
            BuiltInRegistries.PLACED_FEATURE
    );

    //order is important here
    static {
        REG_PRIORITY.forEach(e -> REGISTRIES.put(e, new LinkedHashMap<>()));
    }

    //call from mod setup
    @ApiStatus.Internal
    public static void lateRegisterEntries() {
        for (var m : REGISTRIES.entrySet()) {
            var v = m.getValue();
            //freaking fabric just runs mod initializers in random order. hate this. we run in deterministic manner here
            var sorted = v.keySet().stream().sorted().toList();
            for(var s : sorted){
                v.get(s).initializeEntries();
            }
            if (m.getKey() == BuiltInRegistries.BLOCK) {
                //dynamic block registration after all blocks
                BlockSetInternalImpl.registerEntries();
            }
        }
        //register entities attributes now
        ATTRIBUTE_REGISTRATIONS.forEach(e -> e.accept(FabricDefaultAttributeRegistry::register));
        SPAWN_PLACEMENT_REGISTRATIONS.forEach(e->e.accept(new SpawnPlacementsImpl()));
    }

    static class SpawnPlacementsImpl implements RegHelper.SpawnPlacementEvent {
        @Override
        public <T extends Entity> void register(EntityType<T> entityType, SpawnPlacements.Type decoratorType,
                                                Heightmap.Types heightMapType, SpawnPlacements.SpawnPredicate<T> decoratorPredicate) {
            try {
                SpawnPlacements.register((EntityType<Mob>) entityType, decoratorType, heightMapType, (SpawnPlacements.SpawnPredicate<Mob>) decoratorPredicate);
            }catch (Exception e){
                Moonlight.LOGGER.warn("Skipping placement registration for {} as its not of Mob type", entityType);
            }
        }
    }

    public static void finishRegistration(String modId) {
        for (var r : REGISTRIES.entrySet()) {
            var m = r.getValue();
            var v = m.get(modId);
            if (v != null) {
                v.initializeEntries();
                m.remove(modId);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static <T, E extends T> RegSupplier<E> register(ResourceLocation name, Supplier<E> supplier, Registry<T> reg) {
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
                    if (PlatHelper.getEnv().isClient()) AntiRepostWarning.addMod(modId);

                    return new RegistryQueue<>(reg);
                });
        return registry.add(supplier, name);
    }

    public static <T, E extends T> RegSupplier<E> registerAsync(ResourceLocation name, Supplier<E> supplier, Registry<T> reg) {
        RegistryQueue.EntryWrapper<E, T> entry = new RegistryQueue.EntryWrapper<>(name, supplier, reg);
        entry.initialize();
        return entry;
    }

    public static <T> void registerInBatch(Registry<T> reg, Consumer<Registrator<T>> eventListener) {
        var m = REGISTRIES.computeIfAbsent(reg, h -> new LinkedHashMap<>());
        RegistryQueue<T> registry = (RegistryQueue<T>) m.computeIfAbsent("a", c -> new RegistryQueue<>(reg));
        registry.add(eventListener);
    }

    public static <C extends AbstractContainerMenu> RegSupplier<MenuType<C>> registerMenuType(
            ResourceLocation name,
            TriFunction<Integer, Inventory, FriendlyByteBuf, C> containerFactory) {
        return register(name, () -> IFabricMenuType.create(containerFactory::apply), Registry.MENU);
    }

    public static <T extends Entity> RegSupplier<EntityType<T>> registerEntityType(ResourceLocation name, EntityType.EntityFactory<T> factory, MobCategory category, float width, float height, int clientTrackingRange, int updateInterval) {
        Supplier<EntityType<T>> s = () -> EntityType.Builder.of(factory, category).sized(width, height).build(name.toString());
        return register(name, s, Registry.ENTITY_TYPE);
    }

    public static void registerItemBurnTime(Item item, int burnTime) {
        FuelRegistry.INSTANCE.add(item, burnTime);
    }

    public static void registerBlockFlammability(Block item, int fireSpread, int flammability) {
        FlammableBlockRegistry.getDefaultInstance().add(item, fireSpread, flammability);
    }

    public static void registerVillagerTrades(VillagerProfession profession, int level, Consumer<List<VillagerTrades.ItemListing>> factories) {
        FabricSetupCallbacks.COMMON_SETUP.add(() -> TradeOfferHelper.registerVillagerOffers(profession, level, factories));
    }

    public static void registerWanderingTraderTrades(int level, Consumer<List<VillagerTrades.ItemListing>> factories) {
        //this just runs immediately... needs to run on mod setup instead
        FabricSetupCallbacks.COMMON_SETUP.add(() -> TradeOfferHelper.registerWanderingTraderOffers(level, factories));
    }

    public static void addAttributeRegistration(Consumer<RegHelper.AttributeEvent> eventListener) {
        ATTRIBUTE_REGISTRATIONS.add(eventListener);
    }

    public static void addSpawnPlacementsRegistration(Consumer<RegHelper.SpawnPlacementEvent> eventListener) {
        SPAWN_PLACEMENT_REGISTRATIONS.add(eventListener);
    }

    public static void addCommandRegistration(Consumer<CommandDispatcher<CommandSourceStack>> eventListener) {
        CommandRegistrationCallback.EVENT.register((d, s, b) -> eventListener.accept(d));
    }



    public static void registerSimpleRecipeCondition(ResourceLocation id, Predicate<String> predicate) {
        ResourceConditionsBridge.registerSimple(id, predicate);
    }


    public static <T extends Recipe<?>> RegSupplier<RecipeSerializer<T>> registerSpecialRecipe(ResourceLocation name, Function<ResourceLocation, T> factory) {
        return RegHelper.registerRecipeSerializer(name, () -> new SimpleRecipeSerializer<>(factory));
    }

    public static <T extends Fluid> RegSupplier<T> registerFluid(ResourceLocation name, Supplier<T> fluid) {
        return register(name, fluid, Registry.FLUID);
    }




}
