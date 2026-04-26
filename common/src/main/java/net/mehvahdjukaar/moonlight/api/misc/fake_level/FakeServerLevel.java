package net.mehvahdjukaar.moonlight.api.misc.fake_level;

import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Either;
import net.mehvahdjukaar.candlelight.api.VirtualOverride;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ChunkHolder;

import net.minecraft.server.level.ChunkResult;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ProgressListener;
import net.minecraft.world.Difficulty;
import net.minecraft.world.RandomSequences;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.entity.*;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.timers.TimerCallbacks;
import net.minecraft.world.level.timers.TimerQueue;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public class FakeServerLevel extends ServerLevel {

    private final ServerScoreboard scoreboard;

    public FakeServerLevel(String name, ServerLevel original) {
        super(original.getServer(),
                Util.backgroundExecutor(),
                original.getServer().storageSource,
                new ReadOlyServerLevelData(name, original.serverLevelData),
                ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(name)),
                new LevelStem(original.dimensionTypeRegistration(), original.getChunkSource().getGenerator()),
                new DummyProgressListener(),
                false,
                0,
                Collections.emptyList(),
                false,
                new RandomSequences(0));
        //data storage and server chunk cache will cause issues....
        this.players().clear();
        this.scoreboard = new ServerScoreboard(original.getServer());
    }

    //assigned via mixin since thi can be called in constructor too
    @ApiStatus.Internal
    public static ServerChunkCache createDummyChunkCache(ServerLevel level, LevelStorageSource.LevelStorageAccess levelStorageAccess, DataFixer fixerUpper,
                                                         StructureTemplateManager structureManager, Executor dispatcher, ChunkGenerator generator,
                                                         int viewDistance, int simulationDistance, boolean sync, ChunkProgressListener progressListener,
                                                         ChunkStatusUpdateListener chunkStatusListener, Supplier<DimensionDataStorage> dataStorage) {
        return new DummyServerChunkCache(level, levelStorageAccess, fixerUpper, structureManager,
                Util.backgroundExecutor(), generator, viewDistance, simulationDistance, sync,
                new DummyProgressListener(), chunkStatusListener, dataStorage);
    }

    public static <A extends EntityAccess> PersistentEntitySectionManager<A> createDummyEntityManager(Class<A> entityClass, LevelCallback callbacks, EntityPersistentStorage permanentStorage) {
        return new DummyEntityManager<>(entityClass, callbacks, permanentStorage);
    }

    @Override
    public BlockPos getSharedSpawnPos() {
        return BlockPos.ZERO;
    }

    @Override
    public float getSharedSpawnAngle() {
        return 0;
    }

    @Override
    public boolean noCollision(Entity entity) {
        return super.noCollision(entity);
    }

    @Override
    public Iterable<VoxelShape> getBlockCollisions(@Nullable Entity entity, AABB collisionBox) {
        // return empty iterable
        return Collections.emptyList();
    }

    @Override
    public List<VoxelShape> getEntityCollisions(@Nullable Entity entity, AABB collisionBox) {
        return Collections.emptyList();
    }

    //we avoid all references to server.getPlayerList

    @Override
    public void playSound(Player player, double x, double y, double z, SoundEvent soundIn, SoundSource category,
                          float volume, float pitch) {
    }

    @Override
    public void playSound(Player player, Entity entity, SoundEvent soundEvent, SoundSource category,
                          float volume, float pitch) {
    }

    @Override
    public void playSeededSound(@Nullable Player player, Entity entity, Holder<SoundEvent> sound, SoundSource category, float volume, float pitch, long seed) {
    }

    @Override
    public void playSeededSound(@Nullable Player player, double x, double y, double z, SoundEvent sound, SoundSource source, float volume, float pitch, long seed) {
    }

    @Override
    public void playSeededSound(@Nullable Player player, double x, double y, double z, Holder<SoundEvent> sound, SoundSource source, float volume, float pitch, long seed) {
    }

    @Override
    public void levelEvent(Player player, int type, BlockPos pos, int data) {
    }

    @Override
    public void globalLevelEvent(int id, BlockPos pos, int data) {
    }

    @Override
    public void destroyBlockProgress(int breakerId, BlockPos pos, int progress) {
    }

    @Override
    public void gameEvent(Holder<GameEvent> gameEvent, BlockPos pos, GameEvent.Context context) {
    }

    @Override
    public void setDefaultSpawnPos(BlockPos pos, float angle) {
    }


