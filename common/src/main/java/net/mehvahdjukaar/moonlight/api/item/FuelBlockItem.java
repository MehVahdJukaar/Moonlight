package net.mehvahdjukaar.moonlight.api.item;

import dev.architectury.injectables.annotations.PlatformOnly;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class FuelBlockItem extends BlockItem {
    private final Supplier<Integer> burnTime;

    public FuelBlockItem(Block pBlock, Properties pProperties, Supplier<Integer> burnTime) {
        super(pBlock, pProperties);
        this.burnTime = burnTime;
        PlatHelper.getPlatform().ifFabric(() -> {
            int b = burnTime.get();
            //this won't work for non-vanilla base items... too bad
            if (b != 0) RegHelper.registerItemBurnTime(this, b);
        });
    }

    @PlatformOnly(PlatformOnly.FORGE)
    public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
        return burnTime.get();
    }
}