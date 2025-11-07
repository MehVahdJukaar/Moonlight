package net.mehvahdjukaar.moonlight.core.mixins;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.mehvahdjukaar.moonlight.api.MoonlightRegistry;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TemplateStructurePiece.class)
public class TemplateStructurePieceMixin {

    @Shadow
    protected StructureTemplate template;

    @Shadow
    protected BlockPos templatePosition;

    @Shadow
    protected StructurePlaceSettings placeSettings;

    @Inject(method = "postProcess", at = @At("TAIL"))
    public void ml$processBoxes(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random, BoundingBox box, ChunkPos chunkPos, BlockPos pos, CallbackInfo ci) {
        //same as for jigsaw pieces
        for (StructureTemplate.StructureBlockInfo info : this.template.filterBlocks(this.templatePosition, this.placeSettings,
                MoonlightRegistry.SPAWN_BOX_BLOCK.get())) {
            if (info.nbt() != null) {
                String s = info.nbt().getString("final_state");
                BlockState blockstate = Blocks.AIR.defaultBlockState();

                try {
                    blockstate = BlockStateParser.parseForBlock(level.holderLookup(Registries.BLOCK), s, true).blockState();
                } catch (CommandSyntaxException commandsyntaxexception) {
                    Moonlight.LOGGER.error("Error while parsing blockstate {} in jigsaw block @ {}", s, info.pos());
                }

                level.setBlock(info.pos(), blockstate, 3);
            }
        }
    }
}

