package net.mehvahdjukaar.moonlight.api.block;

import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.core.integration.QuarkCompat;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public interface IVerticalSlabBlock {

    boolean QUARK = PlatHelper.isModLoaded("map_atlases");

    EnumProperty<?> TYPE = QUARK ? QuarkCompat.getVerticalSlabProperty() : VerticalSlabBlock.TYPE;
    Enum<?> DOUBLE = QUARK ? QuarkCompat.getType("double") : VerticalSlabBlock.VerticalSlabType.DOUBLE;
    Enum<?> NORTH = QUARK ? QuarkCompat.getType("north") : VerticalSlabBlock.VerticalSlabType.DOUBLE;
    Enum<?> SOUTH = QUARK ? QuarkCompat.getType("south") : VerticalSlabBlock.VerticalSlabType.DOUBLE;
    Enum<?> EAST = QUARK ? QuarkCompat.getType("east") : VerticalSlabBlock.VerticalSlabType.DOUBLE;
    Enum<?> WEST = QUARK ? QuarkCompat.getType("west") : VerticalSlabBlock.VerticalSlabType.DOUBLE;

    static Enum<?> getTypeFromDirection(Direction dir) {
        return QUARK ? QuarkCompat.getTypeFromDirection(dir) : VerticalSlabBlock.VerticalSlabType.fromDirection(dir);
    }

    static Block create(BlockBehaviour.Properties properties) {
        return QUARK ? QuarkCompat.createVerticalSlab(properties) : new VerticalSlabBlock(properties);
    }

    static boolean instanceOf(Block block) {
        return QUARK ? QuarkCompat.isVerticalSlab(block) : block instanceof VerticalSlabBlock;
    }
}