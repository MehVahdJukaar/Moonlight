package net.mehvahdjukaar.moonlight.api.block;

import net.mehvahdjukaar.moonlight.api.set.BlocksColorAPI;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

//TODO: add color change api
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
