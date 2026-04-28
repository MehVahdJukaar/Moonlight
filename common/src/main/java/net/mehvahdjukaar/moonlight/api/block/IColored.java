package net.mehvahdjukaar.moonlight.api.block;

import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.Nullable;

public interface IColored {

    /**
     * @return Gets the color of this block or item
     */
    @Nullable
    DyeColor getColor();

    /**
     * If this kind of block can have a null color, similar to shulker boxes
     */
    default boolean supportsBlankColor() {
        return false;
    }

}
