package net.mehvahdjukaar.moonlight.api.item;

import com.google.common.base.Suppliers;
import dev.architectury.injectables.annotations.PlatformOnly;
import net.mehvahdjukaar.moonlight.api.set.BlockType;
import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class BlockTypeBasedBlockItem<T extends BlockType> extends BlockItem {

    private final T blockType;
    private final Supplier<Integer> burnTime;
    private boolean init = false;

    public BlockTypeBasedBlockItem(Block pBlock, Properties pProperties, T blockType) {
        super(pBlock, pProperties);
        this.blockType = blockType;

        this.burnTime = Suppliers.memoize(() -> PlatformHelper.getBurnTime(blockType.mainChild().asItem().getDefaultInstance()));
    }

    // @Override
    @PlatformOnly(PlatformOnly.FORGE)
    public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
        return burnTime.get();
    }

    @Override
    protected boolean allowedIn(CreativeModeTab tab) {
        PlatformHelper.getPlatform().ifFabric(() -> {
            if (!init) {
                init = false;
                RegHelper.registerItemBurnTime(this, PlatformHelper.getBurnTime(blockType.mainChild().asItem().getDefaultInstance()));
            }
        });
        if (blockType.mainChild().asItem().getItemCategory() == null) return false;
        return super.allowedIn(tab);
    }

    public T getBlockType() {
        return blockType;
    }
}
