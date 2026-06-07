package net.mehvahdjukaar.moonlight.api.platform.platform;

import com.google.gson.JsonElement;
import com.mojang.authlib.GameProfile;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.loader.api.FabricLoader;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.misc.LoaderCondition;
import net.mehvahdjukaar.moonlight.core.network.platform.ClientBoundOpenCustomMenuMessage;
import net.mehvahdjukaar.moonlight.core.network.platform.ClientBoundSpawnCustomEntityMessage;
import net.mehvahdjukaar.moonlight.platform.MoonlightFabric;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class PlatHelperImpl {

    public static PlatHelper.Platform getPlatform() {
        return PlatHelper.Platform.FABRIC;
    }

    public static boolean isModLoaded(String name) {
        return FabricLoader.getInstance().isModLoaded(name);
    }

    @Nullable
    public static <T> Field findField(Class<? super T> clazz, String fieldName) {
        return null;
    }

    @Nullable
    public static Method findMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        return null;
    }


    public static boolean isMobGriefingOn(Level level, Entity entity) {
        return level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
    }

    public static boolean isAreaLoaded(LevelReader level, BlockPos pos, int maxRange) {
        return level.hasChunksAt(pos.offset(-maxRange, -maxRange, -maxRange), pos.offset(maxRange, maxRange, maxRange));
    }

    public static PlatHelper.Side getPhysicalSide() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT ? PlatHelper.Side.CLIENT : PlatHelper.Side.SERVER;
    }

    @Nullable
    public static FoodProperties getFoodProperties(ItemStack stack, Player player) {
        return stack.get(DataComponents.FOOD);
    }

    public static boolean isDev() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }


    public static int getBurnTime(ItemStack stack) {
        var v = FuelRegistry.INSTANCE.get(stack.getItem());
        if (v == null) return 0;
        return v;
    }

    public static int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return FlammableBlockRegistry.getDefaultInstance().get(state.getBlock()).getSpreadChance();
    }

    public static int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return FlammableBlockRegistry.getDefaultInstance().get(state.getBlock()).getBurnChance();
    }

    public static boolean isFlammable(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return getFlammability(state, level, pos, direction) > 0;
    }

    public static void onCaughtFire(BlockState state, Level level, BlockPos pos, Direction direction, @Nullable LivingEntity igniter) {
    }

    public static boolean isFireSource(BlockState blockState, Level level, BlockPos pos, Direction up) {
        return blockState.is(level.dimensionType().infiniburn());
    }

    @Nullable
    public static MinecraftServer getCurrentServer() {
        return MoonlightFabric.getCurrentServer();
    }

    public static Packet<ClientGamePacketListener> getEntitySpawnPacket(Entity entity) {
        var packet = new ClientBoundSpawnCustomEntityMessage(entity);
        return (Packet<ClientGamePacketListener>) (Packet) ServerPlayNetworking.createS2CPacket(packet);
    }

    public static Path getGamePath() {
        return FabricLoader.getInstance().getGameDir();
    }

    public static SpawnEggItem newSpawnEgg(Supplier<? extends EntityType<? extends Mob>> entityType, int color, int outerColor, Item.Properties properties) {
        return new SpawnEggItem(entityType.get(), color, outerColor, properties);
    }

    public static Path getModFilePath(String modId) {
        return FabricLoader.getInstance().getModContainer(modId).orElseThrow().getRootPaths().getFirst();
    }

    public static String getModPageUrl(String modId) {
        return FabricLoader.getInstance().getModContainer(modId).orElseThrow().getMetadata().getContact().get("homepage").orElse(null);
    }

    public static String getModCurseforgeUrl(String modId) {
        // explicit curseforge key first; fall back to homepage (modders typically
        // point it at the CurseForge page by convention)
        return contact(modId, "curseforge", "homepage");
    }

    public static String getModModrinthUrl(String modId) {
        // no conventional fallback for modrinth — only return if explicitly set
        return contact(modId, "modrinth");
    }

    public static String getModSourcesUrl(String modId) {
        // sources is standard; fall back to homepage as a last resort
        return contact(modId, "sources", "homepage");
    }

    /** Returns the first non-null contact value for the given keys, or null. */
    @Nullable
    private static String contact(String modId, String... keys) {
        var container = FabricLoader.getInstance().getModContainer(modId);
        if (container.isEmpty()) return null;
        var ci = container.get().getMetadata().getContact();
        for (String key : keys) {
            String v = ci.get(key).orElse(null);
            if (v != null && !v.isBlank()) return v;
        }
        return null;
    }

    public static String getModName(String modId) {
        return FabricLoader.getInstance().getModContainer(modId).orElseThrow().getMetadata().getName();
    }

    public static FlowerPotBlock newFlowerPot(@Nullable Supplier<FlowerPotBlock> emptyPot, Supplier<? extends Block> supplier, BlockBehaviour.Properties properties) {
        return new FlowerPotBlock(supplier.get(), properties);
    }

    public static SimpleParticleType newSimpleParticle() {
        return FabricParticleTypes.simple(true);
    }

    public static <T extends ParticleOptions> ParticleType<T> newParticle(
            Function<ParticleType<T>, MapCodec<T>> codec, Function<ParticleType<T>,
            StreamCodec<? super RegistryFriendlyByteBuf, T>> streamCodec, boolean overrideLimiter) {
        return FabricParticleTypes.complex(overrideLimiter, codec, streamCodec);
    }

    public static <T extends BlockEntity> BlockEntityType<T> newBlockEntityType(PlatHelper.BlockEntitySupplier<T> blockEntitySupplier, Block... validBlocks) {
        return FabricBlockEntityTypeBuilder.create(blockEntitySupplier::create, validBlocks).build();
    }

    public static <E extends Entity> EntityType<E> newEntityType(String name,
                                                                 EntityType.EntityFactory<E> factory, MobCategory category, float width, float height,
                                                                 int clientTrackingRange, boolean velocityUpdates, int updateInterval) {
        return FabricEntityTypeBuilder.create(category, factory)
                .dimensions(EntityDimensions.scalable(width, height))
                .trackedUpdateRate(updateInterval)
                .trackRangeChunks(clientTrackingRange)
                .forceTrackedVelocityUpdates(velocityUpdates).build();
    }

    public static void addServerReloadListener(Function<HolderLookup.Provider, PreparableReloadListener> listener, ResourceLocation name) {
        Moonlight.assertInitPhase();

        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(name, p -> new IdentifiableResourceReloadListener() {
            private final PreparableReloadListener instance = listener.apply(p);

            @Override
            public ResourceLocation getFabricId() {
                return name;
            }

            @Override
            public CompletableFuture<Void> reload(PreparationBarrier preparationBarrier, ResourceManager resourceManager, ProfilerFiller preparationsProfiler,
                                                  ProfilerFiller reloadProfiler, Executor backgroundExecutor, Executor gameExecutor) {
                return instance.reload(preparationBarrier, resourceManager, preparationsProfiler, reloadProfiler, backgroundExecutor, gameExecutor);
            }
        });
    }

    public static void openCustomMenu(ServerPlayer player, MenuProvider menuProvider, Consumer<RegistryFriendlyByteBuf> extraDataProvider) {
        ClientBoundOpenCustomMenuMessage.openMenu(player, menuProvider, extraDataProvider);
    }

    public static boolean isModLoadingValid() {
        return true;
    }

    public static void addCommonSetup(Runnable setup) {
        Moonlight.assertInitPhase();

        MoonlightFabric.COMMON_SETUP_WORK.add(setup);
    }

    public static void addCommonSetupAsync(Runnable setup) {
        addCommonSetup(setup);
    }

    public static boolean evaluateRecipeCondition(DynamicOps<JsonElement> ops, JsonElement jo) {
        //TODO: re add
        //if (jo instanceof JsonObject j) ResourceConditionsImpl.conditionsMet(j);
        return true;
    }

    public static List<String> getInstalledMods() {
        return FabricLoader.getInstance().getAllMods().stream()
                .map(m -> m.getMetadata().getId())
                .filter(s -> !s.startsWith("generated_"))
                .toList();
    }

    public static Player getFakeServerPlayer(GameProfile id, ServerLevel level) {
        return FakePlayer.get(level, id);
    }

    public static boolean isInitializing() {
        return MoonlightFabric.isInitializing();
    }

    public static String getModVersion(String modId) {
        return FabricLoader.getInstance().getModContainer(modId).map(v -> v.getMetadata().getVersion().getFriendlyString())
                .orElse(null);
    }

    public static Packet<ClientGamePacketListener> getEntitySpawnPacket(Entity entity, ServerEntity serverEntity) {
        ClientBoundSpawnCustomEntityMessage packet = new ClientBoundSpawnCustomEntityMessage(entity);
        return (Packet<ClientGamePacketListener>) (Packet) ServerPlayNetworking.createS2CPacket(packet);
    }

    public static <A> void setComponent(DataComponentHolder to, DataComponentType<A> type, A componentValue) {
    }

    public static void addReloadableCommonSetup(BiConsumer<RegistryAccess, Boolean> setup) {
        CommonLifecycleEvents.TAGS_LOADED.register(setup::accept);
    }

    public static void invokeLevelUnload(Level l) {
        if (l instanceof ServerLevel sl) {
            ServerWorldEvents.UNLOAD.invoker().onWorldUnload(sl.getServer(), sl);
        }
    }

    public static boolean isFakePlayer(ServerPlayer instance) {
        return instance instanceof FakePlayer;
    }

    private record FabricCondition(ResourceCondition condition) implements LoaderCondition {
        @Override
        public boolean test(HolderLookup.Provider ra) {
            return condition.test(ra);
        }
    }

    private static final MapCodec<FabricCondition> CONDITION_CODEC =
            ResourceCondition.CONDITION_CODEC.xmap(FabricCondition::new,
                    FabricCondition::condition).fieldOf("fabric:load_conditions");

    public static MapCodec<LoaderCondition> getConditionCodec() {
        return (MapCodec) CONDITION_CODEC;
    }


}
