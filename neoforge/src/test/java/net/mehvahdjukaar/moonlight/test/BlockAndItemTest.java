package net.mehvahdjukaar.moonlight.test;

import com.mojang.serialization.Codec;
import net.mehvahdjukaar.moonlight.api.misc.BlockAndItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class BlockAndItemTest extends CodecTest<BlockAndItem> {

    @Override
    Codec<BlockAndItem> codec() {
        return BlockAndItem.CODEC;
    }

    @ParameterizedTest
    @MethodSource("provideEncodingInputs")
    public void testEncoding(String snapshot, BlockAndItem input) {
        assertMatchesSnapshot(input, snapshot);
    }

    @ParameterizedTest
    @MethodSource("provideDecodingInputs")
    public void testDecoding(String snapshot, BlockAndItem expected) {
        var actual = decodeSnapshot(snapshot);
        Assertions.assertEquals(expected, actual);
    }

    public static Stream<Arguments> provideEncodingInputs() {
        return Stream.of(
                // This is working
                Arguments.of("object_both", new BlockAndItem(Blocks.AMETHYST_BLOCK, Items.BUNDLE)),
                // These two tests are failing
                Arguments.of("object_item_only", new BlockAndItem(null, Items.BUNDLE)),
                Arguments.of("object_block_only", new BlockAndItem(Blocks.OBSIDIAN, null))
        );
    }

    public static Stream<Arguments> provideDecodingInputs() {
        return Stream.of(
                // This is working
                Arguments.of("object_both", new BlockAndItem(Blocks.AMETHYST_BLOCK, Items.BUNDLE)),
                Arguments.of("both", new BlockAndItem(Blocks.STONE, Items.STONE)),
                Arguments.of("block_only", new BlockAndItem(Blocks.LAVA, null)),
                Arguments.of("item_only", new BlockAndItem(null, Items.BUNDLE)),
                // These two tests are failing
                Arguments.of("object_item_only", new BlockAndItem(null, Items.BUNDLE)),
                Arguments.of("object_block_only", new BlockAndItem(Blocks.OBSIDIAN, null))
        );
    }

}
