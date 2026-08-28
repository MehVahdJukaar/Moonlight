package net.mehvahdjukaar.moonlight.api.misc.fake_level;

import net.mehvahdjukaar.candlelight.api.VirtualOverride;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.ExplosionParticleInfo;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.Difficulty;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.attribute.EnvironmentAttributeSystem;
import net.minecraft.world.clock.ClockManager;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragonPart;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.crafting.RecipeAccess;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.FuelValues;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
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
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.ticks.LevelTickAccess;
import net.minecraft.world.ticks.ScheduledTick;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

// this is always considered to be client side... has to be because places like to hardcase ti ServerLevel
public class FakeLevel extends Level {


    private final Scoreboard scoreboard = new Scoreboard();
    private final RecipeManager recipeManager;
    private final FuelValues fuelValues;
    private final TickRateManager tickRateManager = new TickRateManager();
    private final ChunkSource chunkManager = new DummyChunkSource();
    private final DummyLevelEntityGetter<Entity> entityGetter = new DummyLevelEntityGetter<>();
    private final LevelTickAccess<Block> blockTicks = new EmptyLevelTickAccess<>();
    private final LevelTickAccess<Fluid> fluidTicks = new EmptyLevelTickAccess<>();
    private final EnvironmentAttributeSystem environmentAttributes = EnvironmentAttributeSystem.builder().build();
    private final ClockManager clockManager = definition -> 0L;
    private final WorldBorder worldBorder = new WorldBorder();
    private LevelData.RespawnData respawnData = LevelData.RespawnData.DEFAULT;

    protected FakeLevel(boolean clientside, String id, RegistryAccess registryAccess) {
        super(new DummyData(),
                ResourceKey.create(Registries.DIMENSION, Identifier.parse(id)),
                registryAccess,
                registryAccess.lookupOrThrow(Registries.DIMENSION_TYPE).getOrThrow(BuiltinDimensionTypes.OVERWORLD),
                true, //client side
                clientside, //debug
                0, 0);
        this.recipeManager = new RecipeManager(registryAccess);
        this.fuelValues = FuelValues.vanillaBurnTimes(registryAccess, FeatureFlags.DEFAULT_FLAGS);
    }

    @VirtualOverride("neoforge")
    public void setDayTimePerTick(float dayTimePerTick) {
    }

    @VirtualOverride("neoforge")
    public float getDayTimePerTick() {
        return -1;
    }

    @VirtualOverride("neoforge")
    public void setDayTimeFraction(float dayTimeFraction) {
    }


    @VirtualOverride("neoforge")
    public float getDayTimeFraction() {
        return 0.0f;
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
    public @Nullable MinecraftServer getServer() {
        return PlatHelper.getCurrentServer();
    }

    @Override
    public void sendBlockUpdated(BlockPos pos, BlockState oldState, BlockState newState, int flags) {
    }

    @Override
    public void playSeededSound(@Nullable Entity except, double x, double y, double z, Holder<SoundEvent> sound,
                                SoundSource source, float volume, float pitch, long seed) {
    }

    @Override
    public void playSeededSound(@Nullable Entity except, Entity sourceEntity, Holder<SoundEvent> sound,
                                SoundSource source, float volume, float pitch, long seed) {
    }

    @Override
    public void explode(@Nullable Entity source, @Nullable DamageSource damageSource,
                        @Nullable ExplosionDamageCalculator damageCalculator, double x, double y, double z,
                        float radius, boolean fire, ExplosionInteraction interactionType,
                        ParticleOptions smallExplosionParticles, ParticleOptions largeExplosionParticles,
                        WeightedList<ExplosionParticleInfo> blockParticles, Holder<SoundEvent> explosionSound) {
    }

    @Override
    public String gatherChunkSourceStats() {
        return "";
    }

    @Override
    public void setRespawnData(LevelData.RespawnData respawnData) {
        this.respawnData = respawnData;
    }

    @Override
    public LevelData.RespawnData getRespawnData() {
        return this.respawnData;
    }

    @Override
    public Entity getEntity(int id) {
        return null;
    }

    @Override
    public Collection<EnderDragonPart> dragonParts() {
        return List.of();
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
    public void destroyBlockProgress(int entityId, BlockPos pos, int progress) {
    }

    @Override
    public RecipeAccess recipeAccess() {
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
    public void levelEvent(@Nullable Entity source, int eventId, BlockPos pos, int data) {
    }

    @Override
    public void gameEvent(Holder<GameEvent> gameEvent, Vec3 pos, GameEvent.Context context) {
    }

    @Override
    public List<? extends Player> players() {
        return List.of();
    }

    @Override
    public ClockManager clockManager() {
        return clockManager;
    }

    @Override
    public EnvironmentAttributeSystem environmentAttributes() {
        return environmentAttributes;
    }

    @Override
    public PotionBrewing potionBrewing() {
        throw new UnsupportedOperationException("This level does not support potion brewing. Sorry...");
    }

    @Override
    public FuelValues fuelValues() {
        return fuelValues;
    }

    @Override
    public int getSeaLevel() {
        return 63;
    }

    @Override
    public WorldBorder getWorldBorder() {
        return worldBorder;
    }

    @Override
    public FeatureFlagSet enabledFeatures() {
        return FeatureFlags.DEFAULT_FLAGS;
    }

    @Override
    public Holder<Biome> getUncachedNoiseBiome(int x, int y, int z) {
        return getPlains(registryAccess());
    }

    @NotNull
    private static Holder.Reference<Biome> getPlains(RegistryAccess registryAccess) {
        return registryAccess.lookupOrThrow(Registries.BIOME).getOrThrow(Biomes.PLAINS);
    }

    private class DummyChunkSource extends ChunkSource {

        private final LevelLightEngine lightEngine;

        public DummyChunkSource() {
            super();
            this.lightEngine = new LevelLightEngine(this, true, FakeLevel.this.dimensionType().hasSkyLight());

        }
        @Override
        public @Nullable ChunkAccess getChunk(int x, int z, net.minecraft.world.level.chunk.status.ChunkStatus chunkStatus, boolean requireChunk) {
            return new EmptyLevelChunk(FakeLevel.this, new ChunkPos(x, z), registryAccess().lookupOrThrow(Registries.BIOME)
                    .getOrThrow(Biomes.FOREST));
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
            return lightEngine;
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

    protected static class DummyData implements WritableLevelData {

        @Override
        public void setSpawn(LevelData.RespawnData respawnData) {
        }

        @Override
        public LevelData.RespawnData getRespawnData() {
            return LevelData.RespawnData.DEFAULT;
        }

        @Override
        public long getGameTime() {
            return 0;
        }

        @Override
        public boolean isHardcore() {
            return false;
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
