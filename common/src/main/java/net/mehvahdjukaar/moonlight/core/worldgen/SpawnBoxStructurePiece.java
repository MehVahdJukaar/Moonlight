package net.mehvahdjukaar.moonlight.core.worldgen;

import com.google.common.annotations.VisibleForTesting;
import net.mehvahdjukaar.moonlight.api.MoonlightRegistry;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.pools.ListPoolElement;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

//has to extend this unfortunately due to how Jigsaw Placer list works. Altho we could have hacked it since generics
public class SpawnBoxStructurePiece extends PoolElementStructurePiece {

    public SpawnBoxStructurePiece(StructureTemplateManager structureTemplateManager,
                                  EmptyBoxPoolElement poolElement,
                                  BlockPos blockPos, int groundLevelDelta, Rotation rotation,
                                  LiquidSettings liquidSettings) {
        super(structureTemplateManager, poolElement,
                blockPos, groundLevelDelta, rotation,
                poolElement.getBoundingBox(structureTemplateManager, blockPos, rotation),
                liquidSettings);
    }

    public SpawnBoxStructurePiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(context, tag);
    }

    @Override
    public StructurePieceType getType() {
        return MoonlightRegistry.SPAWN_BOX_PIECE.get();
    }

    @Override
    public void place(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random, BoundingBox box, BlockPos pos, boolean keepJigsaws) {
        //do nothing
    }

    private String targetName() {
        return null;
    }

    private static final RandomSource RAND = RandomSource.createNewThreadLocalInstance();

    //gets first random box at
    @Nullable
    public static String getNamedBoxesAt(StructureStart structureStart, BlockPos pos) {

        List<SpawnBoxStructurePiece> boxes = new ArrayList<>();
        for (StructurePiece structurePiece : structureStart.getPieces()) {
            if (structurePiece instanceof SpawnBoxStructurePiece sb) {
                boxes.add(sb);
            }
        }
        if (boxes.isEmpty()) return null;
        Util.shuffle(boxes, RAND);
        for (SpawnBoxStructurePiece box : boxes) {
            if (box.getBoundingBox().isInside(pos)) {
                return box.targetName();
            }
        }
        return null;
    }


    //same that find jigsaw does

    private static List<StructureTemplate.StructureBlockInfo> findSpawnBoxesInStructure(
            StructurePoolElement poolElement, StructureTemplateManager manager,
            BlockPos pos, Rotation rotation) {
        SinglePoolElement sp = null;
        if (poolElement instanceof SinglePoolElement spp) {
            sp = spp;
        } else if (poolElement instanceof ListPoolElement lp) {
            if (lp.elements.getFirst() instanceof SinglePoolElement spp) {
                sp = spp;
            }
        }
        if (sp != null) {
            StructureTemplate structureTemplate = sp.getTemplate(manager);
            return structureTemplate.filterBlocks(
                    pos, new StructurePlaceSettings().setRotation(rotation), MoonlightRegistry.SPAWN_BOX_BLOCK.get(), true
            );
        }
        return List.of();
    }

    public static List<SpawnBoxStructurePiece> getSpawnBoxPieces(
            PoolElementStructurePiece parentPiece, StructureTemplateManager structureTemplateManager, LiquidSettings liquidSettings) {
        var structurePoolElement = parentPiece.getElement();
        BlockPos parentPiecePos = parentPiece.getPosition();
        Rotation parentPieceRot = parentPiece.getRotation();
        var list = findSpawnBoxesInStructure(structurePoolElement,
                structureTemplateManager, parentPiecePos, parentPieceRot);
        List<SpawnBoxStructurePiece> result = new ArrayList<>();

        for (var spawnBox : list) {
            if (spawnBox.nbt() == null) continue;
            BlockPos spawnBoxPos = spawnBox.pos();
            BlockPos offset = SpawnBoxBlockEntity.readOffsetPos(spawnBox.nbt());
            Vec3i size = SpawnBoxBlockEntity.readBoxSize(spawnBox.nbt());
            Vec3i relativePos = spawnBoxPos.subtract(parentPiecePos);
            BlockPos startRelativePos = offset.offset(relativePos);

            //mojang. dont ask why I got no idea.

            EmptyBoxPoolElement boxPoolElement = new EmptyBoxPoolElement(size, startRelativePos);

            int groundLevelDelta = structurePoolElement.getGroundLevelDelta();

            SpawnBoxStructurePiece newPiece = new SpawnBoxStructurePiece(
                    structureTemplateManager, boxPoolElement,
                    spawnBoxPos, groundLevelDelta, parentPieceRot, liquidSettings
            );
            result.add(newPiece);
        }

        return result;
    }



}
