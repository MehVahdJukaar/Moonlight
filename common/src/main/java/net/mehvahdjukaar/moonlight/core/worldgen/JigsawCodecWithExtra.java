package net.mehvahdjukaar.moonlight.core.worldgen;

import com.mojang.serialization.*;
import net.mehvahdjukaar.moonlight.api.worldgen.ISpawnBoxStructure;
import net.mehvahdjukaar.moonlight.api.worldgen.SpawnBoxSettings;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;

import java.util.stream.Stream;

public class JigsawCodecWithExtra extends MapCodec<JigsawStructure> {

    private static final String BOX_KEY = "spawn_boxes";
    private static final MapCodec<SpawnBoxSettings> BOX_CODEC = SpawnBoxSettings.CODEC.optionalFieldOf(BOX_KEY, SpawnBoxSettings.EMPTY);
    private final MapCodec<JigsawStructure> original;

    public JigsawCodecWithExtra(MapCodec<JigsawStructure> original) {
        this.original = original;
    }

    @Override
    public <T> Stream<T> keys(DynamicOps<T> dynamicOps) {
        return Stream.concat(original.keys(dynamicOps), Stream.of(dynamicOps.createString(BOX_KEY)));
    }

    @SuppressWarnings("ConstantValue")
    @Override
    public <T> DataResult<JigsawStructure> decode(DynamicOps<T> dynamicOps, MapLike<T> mapLike) {
        DataResult<JigsawStructure> result = original.decode(dynamicOps, mapLike);
        DataResult<SpawnBoxSettings> boxResult = BOX_CODEC.decode(dynamicOps, mapLike);
        if (boxResult.isError()) {
            return DataResult.error(() -> boxResult.error().get().message());
        }

        if (result.isSuccess()) {
            Structure value = result.getOrThrow();
            if (value instanceof ISpawnBoxStructure sb) {
                sb.ml$setSpawnBoxSettings(boxResult.getOrThrow());
            }
        }

        return result;
    }

    @SuppressWarnings("ConstantValue")
    @Override
    public <T> RecordBuilder<T> encode(JigsawStructure jigsawStructure, DynamicOps<T> dynamicOps, RecordBuilder<T> recordBuilder) {
        RecordBuilder<T> rb = original.encode(jigsawStructure, dynamicOps, recordBuilder);
        Structure str = jigsawStructure;
        if (str instanceof ISpawnBoxStructure sb) {
            SpawnBoxSettings s = sb.ml$getSpawnBoxSettings();
            if (!s.isEmpty()) {
                rb = BOX_CODEC.encode(s, dynamicOps, rb);
            }
        }
        return rb;
    }
}
