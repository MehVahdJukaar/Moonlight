package net.mehvahdjukaar.moonlight.api.item;

import com.google.common.base.Suppliers;
import dev.architectury.injectables.annotations.PlatformOnly;
import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.mehvahdjukaar.moonlight.api.set.BlockType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class BlockTypeBasedItem<T extends BlockType> extends FuelItem {

    private final T blockType;

    public BlockTypeBasedItem(Properties pProperties, T blockType) {
        this(pProperties, blockType, Suppliers.memoize(() -> PlatformHelper.getBurnTime(blockType.mainChild().asItem().getDefaultInstance())));
    }

    public BlockTypeBasedItem(Properties pProperties, T blockType, Supplier<Integer> burnTime) {
        super(pProperties, burnTime);
        this.blockType = blockType;
    }

    @Override
    protected boolean allowedIn(CreativeModeTab tab) {
        if (blockType.mainChild().asItem().getItemCategory() == null) return false;
        return super.allowedIn(tab);
    }

    public T getBlockType() {
        return blockType;
    }
}