//server stuff

    @Override
    protected void tickTime() {
    }

    @Override
    public ServerScoreboard getScoreboard() {
        return scoreboard;
    }

    @Override
    public void save(@Nullable ProgressListener progress, boolean flush, boolean skipSave) {
    }

    @Override
    public @Nullable BlockPos findNearestMapStructure(TagKey<Structure> structureTag, BlockPos pos, int radius, boolean skipExistingChunks) {
        return null;
    }

// map data

    @Override
    public void setMapData(MapId mapId, MapItemSavedData mapData) {
        super.setMapData(mapId, mapData);
    }

    @Override
    public @Nullable MapItemSavedData getMapData(MapId mapId) {
        return null;
    }

    @Override
    public boolean setChunkForced(int chunkX, int chunkZ, boolean add) {
        return false;
    }

    @Override
    public void setBlockEntity(BlockEntity blockEntity) {
    }

    @Override
    public boolean setBlock(BlockPos pos, BlockState state, int flags, int recursionLeft) {
        return false;
    }
// getters

    @Override
    public @Nullable BlockEntity getBlockEntity(BlockPos pos) {
        return null;
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        return Blocks.AIR.defaultBlockState();
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return Fluids.EMPTY.defaultFluidState();
    }

    @Override
    public @Nullable Entity getEntity(int id) {
        return null;
    }

    @Override
    public void tick(BooleanSupplier hasTimeLeft) {
    }

    public static class DummyProgressListener implements ChunkProgressListener {

        @Override
        public void updateSpawnPos(ChunkPos center) {
        }

        @Override
        public void onStatusChange(ChunkPos chunkPos, @Nullable net.minecraft.world.level.chunk.status.ChunkStatus chunkStatus) {
        }

        @Override
        public void start() {
        }

        @Override
        public void stop() {
        }
    }

    public static class ReadOlyServerLevelData implements ServerLevelData {
        public final String name;
        public final ServerLevelData wrapped;
        private final TimerQueue<MinecraftServer> timerQueue = new TimerQueue(TimerCallbacks.SERVER_CALLBACKS);

        public ReadOlyServerLevelData(String name, ServerLevelData wrapped) {
            this.name = name;
            this.wrapped = wrapped;
        }

        @VirtualOverride("neoforge")
        public void setDayTimePerTick(float dayTimePerTick) {
        }

        @VirtualOverride("neoforge")
        public void setDayTimeFraction(float dayTimeFraction) {
        }

        @VirtualOverride("neoforge")
        public float getDayTimeFraction() {
            return 0.0f;
        }

        @VirtualOverride("neoforge")
        public float getDayTimePerTick() {
            return -1;
        }




        @Override
        public String getLevelName() {
            return name;
        }

        @Override
        public void setThundering(boolean thundering) {
        }

        @Override
        public int getRainTime() {
            return wrapped.getRainTime();
        }

        @Override
        public void setRainTime(int time) {
        }

        @Override
        public void setThunderTime(int time) {
        }

        @Override
        public int getThunderTime() {
            return wrapped.getThunderTime();
        }

        @Override
        public int getClearWeatherTime() {
            return wrapped.getClearWeatherTime();
        }

        @Override
        public void setClearWeatherTime(int time) {
        }

        @Override
        public int getWanderingTraderSpawnDelay() {
            return wrapped.getWanderingTraderSpawnDelay();
        }

        @Override
        public void setWanderingTraderSpawnDelay(int delay) {
        }

        @Override
        public int getWanderingTraderSpawnChance() {
            return wrapped.getWanderingTraderSpawnChance();
        }

        @Override
        public void setWanderingTraderSpawnChance(int chance) {
        }

        @Override
        public @Nullable UUID getWanderingTraderId() {
            return wrapped.getWanderingTraderId();
        }

        @Override
        public void setWanderingTraderId(UUID id) {
        }

        @Override
        public GameType getGameType() {
            return wrapped.getGameType();
        }

        @Override
        public void setWorldBorder(WorldBorder.Settings serializer) {
        }

        @Override
        public WorldBorder.Settings getWorldBorder() {
            return wrapped.getWorldBorder();
        }

        @Override
        public boolean isInitialized() {
            return wrapped.isInitialized();
        }

        @Override
        public void setInitialized(boolean initialized) {
        }

        @Override
        public boolean isAllowCommands() {
            return wrapped.isAllowCommands();
        }

        @Override
        public void setGameType(GameType type) {
        }

        @Override
        public TimerQueue<MinecraftServer> getScheduledEvents() {
            return timerQueue;
        }

        @Override
        public void setGameTime(long time) {
        }

        @Override
        public void setDayTime(long time) {
        }

        @Override
        public BlockPos getSpawnPos() {
            return wrapped.getSpawnPos();
        }

        @Override
        public float getSpawnAngle() {
            return wrapped.getSpawnAngle();
        }

        @Override
        public long getGameTime() {
            return wrapped.getGameTime();
        }

        @Override
        public long getDayTime() {
            return wrapped.getDayTime();
        }

        @Override
        public boolean isThundering() {
            return wrapped.isThundering();
        }

        @Override
        public boolean isRaining() {
            return wrapped.isRaining();
        }

        @Override
        public void setRaining(boolean raining) {
        }

        @Override
        public boolean isHardcore() {
            return wrapped.isHardcore();
        }

        @Override
        public GameRules getGameRules() {
            return wrapped.getGameRules();
        }

        @Override
        public Difficulty getDifficulty() {
            return wrapped.getDifficulty();
        }

        @Override
        public boolean isDifficultyLocked() {
            return wrapped.isDifficultyLocked();
        }

        @Override
        public void setSpawn(BlockPos spawnPoint, float spawnAngle) {
        }
    }

    //not ideal really
    private static class DummyServerChunkCache extends ServerChunkCache {

        public DummyServerChunkCache(ServerLevel level, LevelStorageSource.LevelStorageAccess levelStorageAccess, DataFixer fixerUpper, StructureTemplateManager structureManager, Executor dispatcher, ChunkGenerator generator, int viewDistance, int simulationDistance, boolean sync, ChunkProgressListener progressListener, ChunkStatusUpdateListener chunkStatusListener, Supplier<DimensionDataStorage> overworldDataStorage) {
            super(level, levelStorageAccess, fixerUpper, structureManager, dispatcher, generator, viewDistance, simulationDistance, sync, progressListener, chunkStatusListener, overworldDataStorage);
        }


        @Override
        public void tick(BooleanSupplier hasTimeLeft, boolean tickChunks) {
        }

        @Override
        public ChunkAccess getChunk(int x, int z, ChunkStatus leastStatus, boolean create) {
            return getEmptyChunk(x, z);
        }

        @Override
        public @Nullable LevelChunk getChunkNow(int chunkX, int chunkZ) {
            return getEmptyChunk(chunkX, chunkZ);
        }

        @Override
        public CompletableFuture<ChunkResult<ChunkAccess>> getChunkFuture(int x, int z, ChunkStatus chunkStatus, boolean requireChunk) {
            return CompletableFuture.completedFuture(ChunkResult.of(getEmptyChunk(x, z)));
        }

        @Override
        public boolean hasChunk(int chunkX, int chunkZ) {
            return true;
        }

        @Override
        public @Nullable LightChunk getChunkForLighting(int chunkX, int chunkZ) {
            return getEmptyChunk(chunkX, chunkZ);
        }

        private EmptyLevelChunk emptyChunkInstance;

        private @NotNull EmptyLevelChunk getEmptyChunk(int x, int z) {
            if (emptyChunkInstance == null) {
                emptyChunkInstance = new EmptyLevelChunk(getLevel(), new ChunkPos(0, 0),
                        getLevel().registryAccess().registryOrThrow(Registries.BIOME).getHolderOrThrow(Biomes.FOREST));
            }
            return emptyChunkInstance;
        }

        @Override
        public void close() throws IOException {
            super.close();
        }

        @Override
        public void save(boolean flush) {
        }
    }


    private static class DummyEntityManager<A extends EntityAccess> extends PersistentEntitySectionManager<A> {

        public DummyEntityManager(Class entityClass, LevelCallback callbacks, EntityPersistentStorage permanentStorage) {
            super(entityClass, callbacks, permanentStorage);
        }

        @Override
        public void saveAll() {

        }

        @Override
        public void close() throws IOException {
            super.close();
        }
    }
}
