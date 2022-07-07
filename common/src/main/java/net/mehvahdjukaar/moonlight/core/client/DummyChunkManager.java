package net.mehvahdjukaar.moonlight.core.client;

import net.minecraft.data.BuiltinRegistries;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.EmptyLevelChunk;
import net.minecraft.world.level.lighting.LevelLightEngine;

import java.util.function.BooleanSupplier;

public class DummyChunkManager extends ChunkSource {

    private final Level world;

    public DummyChunkManager(Level world) {
        this.world = world;
    }

    @Override
    public ChunkAccess getChunk(int x, int z, ChunkStatus leastStatus, boolean create) {
        return new EmptyLevelChunk(this.world, new ChunkPos(x, z), BuiltinRegistries.BIOME.getHolderOrThrow(Biomes.FOREST));
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
