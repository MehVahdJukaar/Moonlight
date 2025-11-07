package net.mehvahdjukaar.moonlight.core.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.mehvahdjukaar.moonlight.api.worldgen.ISpecialSpawnsStructure;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(ChunkGenerator.class)
public class ChunkGeneratorMixin {

    @Inject(method = "getMobsAt", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/levelgen/structure/Structure;spawnOverrides()Ljava/util/Map;"), cancellable = true)
    private void ml$getSpecialSpawnsOverrides(Holder<Biome> biome, StructureManager structureManager, MobCategory category, BlockPos pos, CallbackInfoReturnable<WeightedRandomList<MobSpawnSettings.SpawnerData>> cir,
                                              @Local Structure structure, @Local Map.Entry<Structure, LongSet> entry) {

        LongSet chunkPosReferences = entry.getValue();
        if (structure instanceof ISpecialSpawnsStructure sps) {
            WeightedRandomList<MobSpawnSettings.SpawnerData> specialSpawns = sps.ml$getSpecialSpawns(
                    structureManager, structure, pos, chunkPosReferences, category);
            if (specialSpawns != null) {
                cir.setReturnValue(specialSpawns);
            }
        }
    }
}
