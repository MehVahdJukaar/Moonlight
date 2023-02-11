package net.mehvahdjukaar.moonlight.api.item;

import dev.architectury.injectables.annotations.PlatformOnly;
import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class FuelItem extends Item {
    private final Supplier<Integer> burnTime;

    public FuelItem(Properties pProperties, Supplier<Integer> burnTime) {
        super(pProperties);
        this.burnTime = burnTime;
        if (PlatformHelper.getPlatform().isFabric()) {
            int b = burnTime.get();
            //this won't work for non-vanilla based items... too bad
            if (b != 0) RegHelper.registerItemBurnTime(this, b);
        }
    }

    @PlatformOnly(PlatformOnly.FORGE)
    public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
        return burnTime.get();
    }
}