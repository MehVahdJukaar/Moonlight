package net.mehvahdjukaar.moonlight.core.misc;

import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.profiling.InactiveProfiler;
import net.minecraft.world.Difficulty;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
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
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.ticks.LevelTickAccess;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BooleanSupplier;

public class DummyWorld extends Level {

    private static DummyWorld instance;

    private final Scoreboard scoreboard = new Scoreboard();
    private final ChunkSource chunkManager = new DummyChunkManager(this);
    private final TickRateManager tickRateManager = new TickRateManager();

    private DummyWorld() {
        super(new ClientLevel.ClientLevelData(Difficulty.NORMAL, false, false),
                ResourceKey.create(Registries.DIMENSION, new ResourceLocation("dummy")),
                Utils.hackyGetRegistryAccess(),
                Utils.hackyGetRegistryAccess().registryOrThrow(Registries.DIMENSION_TYPE).getHolderOrThrow(BuiltinDimensionTypes.OVERWORLD),
                () -> InactiveProfiler.INSTANCE, true, false, 0, 0);
    }

    public static DummyWorld getInstance() {
        if (instance == null) {
            instance = new DummyWorld();
        }
        return instance;
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
        throw new IllegalStateException("not implemented");
    }

    @Override
    public void playSeededSound(@Nullable Player player, double d, double e, double f, Holder<SoundEvent> holder, SoundSource soundSource, float g, float h, long l) {

    }

    @Override
    public void playSeededSound(@Nullable Player player, double x, double y, double z, SoundEvent soundEvent, SoundSource soundSource, float p_220369_, float p_220370_, long p_220371_) {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public void playSeededSound(@Nullable Player player, Entity entity, Holder<SoundEvent> holder, SoundSource soundSource, float f, float g, long l) {
        throw new IllegalStateException("not implemented");

    }

    @Override
    public void playSound(Player player, Entity entity, SoundEvent sound, SoundSource category, float volume, float pitch) {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public String gatherChunkSourceStats() {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public Entity getEntity(int id) {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public TickRateManager tickRateManager() {
        return tickRateManager;
    }

    @Override
    public MapItemSavedData getMapData(String id) {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public void setMapData(String pMapId, MapItemSavedData pData) {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public int getFreeMapId() {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public void destroyBlockProgress(int entityId, BlockPos pos, int progress) {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public RecipeManager getRecipeManager() {
        throw new IllegalStateException("not implemented");
    }

    @Override
    protected LevelEntityGetter<Entity> getEntities() {
        throw new IllegalStateException("not implemented");
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
        throw new IllegalStateException("not implemented");
    }

    @Override
    public List<? extends Player> players() {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public RegistryAccess registryAccess() {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public FeatureFlagSet enabledFeatures() {
        return null;
    }

    @Override
    public Holder<Biome> getUncachedNoiseBiome(int x, int y, int z) {
        throw new IllegalStateException("not implemented");
    }

    private static class DummyChunkManager extends ChunkSource {

        private final Level world;

        public DummyChunkManager(Level world) {
            this.world = world;
        }

        @Override
        public ChunkAccess getChunk(int x, int z, ChunkStatus leastStatus, boolean create) {
            return new EmptyLevelChunk(this.world, new ChunkPos(x, z), Utils.hackyGetRegistryAccess().registryOrThrow(Registries.BIOME)
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
            return this.world;
        }

    }
}