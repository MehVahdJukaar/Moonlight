package net.mehvahdjukaar.moonlight.core.misc;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.EmptyLevelChunk;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.WritableLevelData;
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
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class FakeClientLevel extends Level {

    private static final Map<String, FakeClientLevel> INSTANCES = new Object2ObjectArrayMap<>();

    private final Scoreboard scoreboard = new Scoreboard();
    private final RecipeManager recipeManager = new RecipeManager();
    private final ChunkSource chunkManager = new DummyChunkManager();
    private final DummyLevelEntityGetter<Entity> entityGetter = new DummyLevelEntityGetter<>();

    protected FakeClientLevel() {
        super(new DummyData(),
                ResourceKey.create(Registries.DIMENSION, new ResourceLocation("dummy_" + INSTANCES.size())),
                Utils.hackyGetRegistryAccess(),
                Utils.hackyGetRegistryAccess().registryOrThrow(Registries.DIMENSION_TYPE).getHolderOrThrow(BuiltinDimensionTypes.OVERWORLD),
                () -> InactiveProfiler.INSTANCE,
                false, //client side
                false, //debug
                0, 0);
    }

    @Deprecated(forRemoval = true)
    public static FakeClientLevel getInstance() {
        return getCachedInstance();
    }

    public static FakeClientLevel getCachedInstance() {
        return getCachedInstance("dummy_world", FakeClientLevel::new);
    }

    public static <T extends FakeClientLevel> T getCachedInstance(String id, Supplier<T> constructor) {
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
            return new EmptyLevelChunk(FakeClientLevel.this, new ChunkPos(x, z), Utils.hackyGetRegistryAccess().registryOrThrow(Registries.BIOME)
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
            return FakeClientLevel.this;
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
    }

}