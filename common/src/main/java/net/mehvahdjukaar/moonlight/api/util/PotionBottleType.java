package net.mehvahdjukaar.moonlight.api.util;

import com.mojang.serialization.Codec;
import net.mehvahdjukaar.moonlight.api.MoonlightRegistry;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.*;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.function.Supplier;

public enum PotionBottleType implements StringRepresentable {
    REGULAR,
    SPLASH,
    LINGERING;

    public static final Codec<PotionBottleType> CODEC = StringRepresentable.fromValues(PotionBottleType::values);



    public ItemStack getDefaultItem() {
        return (switch (this) {
            case REGULAR -> Items.POTION;
            case LINGERING -> Items.LINGERING_POTION;
            case SPLASH -> Items.SPLASH_POTION;
        }).getDefaultInstance();
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    @Nullable
    public static PotionBottleType get(Item potionItem) {
        if (potionItem instanceof SplashPotionItem) return SPLASH;
        else if (potionItem instanceof LingeringPotionItem) return LINGERING;
        else if (potionItem instanceof PotionItem) return REGULAR;
        return null;
    }

    public static PotionBottleType getOrDefault(Item filledContainer) {
        PotionBottleType type = get(filledContainer);
        return type != null ? type : REGULAR;
    }

    public static PotionBottleType get(SoftFluidStack stack){
        return stack.getOrDefault(MoonlightRegistry.BOTTLE_TYPE.get(), REGULAR);
    }

}
