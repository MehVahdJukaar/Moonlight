package net.mehvahdjukaar.moonlight.core.integration;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public class QuarkCompat {
    @ExpectPlatform
    public static Block createVerticalSlab(BlockBehaviour.Properties properties) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static EnumProperty<?> getVerticalSlabProperty() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static Enum<?> getType(String aDouble) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static Enum<?> getTypeFromDirection(Direction dir) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean isVerticalSlab(Block block) {
        throw new AssertionError();
    }
}
