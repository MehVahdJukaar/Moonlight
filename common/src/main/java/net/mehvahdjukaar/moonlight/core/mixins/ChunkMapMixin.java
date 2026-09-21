package net.mehvahdjukaar.moonlight.core.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.mehvahdjukaar.moonlight.api.misc.fake_level.FakeServerLevel;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.stream.Stream;

@Mixin(ChunkMap.class)
public class ChunkMapMixin {

    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/RandomState;create(Lnet/minecraft/world/level/levelgen/NoiseGeneratorSettings;Lnet/minecraft/core/HolderGetter;J)Lnet/minecraft/world/level/levelgen/RandomState;"))
    private RandomState ml$dummyRandomStateForFakeLevels(NoiseGeneratorSettings settings, HolderGetter<NormalNoise.NoiseParameters> noises, long seed,
                                                         Operation<RandomState> original, @Local(argsOnly = true) ServerLevel level) {
        if (level instanceof FakeServerLevel) settings = NoiseGeneratorSettings.dummy();
        return original.call(settings, noises, seed);
    }

    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/ChunkGenerator;createState(Lnet/minecraft/core/HolderLookup;Lnet/minecraft/world/level/levelgen/RandomState;J)Lnet/minecraft/world/level/chunk/ChunkGeneratorStructureState;"))
    private ChunkGeneratorStructureState ml$emptyStructureStateForFakeLevels(ChunkGenerator generator, HolderLookup<StructureSet> structureSets, RandomState randomState, long seed,
                                                                           Operation<ChunkGeneratorStructureState> original, @Local(argsOnly = true) ServerLevel level) {
        if (level instanceof FakeServerLevel) {
            return ChunkGeneratorStructureState.createForFlat(randomState, seed, generator.getBiomeSource(), Stream.empty());
        }
        return original.call(generator, structureSets, randomState, seed);
    }
}
