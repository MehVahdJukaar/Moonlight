package net.mehvahdjukaar.moonlight.api.util;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.AttributeModifierTemplate;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;

import org.jetbrains.annotations.Nullable;
import java.util.List;
import java.util.Map;

import static net.minecraft.world.item.alchemy.PotionUtils.*;

public class PotionNBTHelper {
    private static final MutableComponent EMPTY = (Component.translatable("effect.none")).withStyle(ChatFormatting.GRAY);

    //I need this because I'm using block entity tag, so I can't give PotionUtil methods an itemStack directly
    public static void addPotionTooltip(@Nullable CompoundTag com, List<Component> tooltip, float durationFactor, float g) {
       PotionUtils.addPotionTooltip(getAllEffects(com), tooltip, durationFactor, g);
    }

    public static int getColorFromNBT(@Nullable CompoundTag com) {
        if (com != null && com.contains("CustomPotionColor", 99)) {
            return com.getInt("CustomPotionColor");
        } else {
            return getColor(getAllEffects(com));
        }
    }
}
