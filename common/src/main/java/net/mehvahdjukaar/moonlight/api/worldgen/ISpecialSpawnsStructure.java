package net.mehvahdjukaar.moonlight.api.worldgen;

import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.jetbrains.annotations.Nullable;

public interface ISpecialSpawnsStructure {

    @Nullable WeightedList<MobSpawnSettings.SpawnerData> ml$getSpecialSpawns(
            StructureManager structureManager, Structure structure, BlockPos pos, LongSet chunkPosReferences,  MobCategory category);

}
