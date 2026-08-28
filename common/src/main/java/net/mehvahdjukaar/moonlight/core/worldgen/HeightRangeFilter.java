package net.mehvahdjukaar.moonlight.core.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.moonlight.api.MoonlightRegistry;
import net.mehvahdjukaar.moonlight.api.misc.CodecMapRegistry;
import net.minecraft.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import java.util.List;

public class HeightRangeFilter extends PlacementFilter {

    public static final MapCodec<HeightRangeFilter> CODEC = RecordCodecBuilder.mapCodec((i) -> i.group(
                    VerticalProvider.CODEC.listOf().optionalFieldOf("below", List.of()).forGetter(p -> p.below),
                    VerticalProvider.CODEC.listOf().optionalFieldOf("above", List.of()).forGetter(p -> p.above)
            )
            .apply(i, HeightRangeFilter::new));

    private final List<VerticalProvider> below;
    private final List<VerticalProvider> above;

    private HeightRangeFilter(List<VerticalProvider> below, List<VerticalProvider> above) {
        this.below = below;
        this.above = above;
    }


    @Override
    protected boolean shouldPlace(PlacementContext context, RandomSource random, BlockPos pos) {
        for (var provider : below) {
            int limitY = provider.resolveY(context, pos.getX(), pos.getZ());
            if (pos.getY() > limitY) return false;
        }
        for (var provider : above) {
            int limitY = provider.resolveY(context, pos.getX(), pos.getZ());
            if (pos.getY() < limitY) return false;
        }
        return true;
    }

    @Override
    public PlacementModifierType<?> type() {
        return MoonlightRegistry.HEIGHT_RANGE.get();
    }

    private interface VerticalProvider {
        int resolveY(PlacementContext context, int x, int z);

        MapCodec<? extends VerticalProvider> getCodec();
        CodecMapRegistry<VerticalProvider> REG = Util.make(() -> {
            CodecMapRegistry<VerticalProvider> reg = new CodecMapRegistry<>("vertical_providers");
            reg.register("anchor", Anchor.CODEC);
            reg.register("sea_level", SeaLevelAnchor.CODEC);
            reg.register("heightmap", HeightmapAnchor.CODEC);
            return reg;
        });


        Codec<VerticalProvider> CODEC = REG.dispatch(VerticalProvider::getCodec);
    }

    public record Anchor(VerticalAnchor anchor) implements VerticalProvider {
        public static final MapCodec<Anchor> CODEC =
                VerticalAnchor.CODEC.fieldOf("anchor").xmap(Anchor::new, Anchor::anchor);

        @Override
        public int resolveY(PlacementContext context, int x, int z) {
            return anchor.resolveY(context);
        }

        @Override
        public String toString() {
            return anchor.toString();
        }

        @Override
        public MapCodec<Anchor> getCodec() {
            return CODEC;
        }
    }

    public record SeaLevelAnchor(int offset) implements VerticalProvider {
        public static final MapCodec<SeaLevelAnchor> CODEC = Codec.INT.fieldOf("offset")
                .xmap(SeaLevelAnchor::new, SeaLevelAnchor::offset);

        @Override
        public int resolveY(PlacementContext context, int x, int z) {
            return context.getLevel().getSeaLevel() + this.offset;
        }

        @Override
        public String toString() {
            return this.offset + " from sea level";
        }

        @Override
        public MapCodec<SeaLevelAnchor> getCodec() {
            return CODEC;
        }
    }

    public record HeightmapAnchor(Heightmap.Types heightmap, int offset) implements VerticalProvider {
        public static final MapCodec<HeightmapAnchor> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Heightmap.Types.CODEC.fieldOf("heightmap").forGetter(HeightmapAnchor::heightmap),
                Codec.INT.fieldOf("offset").forGetter(HeightmapAnchor::offset)
        ).apply(instance, HeightmapAnchor::new));

        @Override
        public int resolveY(PlacementContext context, int x, int z) {
            return context.getHeight(this.heightmap, x, z) + this.offset;
        }

        @Override
        public String toString() {
            return this.offset + " from heightmap " + this.heightmap;
        }

        @Override
        public MapCodec<HeightmapAnchor> getCodec() {
            return CODEC;
        }
    }

}