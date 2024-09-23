package net.mehvahdjukaar.moonlight.api.util;

import com.mojang.serialization.Codec;
import net.mehvahdjukaar.moonlight.api.MoonlightRegistry;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.*;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public enum PotionBottleType implements StringRepresentable {
    REGULAR("item.minecraft.potion"),
    SPLASH("item.minecraft.splash_potion"),
    LINGERING("item.minecraft.lingering_potion");

    private final String name;
    private final Component translatedName;

    PotionBottleType(String translatedKey) {
        this.name = this.name().toLowerCase(Locale.ROOT);
        this.translatedName = Component.translatable(translatedKey);
    }

    public static final Codec<PotionBottleType> CODEC = StringRepresentable.fromValues(PotionBottleType::values);

    public ItemStack getDefaultItem() {
        return (switch (this) {
            case REGULAR -> Items.POTION;
            case LINGERING -> Items.LINGERING_POTION;
            case SPLASH -> Items.SPLASH_POTION;
        }).getDefaultInstance();
    }

    public Component getTranslatedName() {
        return translatedName;
    }

    @Override
    public String getSerializedName() {
        return name;
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

    public static PotionBottleType getOrDefault(SoftFluidStack stack) {
        return stack.getOrDefault(MoonlightRegistry.BOTTLE_TYPE.get(), REGULAR);
    }

}
