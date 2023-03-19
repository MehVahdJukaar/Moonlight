package net.mehvahdjukaar.moonlight.api.platform.forge;

import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import net.mehvahdjukaar.moonlight.api.misc.RegSupplier;
import net.mehvahdjukaar.moonlight.api.misc.Registrator;
import net.mehvahdjukaar.moonlight.api.misc.TriFunction;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.api.resources.recipe.forge.OptionalRecipeCondition;
import net.mehvahdjukaar.moonlight.core.misc.AntiRepostWarning;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;
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
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.event.village.WandererTradesEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;


public class RegHelperImpl {

    public record EntryWrapper<T>(RegistryObject<T> registryObject) implements RegSupplier<T> {
        @Override
        public T get() {
            return registryObject.get();
        }

        @Override
        public ResourceLocation getId() {
            return registryObject.getId();
        }

        @Override
        public Holder<T> getHolder() {
            return registryObject.getHolder().get();
        }
    }

    //this might be accessed on multiple threads
    private static final Map<ResourceKey<? extends Registry<?>>, Map<String, DeferredRegister<?>>> REGISTRIES = new ConcurrentHashMap<>();

    public static <T, E extends T> RegSupplier<E> register(
            ResourceLocation name, Supplier<E> supplier, Registry<T> reg) {
        return register(name, supplier, reg.key());
    }

    @SuppressWarnings("unchecked")
    public static <T, E extends T> RegSupplier<E> register(
            ResourceLocation name, Supplier<E> supplier, ResourceKey<? extends Registry<T>> regKey) {
        if (supplier == null) {
            throw new IllegalArgumentException("Registry entry Supplier for " + name + " can't be null");
        }
        if (name.getNamespace().equals("minecraft")) {
            throw new IllegalArgumentException("Registering under minecraft namespace is not supported");
        }
        var m = REGISTRIES.computeIfAbsent(regKey, h -> new ConcurrentHashMap<>());
        String modId = ModLoadingContext.get().getActiveContainer().getModId();
        DeferredRegister<T> registry = (DeferredRegister<T>) m.computeIfAbsent(modId, c -> {
            if (PlatHelper.getPhysicalSide().isClient()) AntiRepostWarning.addMod(modId);

            DeferredRegister<T> r = DeferredRegister.create(regKey, modId);
            var bus = FMLJavaModLoadingContext.get().getModEventBus();
            r.register(bus);
            return r;
        });
        //forge we don't care about mod id since it's always the active container one
        return new EntryWrapper<>(registry.register(name.getPath(), supplier));
    }

    public static <T, E extends T> RegSupplier<E> registerAsync(ResourceLocation name, Supplier<E> supplier, ResourceKey<? extends Registry<T>> reg) {
        return register(name, supplier, reg);
    }

    public static <T> void registerInBatch(Registry<T> reg, Consumer<Registrator<T>> eventListener) {
        Consumer<RegisterEvent> eventConsumer = event -> {
            if (event.getVanillaRegistry() == reg) {
                eventListener.accept(event.getForgeRegistry()::register);
            }
        };
        FMLJavaModLoadingContext.get().getModEventBus().addListener(eventConsumer);
    }

    public static <C extends AbstractContainerMenu> RegSupplier<MenuType<C>> registerMenuType(
            ResourceLocation name,
            TriFunction<Integer, Inventory, FriendlyByteBuf, C> containerFactory) {
        return register(name, () -> IForgeMenuType.create(containerFactory::apply), Registries.MENU);
    }

    public static <T extends
            Entity> RegSupplier<EntityType<T>> registerEntityType(ResourceLocation name, EntityType.EntityFactory<T> factory, MobCategory category,
                                                                  float width, float height, int clientTrackingRange, int updateInterval) {
        return register(name, () -> EntityType.Builder.of(factory, category)
                .sized(width, height).build(name.toString()), Registries.ENTITY_TYPE);
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
        MinecraftForge.EVENT_BUS.addListener(eventConsumer);
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
        MinecraftForge.EVENT_BUS.addListener(eventConsumer);
    }

    public static void addAttributeRegistration(Consumer<RegHelper.AttributeEvent> eventListener) {
        Consumer<EntityAttributeCreationEvent> eventConsumer = event -> {
            eventListener.accept((e, b) -> event.put(e, b.build()));
        };
        FMLJavaModLoadingContext.get().getModEventBus().addListener(eventConsumer);
    }

    public static void addCommandRegistration(Consumer<CommandDispatcher<CommandSourceStack>> eventListener) {
        Consumer<RegisterCommandsEvent> eventConsumer = event -> {
            eventListener.accept(event.getDispatcher());
        };
        MinecraftForge.EVENT_BUS.addListener(eventConsumer);
    }

    record PlacementEventImpl(SpawnPlacementRegisterEvent event) implements RegHelper.SpawnPlacementEvent {
        @Override
        public <T extends Entity> void register(EntityType<T> entityType, SpawnPlacements.Type decoratorType,
                                                Heightmap.Types heightMapType, SpawnPlacements.SpawnPredicate<T> decoratorPredicate) {
            event.register(entityType, decoratorType, heightMapType, decoratorPredicate, SpawnPlacementRegisterEvent.Operation.AND);
        }
    }

    public static void addSpawnPlacementsRegistration(Consumer<RegHelper.SpawnPlacementEvent> eventListener) {
        Consumer<SpawnPlacementRegisterEvent> eventConsumer = event -> {
            RegHelper.SpawnPlacementEvent spawnPlacementEvent = new PlacementEventImpl(event);
            eventListener.accept(spawnPlacementEvent);
        };
        FMLJavaModLoadingContext.get().getModEventBus().addListener(eventConsumer);
    }

    private static <T extends Entity> void doStuff(EntityType<T> a, SpawnPlacements.Type b, Heightmap.Types c, SpawnPlacements.SpawnPredicate<T> d) {
    }

    private static <T extends Entity> void extracted(SpawnPlacementRegisterEvent event,
                                                     SpawnPlacements.Type b, Heightmap.Types c,
                                                     SpawnPlacements.SpawnPredicate<T> d, EntityType<T> h) {
        event.register(h, b, c, d, SpawnPlacementRegisterEvent.Operation.AND);
    }


    public static void registerSimpleRecipeCondition(ResourceLocation id, Predicate<String> predicate) {
        CraftingHelper.register(new OptionalRecipeCondition(id, predicate));
    }

    public static <T extends Recipe<?>> RegSupplier<RecipeSerializer<T>> registerSpecialRecipe(ResourceLocation name, Function<ResourceLocation, T> factory) {
        return RegHelper.registerRecipeSerializer(name, () -> new OptionalSimpleRecipeSerializer<>(factory));
    }

    public static <T extends Fluid> RegSupplier<T> registerFluid(ResourceLocation name, Supplier<T> fluid) {
        var f = register(name, fluid, Registries.FLUID);
        //register fluid type
        register(name, () -> f.get().getFluidType(), ForgeRegistries.Keys.FLUID_TYPES);
        return f;
    }


    private static class OptionalSimpleRecipeSerializer<T extends Recipe<?>> extends SimpleRecipeSerializer<T> {
        public OptionalSimpleRecipeSerializer(Function<ResourceLocation, T> function) {
            super(function);
        }

        @Override
        public T fromJson(ResourceLocation recipeId, JsonObject json, ICondition.IContext context) {

            if (CraftingHelper.processConditions(GsonHelper.getAsJsonArray(json, "conditions"), context)) {
                return super.fromJson(recipeId, json, context);
            }
            return null;
        }
    }

}
