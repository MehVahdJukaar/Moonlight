package net.mehvahdjukaar.moonlight.api.block;

import net.mehvahdjukaar.moonlight.api.set.BlocksColorAPI;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

import org.jetbrains.annotations.Nullable;
import java.util.Optional;

public interface IColored {

    /**
     * @return Gets the color of this block or item
     */
    @Nullable
    DyeColor getColor();

    /**
     * @return the associated item to this block or item with the given color. Null if it fails
     */
    @Deprecated
    @Nullable
    default Item changeItemColor(@Nullable DyeColor color){
      return BlocksColorAPI.changeColor(((ItemLike)this).asItem(), color);
    }

    /**
     * If this kind of block can have a null color, similar to shulker boxes
     */
    default boolean supportsBlankColor() {
        return false;
    }

    /**
     * casts the given object to this interface if it or its block are colored
     */
    static Optional<IColored> getOptional(ItemLike itemLike) {
        if (itemLike instanceof IColored col) return Optional.of(col);
        if (itemLike instanceof BlockItem bi && bi.getBlock() instanceof IColored col) return Optional.of(col);
        return Optional.empty();
    }
}
