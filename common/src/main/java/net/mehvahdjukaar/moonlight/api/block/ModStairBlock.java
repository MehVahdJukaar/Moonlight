package net.mehvahdjukaar.moonlight.api.block;

import com.google.common.base.Preconditions;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.lang.reflect.Field;
import java.util.function.Supplier;

public class ModStairBlock extends StairBlock {
    public static final MapCodec<ModStairBlock> CODEC = RecordCodecBuilder.mapCodec((i) -> i.group(
            BuiltInRegistries.BLOCK.byNameCodec().fieldOf("base_block")
                    .forGetter(ModStairBlock::getBaseBlock), propertiesCodec()
    ).apply(i, (b, s) -> new ModStairBlock(() -> b, s)));

    private static final Field FORGE_BLOCK_SUPPLIER = PlatHelper.findField(StairBlock.class, "stateSupplier");
    private final Supplier<Block> baseBlock;

    public ModStairBlock(Supplier<Block> baseBlock, Properties settings) {
        super(FORGE_BLOCK_SUPPLIER == null ? Preconditions.checkNotNull(baseBlock.get(), "Stairs block was given a null base block!")
                .defaultBlockState() : Blocks.AIR.defaultBlockState(), settings);
        if (FORGE_BLOCK_SUPPLIER != null) {
            FORGE_BLOCK_SUPPLIER.setAccessible(true);
            try {
                FORGE_BLOCK_SUPPLIER.set(this, (Supplier<BlockState>) () -> baseBlock.get().defaultBlockState());
            } catch (Exception ignored) {
            }
        }
        this.baseBlock = baseBlock;
    }

    @Override
    public MapCodec<? extends ModStairBlock> codec() {
        return CODEC;
    }

    public Block getBaseBlock() {
        return baseBlock.get();
    }
}
