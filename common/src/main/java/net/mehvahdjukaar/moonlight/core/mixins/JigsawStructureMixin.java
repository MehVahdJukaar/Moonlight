package net.mehvahdjukaar.moonlight.core.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.serialization.MapCodec;
import net.mehvahdjukaar.moonlight.api.worldgen.ISpawnBoxStructure;
import net.mehvahdjukaar.moonlight.api.worldgen.SpawnBoxSettings;
import net.mehvahdjukaar.moonlight.core.worldgen.JigsawCodecWithExtra;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(JigsawStructure.class)
public abstract class JigsawStructureMixin implements ISpawnBoxStructure {

    @Unique
    private SpawnBoxSettings ml$specialSpawnsSettings = SpawnBoxSettings.EMPTY;


    @Override
    public @NotNull SpawnBoxSettings ml$getSpawnBoxSettings() {
        return ml$specialSpawnsSettings;
    }

    @Override
    public void ml$setSpawnBoxSettings(@NotNull SpawnBoxSettings settings) {
        this.ml$specialSpawnsSettings = settings;
    }

    @ModifyExpressionValue(method = "<clinit>", at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/codecs/RecordCodecBuilder;mapCodec(Ljava/util/function/Function;)Lcom/mojang/serialization/MapCodec;"))
    private static MapCodec<JigsawStructure> ml$addStuffToCodec(MapCodec<JigsawStructure> original) {
        return new JigsawCodecWithExtra(original);
    }
}
