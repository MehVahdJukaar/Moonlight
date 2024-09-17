package net.mehvahdjukaar.moonlight.api.misc;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.util.profiling.InactiveProfiler;
import net.minecraft.world.Difficulty;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.EmptyLevelChunk;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.ticks.LevelTickAccess;
import net.minecraft.world.ticks.ScheduledTick;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class FakeLevel extends Level {

    private static final Map<String, FakeLevel> INSTANCES = new Object2ObjectArrayMap<>();

    private final Scoreboard scoreboard = new Scoreboard();
    private final RecipeManager recipeManager;
    private final MapId mapId = new MapId(0);
    private final TickRateManager tickRateManager = new TickRateManager();
    private final ChunkSource chunkManager = new DummyChunkManager();
    private final DummyLevelEntityGetter<Entity> entityGetter = new DummyLevelEntityGetter<>();
    private final WeakReference<RegistryAccess> registryAccess;
    private final LevelTickAccess<Block> blockTicks = new EmptyLevelTickAccess<>();
    private final LevelTickAccess<Fluid> fluidTicks = new EmptyLevelTickAccess<>();

    protected FakeLevel(boolean client, String id, RegistryAccess registryAccess) {
        super(new DummyData(),
                ResourceKey.create(Registries.DIMENSION, ResourceLocation.tryParse(id)),
                registryAccess,
                registryAccess.registryOrThrow(Registries.DIMENSION_TYPE).getHolderOrThrow(BuiltinDimensionTypes.OVERWORLD),
                () -> InactiveProfiler.INSTANCE,
                client, //client side
                false, //debug
                0, 0);
        this.registryAccess = new WeakReference<>(registryAccess);
        this.recipeManager = new RecipeManager(registryAccess);
    }

    public static FakeLevel getDefault(boolean client, RegistryAccess registryAccess) {
        return get("dummy_world", client,registryAccess,  FakeLevel::new);
    }

    public static <T extends FakeLevel> T get(String id, boolean client,RegistryAccess registryAccess, TriFunction<Boolean, String, RegistryAccess, T> constructor) {
        if (client) {
            id = "client_" + id;
        }
        String finalId = id;
        return (T) INSTANCES.computeIfAbsent(id, k -> constructor.apply(client, finalId, registryAccess));
    }

    @ApiStatus.Internal
    public static void clearInstance() {
        INSTANCES.clear();
    }

    @Override
    public Scoreboard getScoreboard() {
        return this.scoreboard;
    }

    @Override
    public ChunkSource getChunkSource() {
        return this.chunkManager;
    }

    @Override
    public void playSound(Player player, double x, double y, double z, SoundEvent sound, SoundSource category, float volume, float pitch) {
    }

    @Override
    public void sendBlockUpdated(BlockPos pos, BlockState oldState, BlockState newState, int flags) {
    }

    @Override
    public void playSeededSound(@Nullable Player player, double d, double e, double f, Holder<SoundEvent> holder, SoundSource soundSource, float g, float h, long l) {
    }

    @Override
    public void playSeededSound(@Nullable Player player, double x, double y, double z, SoundEvent soundEvent, SoundSource soundSource, float p_220369_, float p_220370_, long p_220371_) {
    }

    @Override
    public void playSeededSound(@Nullable Player player, Entity entity, Holder<SoundEvent> holder, SoundSource soundSource, float f, float g, long l) {
    }

    @Override
    public void playSound(Player player, Entity entity, SoundEvent sound, SoundSource category, float volume, float pitch) {
    }

    @Override
    public String gatherChunkSourceStats() {
        return "";
    }

    @Override
    public Entity getEntity(int id) {
        return null;
    }

    @Override
    public TickRateManager tickRateManager() {
        return tickRateManager;
    }

    @Override
    public @Nullable MapItemSavedData getMapData(MapId mapId) {
        return null;
    }

    @Override
    public void setMapData(MapId mapId, MapItemSavedData mapData) {

    }

    @Override
    public MapId getFreeMapId() {
        return mapId;
    }

    @Override
    public void destroyBlockProgress(int entityId, BlockPos pos, int progress) {
    }

    @Override
    public RecipeManager getRecipeManager() {
        return recipeManager;
    }

    @Override
    protected LevelEntityGetter<Entity> getEntities() {
        return entityGetter;
    }

    @Override
    public LevelTickAccess<Block> getBlockTicks() {
        return blockTicks;
    }

    @Override
    public LevelTickAccess<Fluid> getFluidTicks() {
        return fluidTicks;
    }

    @Override
    public void levelEvent(Player player, int eventId, BlockPos pos, int data) {
    }

    @Override
    public void gameEvent(Holder<GameEvent> gameEvent, Vec3 pos, GameEvent.Context context) {

    }

    @Override
    public float getShade(Direction direction, boolean shaded) {
        return 0;
    }

    @Override
    public List<? extends Player> players() {
        return List.of();
    }

    @Override
    public RegistryAccess registryAccess() {
        return registryAccess.get();
    }

    @Override
    public PotionBrewing potionBrewing() {
        return null;
    }

    @Override
    public FeatureFlagSet enabledFeatures() {
        return FeatureFlags.DEFAULT_FLAGS;
    }

    @Override
    public Holder<Biome> getUncachedNoiseBiome(int x, int y, int z) {
        return getPlains(registryAccess.get());
    }

    @NotNull
    private static Holder.Reference<Biome> getPlains(RegistryAccess registryAccess) {
        return registryAccess.registry(Registries.BIOME)
                .get().getHolder(ResourceKey.create(Registries.BIOME, ResourceLocation.withDefaultNamespace("plains")))
                .get();
    }

    private class DummyChunkManager extends ChunkSource {

        @Override
        public @Nullable ChunkAccess getChunk(int x, int z, net.minecraft.world.level.chunk.status.ChunkStatus chunkStatus, boolean requireChunk) {
            return new EmptyLevelChunk(FakeLevel.this, new ChunkPos(x, z), registryAccess.get().registryOrThrow(Registries.BIOME)
                    .getHolderOrThrow(Biomes.FOREST));
        }

        @Override
        public void tick(BooleanSupplier supplier, boolean b) {
        }

        @Override
        public String gatherStats() {
            return "";
        }

        @Override
        public int getLoadedChunksCount() {
            return 0;
        }

        @Override
        public LevelLightEngine getLightEngine() {
            throw new IllegalStateException("not implemented"); // TODO
        }

        @Override
        public BlockGetter getLevel() {
            return FakeLevel.this;
        }

    }

    public static class DummyLevelEntityGetter<T extends EntityAccess> implements LevelEntityGetter<T> {

        public T get(int id) {
            return null;
        }

        public T get(UUID pUuid) {
            return null;
        }

        public Iterable<T> getAll() {
            return Collections.emptyList();
        }

        public <U extends T> void get(EntityTypeTest<T, U> tuEntityTypeTest, AbortableIterationConsumer<U> uAbortableIterationConsumer) {
        }

        public void get(AABB boundingBox, Consumer<T> tConsumer) {
        }

        public <U extends T> void get(EntityTypeTest<T, U> tuEntityTypeTest, AABB bounds, AbortableIterationConsumer<U> uAbortableIterationConsumer) {
        }
    }

    private static class DummyData implements WritableLevelData {

        GameRules gameRules = new GameRules();

        @Override
        public BlockPos getSpawnPos() {
            return BlockPos.ZERO;
        }

        @Override
        public float getSpawnAngle() {
            return 0;
        }

        @Override
        public long getGameTime() {
            return 0;
        }

        @Override
        public long getDayTime() {
            return 0;
        }

        @Override
        public boolean isThundering() {
            return false;
        }

        @Override
        public boolean isRaining() {
            return false;
        }

        @Override
        public void setRaining(boolean raining) {
        }

        @Override
        public boolean isHardcore() {
            return false;
        }

        @Override
        public GameRules getGameRules() {
            return gameRules;
        }

        @Override
        public Difficulty getDifficulty() {
            return Difficulty.NORMAL;
        }

        @Override
        public boolean isDifficultyLocked() {
            return false;
        }

        @Override
        public void setSpawn(BlockPos spawnPoint, float spawnAngle) {

        }
    }

    private static class EmptyLevelTickAccess<T> implements LevelTickAccess<T>{

        @Override
        public boolean willTickThisTick(BlockPos pos, T type) {
            return false;
        }

        @Override
        public void schedule(ScheduledTick<T> tick) {

        }

        @Override
        public boolean hasScheduledTick(BlockPos pos, T type) {
            return false;
        }

        @Override
        public int count() {
            return 0;
        }
    }

}