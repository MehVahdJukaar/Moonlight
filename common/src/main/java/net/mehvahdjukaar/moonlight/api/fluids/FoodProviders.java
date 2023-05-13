package net.mehvahdjukaar.moonlight.api.fluids;

import net.mehvahdjukaar.moonlight.api.platform.ForgeHelper;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Consumer;

public class FoodProviders {
    private static final FoodProvider MILK = new FoodProvider(Items.MILK_BUCKET, 3) {

        @Override
        public boolean consume(Player player, Level world, @Nullable Consumer<ItemStack> nbtApplier) {
            ItemStack stack = this.food.getDefaultInstance();
            if (nbtApplier != null) nbtApplier.accept(stack);
            for (MobEffectInstance effect : player.getActiveEffectsMap().values()) {
                if (ForgeHelper.isCurativeItem(stack, effect)) {
                    player.removeEffect(effect.getEffect());
                    break;
                }
            }
            player.playSound(this.food.getDrinkingSound(), 1, 1);
            return true;
        }
    };

    private static final FoodProvider SUS_STEW = new FoodProvider(Items.SUSPICIOUS_STEW, 2) {

        @Override
        public boolean consume(Player player, Level world, @Nullable Consumer<ItemStack> nbtApplier) {

            ItemStack stack = this.food.getDefaultInstance();
            if (nbtApplier != null) nbtApplier.accept(stack);
            FoodProperties foodProperties = this.food.getFoodProperties();
            if (foodProperties != null && player.canEat(false)) {

                CompoundTag tag = stack.getTag();
                if (tag != null && tag.contains("Effects", 9)) {
                    ListTag effects = tag.getList("Effects", 10);
                    for (int i = 0; i < effects.size(); ++i) {
                        int j = 160;
                        CompoundTag effectsCompound = effects.getCompound(i);
                        if (effectsCompound.contains("EffectDuration", 3))
                            j = effectsCompound.getInt("EffectDuration") / this.divider;
                        MobEffect effect = MobEffect.byId(effectsCompound.getByte("EffectId"));
                        if (effect != null) {
                            player.addEffect(new MobEffectInstance(effect, j));
                        }
                    }
                }
                player.getFoodData().eat(foodProperties.getNutrition() / this.divider, foodProperties.getSaturationModifier() / (float) this.divider);
                player.playSound(this.food.getDrinkingSound(), 1, 1);
                return true;
            }
            return false;
        }
    };

    private static final FoodProvider XP = new FoodProvider(Items.EXPERIENCE_BOTTLE, 1) {

        @Override
        public boolean consume(Player player, Level world, @Nullable Consumer<ItemStack> nbtApplier) {
            player.giveExperiencePoints(Utils.getXPinaBottle(1, world.random));
            player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1, 1);
            return true;
        }
    };

    static final Map<Item, FoodProvider> CUSTOM_PROVIDERS = Util.make(() -> {
        var map = new IdentityHashMap<Item, FoodProvider>();

        map.put(Items.AIR, FoodProvider.EMPTY);
        map.put(Items.SUSPICIOUS_STEW, SUS_STEW);
        map.put(Items.MILK_BUCKET, MILK);
        map.put(Items.EXPERIENCE_BOTTLE, XP);
        return map;
    });
}
