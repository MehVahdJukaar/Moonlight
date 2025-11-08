package net.mehvahdjukaar.moonlight.core.worldgen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.moonlight.api.MoonlightRegistry;
import net.mehvahdjukaar.moonlight.core.mixins.DebugPacketsMixin;
import net.mehvahdjukaar.moonlight.core.mixins.DebugRendererMixin;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.util.List;

public class EmptyBoxPoolElement extends StructurePoolElement {

    public static final MapCodec<EmptyBoxPoolElement> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                            Vec3i.CODEC.fieldOf("size").forGetter(e -> e.size),
                            Vec3i.CODEC.fieldOf("offset").forGetter(e -> e.offset))
                    .apply(instance, EmptyBoxPoolElement::new)
    );
    private final Vec3i size;
    private final Vec3i offset;

    public EmptyBoxPoolElement(Vec3i size, Vec3i offset) {
        super(StructureTemplatePool.Projection.RIGID);
        this.size = size;
        this.offset = offset;
    }

    @Override
    public Vec3i getSize(StructureTemplateManager structureTemplateManager, Rotation rotation) {
        return switch (rotation) {
            case COUNTERCLOCKWISE_90, CLOCKWISE_90 -> new Vec3i(this.size.getZ(), this.size.getY(), this.size.getX());
            default -> this.size;
        };
    }

    @Override
    public List<StructureTemplate.StructureBlockInfo> getShuffledJigsawBlocks(StructureTemplateManager structureTemplateManager, BlockPos pos, Rotation rotation, RandomSource random) {
        return List.of();
    }

    @Override
    public BoundingBox getBoundingBox(StructureTemplateManager structureTemplateManager, BlockPos spawnBoxPos, Rotation rotation) {
       BlockPos pivot =spawnBoxPos.offset(this.offset);
        var settings = new StructurePlaceSettings()
                .setRotationPivot(BlockPos.ZERO)
                .setRotation(Rotation.NONE);
        BlockPos startPos = spawnBoxPos.offset(offset);
        return getBoundingBox(spawnBoxPos, settings.getRotation(),
                settings.getRotationPivot(),
                settings.getMirror(), this.size);
    }

    //same as StructureTemplate
    private static BoundingBox getBoundingBox(BlockPos startPos, Rotation rotation, BlockPos pivotPos,
                                              Mirror mirror, Vec3i size) {
        Vec3i vec3i = size.offset(-1, -1, -1);
        BlockPos blockPos = StructureTemplate.transform(BlockPos.ZERO, mirror, rotation, pivotPos);
        BlockPos blockPos2 = StructureTemplate.transform(BlockPos.ZERO.offset(vec3i), mirror, rotation, pivotPos);
        return BoundingBox.fromCorners(blockPos, blockPos2).move(startPos);
    }


    @Override
    public boolean place(StructureTemplateManager structureTemplateManager, WorldGenLevel worldGenLevel, StructureManager structureManager, ChunkGenerator chunkGenerator, BlockPos blockPos, BlockPos blockPos2, Rotation rotation, BoundingBox boundingBox, RandomSource randomSource, LiquidSettings liquidSettings, boolean bl) {
        return true;
    }

    @Override
    public StructurePoolElementType<?> getType() {
        return MoonlightRegistry.EMPTY_BOX_POOL_ELEMENT.get();
    }
}
