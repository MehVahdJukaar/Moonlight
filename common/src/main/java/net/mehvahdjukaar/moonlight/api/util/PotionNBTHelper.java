package net.mehvahdjukaar.moonlight.api.util;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.*;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static net.minecraft.world.item.alchemy.PotionUtils.getAllEffects;
import static net.minecraft.world.item.alchemy.PotionUtils.getColor;

public class PotionNBTHelper {
    private static final MutableComponent EMPTY = (Component.translatable("effect.none")).withStyle(ChatFormatting.GRAY);

    //I need this because I'm using block entity tag, so I can't give PotionUtil methods an itemStack directly
    public static void addPotionTooltip(@Nullable CompoundTag com, List<Component> tooltip, float durationFactor) {
        List<MobEffectInstance> list = getAllEffects(com);
        List<Pair<Attribute, AttributeModifier>> list1 = Lists.newArrayList();
        if (list.isEmpty()) {
            tooltip.add(EMPTY);
        } else {
            for (MobEffectInstance effectInstance : list) {
                MutableComponent translatable = Component.translatable(effectInstance.getDescriptionId());
                MobEffect effect = effectInstance.getEffect();
                Map<Attribute, AttributeModifier> map = effect.getAttributeModifiers();
                if (!map.isEmpty()) {
                    for (Map.Entry<Attribute, AttributeModifier> entry : map.entrySet()) {
                        AttributeModifier attributemodifier = entry.getValue();
                        AttributeModifier modifier = new AttributeModifier(attributemodifier.getName(), effect.getAttributeModifierValue(effectInstance.getAmplifier(), attributemodifier), attributemodifier.getOperation());
                        list1.add(new Pair<>(entry.getKey(), modifier));
                    }
                }

                if (effectInstance.getAmplifier() > 0) {
                    translatable = Component.translatable("potion.withAmplifier", translatable, Component.translatable("potion.potency." + effectInstance.getAmplifier()));
                }

                if (effectInstance.getDuration() > 20) {
                    translatable = Component.translatable("potion.withDuration", translatable, MobEffectUtil.formatDuration(effectInstance, durationFactor));
                }

                tooltip.add(translatable.withStyle(effect.getCategory().getTooltipFormatting()));
            }
        }

        if (!list1.isEmpty()) {
            tooltip.add(CommonComponents.EMPTY);
            tooltip.add(Component.translatable("potion.whenDrank").withStyle(ChatFormatting.DARK_PURPLE));

            for (Pair<Attribute, AttributeModifier> pair : list1) {
                AttributeModifier modifier = pair.getSecond();
                double d0 = modifier.getAmount();
                double d1;
                if (modifier.getOperation() != AttributeModifier.Operation.MULTIPLY_BASE && modifier.getOperation() != AttributeModifier.Operation.MULTIPLY_TOTAL) {
                    d1 = modifier.getAmount();
                } else {
                    d1 = modifier.getAmount() * 100.0D;
                }

                if (d0 > 0.0D) {
                    tooltip.add(Component.translatable("attribute.modifier.plus." + modifier.getOperation().toValue(), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(d1), Component.translatable(pair.getFirst().getDescriptionId())).withStyle(ChatFormatting.BLUE));
                } else if (d0 < 0.0D) {
                    d1 = d1 * -1.0D;
                    tooltip.add(Component.translatable("attribute.modifier.take." + modifier.getOperation().toValue(), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(d1), Component.translatable(pair.getFirst().getDescriptionId())).withStyle(ChatFormatting.RED));
                }
            }
        }

    }

    public static int getColorFromNBT(@Nullable CompoundTag com) {
        if (com != null && com.contains("CustomPotionColor", 99)) {
            return com.getInt("CustomPotionColor");
        } else {
            return getColor(getAllEffects(com));
        }
    }

    public enum Type {
        REGULAR,
        SPLASH,
        LINGERING;

        static final Map<String, Type> BY_NAME = Arrays.stream(Type.values()).collect(Collectors.toMap(
                Enum::name, i -> i));

        public ItemStack getDefaultItem() {
            return (switch (this) {
                case REGULAR -> Items.POTION;
                case LINGERING -> Items.LINGERING_POTION;
                case SPLASH -> Items.SPLASH_POTION;
            }).getDefaultInstance();
        }

        public void applyToTag(CompoundTag tag){
            tag.putString(POTION_TYPE_KEY, this.name());
        }
    }

    public static final String POTION_TYPE_KEY = "Bottle";

    @Nullable
    public static PotionNBTHelper.Type getPotionType(CompoundTag tag) {
        if (!tag.contains(POTION_TYPE_KEY)) return null;
        String type = tag.getString(POTION_TYPE_KEY);
        return Type.BY_NAME.get(type);
    }

    @Nullable
    public static PotionNBTHelper.Type getPotionType(Item potionItem) {
        if (potionItem instanceof SplashPotionItem) return Type.SPLASH;
        else if (potionItem instanceof LingeringPotionItem) return Type.LINGERING;
        else if (potionItem instanceof PotionItem) return Type.REGULAR;
        return null;
    }

}
