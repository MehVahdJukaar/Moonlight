package net.mehvahdjukaar.moonlight.core.misc;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.util.profiling.InactiveProfiler;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.level.timers.TimerQueue;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.ticks.LevelTickAccess;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class FakeServerLevel extends ServerLevel {

    private static final Map<String, FakeServerLevel> INSTANCES = new Object2ObjectArrayMap<>();

    private final Scoreboard scoreboard = new Scoreboard();
    private final RecipeManager recipeManager = new RecipeManager();
    private final ChunkSource chunkManager = new DummyChunkManager();
    private final DummyLevelEntityGetter<Entity> entityGetter = new DummyLevelEntityGetter<>();

    protected FakeServerLevel() {
        super(PlatHelper.getCurrentServer(),
                (Executor) runnable -> {},
                new LevelStorageSource.LevelStorageAccess("a", "b"),

        new DummyData(),
                ResourceKey.create(Registries.DIMENSION, new ResourceLocation("dummy_" + INSTANCES.size())),


                FAKE_SERVER_DATA,
                new ResourceKey<>(Registries.DIMENSION, new ResourceLocation("dummy")),
                new LevelStem(Holder, new DummyChunkGenerator(biome));

                Utils.hackyGetRegistryAccess(),
                Utils.hackyGetRegistryAccess().registryOrThrow(Registries.DIMENSION_TYPE).getHolderOrThrow(BuiltinDimensionTypes.OVERWORLD),
                () -> InactiveProfiler.INSTANCE,
                false, //client side
                false, //debug
                0, 0);
    }

    @Deprecated(forRemoval = true)
    public static FakeServerLevel getInstance() {
        return getCachedInstance();
    }

    public static FakeServerLevel getCachedInstance() {
        return getCachedInstance("dummy_world", FakeServerLevel::new);
    }

    public static <T extends FakeServerLevel> T getCachedInstance(String id, Supplier<T> constructor) {
        return (T) INSTANCES.computeIfAbsent(id, k -> constructor.get());
    }

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
    public MapItemSavedData getMapData(String id) {
        return null;
    }

    @Override
    public void setMapData(String pMapId, MapItemSavedData pData) {
    }

    @Override
    public int getFreeMapId() {
        return -1;
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
        throw new IllegalStateException("not implemented");
    }

    @Override
    public LevelTickAccess<Fluid> getFluidTicks() {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public void levelEvent(Player player, int eventId, BlockPos pos, int data) {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public void gameEvent(GameEvent p_220404_, Vec3 p_220405_, GameEvent.Context p_220406_) {

    }

    @Override
    public void gameEvent(@Nullable Entity pEntity, GameEvent pEvent, BlockPos pPos) {
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
        return Utils.hackyGetRegistryAccess();
    }

    @Override
    public FeatureFlagSet enabledFeatures() {
        return FeatureFlags.DEFAULT_FLAGS;
    }

    @Override
    public Holder<Biome> getUncachedNoiseBiome(int x, int y, int z) {
        return getPlains();
    }

    @NotNull
    private static Holder.Reference<Biome> getPlains() {
        return Utils.hackyGetRegistryAccess().registry(Registries.BIOME)
                .get().getHolder(ResourceKey.create(Registries.BIOME, new ResourceLocation("minecraft:plains")))
                .get();
    }

    private class DummyChunkManager extends ChunkSource {

        @Override
        public ChunkAccess getChunk(int x, int z, ChunkStatus leastStatus, boolean create) {
            return new EmptyLevelChunk(FakeServerLevel.this, new ChunkPos(x, z), Utils.hackyGetRegistryAccess().registryOrThrow(Registries.BIOME)
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
            return FakeServerLevel.this;
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

    private static class DummyData implements ServerLevelData {

        GameRules gameRules = new GameRules();
        WorldBorder.Settings worldBorderSettings = new WorldBorder.Settings(0,0,0,0,0,0,0,,0);
        @Override
        public void setXSpawn(int xSpawn) {
        }

        @Override
        public void setYSpawn(int ySpawn) {
        }

        @Override
        public void setZSpawn(int zSpawn) {
        }

        @Override
        public void setSpawnAngle(float spawnAngle) {
        }

        @Override
        public int getXSpawn() {
            return 0;
        }

        @Override
        public int getYSpawn() {
            return 0;
        }

        @Override
        public int getZSpawn() {
            return 0;
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
        public String getLevelName() {
            return "";
        }

        @Override
        public void setThundering(boolean thundering) {

        }

        @Override
        public int getRainTime() {
            return 0;
        }

        @Override
        public void setRainTime(int time) {

        }

        @Override
        public void setThunderTime(int time) {

        }

        @Override
        public int getThunderTime() {
            return 0;
        }

        @Override
        public int getClearWeatherTime() {
            return 0;
        }

        @Override
        public void setClearWeatherTime(int time) {

        }

        @Override
        public int getWanderingTraderSpawnDelay() {
            return 0;
        }

        @Override
        public void setWanderingTraderSpawnDelay(int delay) {

        }

        @Override
        public int getWanderingTraderSpawnChance() {
            return 0;
        }

        @Override
        public void setWanderingTraderSpawnChance(int chance) {

        }

        @Override
        public @Nullable UUID getWanderingTraderId() {
            return null;
        }

        @Override
        public void setWanderingTraderId(UUID id) {

        }

        @Override
        public GameType getGameType() {
            return null;
        }

        @Override
        public void setWorldBorder(WorldBorder.Settings serializer) {

        }

        @Override
        public WorldBorder.Settings getWorldBorder() {
            return worldBorderSettings;
        }

        @Override
        public boolean isInitialized() {
            return true;
        }

        @Override
        public void setInitialized(boolean initialized) {

        }

        @Override
        public boolean getAllowCommands() {
            return false;
        }

        @Override
        public void setGameType(GameType type) {

        }

        @Override
        public TimerQueue<MinecraftServer> getScheduledEvents() {
            return new TimerQueue<>();
        }

        @Override
        public void setGameTime(long time) {

        }

        @Override
        public void setDayTime(long time) {

        }
    }

    private static class DummyChunkGenerator extends ChunkGenerator {

        public DummyChunkGenerator(Holder<Biome> biome) {

            super(new FixedBiomeSource(biome));
        }

        @Override
        protected Codec<? extends ChunkGenerator> codec() {
            return null;
        }

        @Override
        public void applyCarvers(WorldGenRegion level, long seed, RandomState random, BiomeManager biomeManager, StructureManager structureManager, ChunkAccess chunk, GenerationStep.Carving step) {

        }

        @Override
        public void buildSurface(WorldGenRegion level, StructureManager structureManager, RandomState random, ChunkAccess chunk) {

        }

        @Override
        public void spawnOriginalMobs(WorldGenRegion level) {

        }

        @Override
        public int getGenDepth() {
            return 0;
        }

        @Override
        public CompletableFuture<ChunkAccess> fillFromNoise(Executor executor, Blender blender, RandomState random, StructureManager structureManager, ChunkAccess chunk) {
            return new CompletableFuture<>();
        }

        @Override
        public int getSeaLevel() {
            return 0;
        }

        @Override
        public int getMinY() {
            return 0;
        }

        @Override
        public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor level, RandomState random) {
            return 0;
        }

        @Override
        public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor height, RandomState random) {
            return new NoiseColumn(0, new BlockState[0]);
        }

        @Override
        public void addDebugScreenInfo(List<String> info, RandomState random, BlockPos pos) {

        }
    }

}