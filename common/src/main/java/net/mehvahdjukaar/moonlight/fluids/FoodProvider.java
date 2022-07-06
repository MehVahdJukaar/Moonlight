package net.mehvahdjukaar.moonlight.fluids;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.moonlight.platform.PlatformHelper;
import net.mehvahdjukaar.moonlight.util.Utils;
import net.minecraft.core.Registry;
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
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class FoodProvider {

    public static final Codec<FoodProvider> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
            Registry.ITEM.byNameCodec().fieldOf("item").forGetter(f -> f.food),
            Codec.INT.fieldOf("divider").forGetter(f -> f.divider)
    ).apply(instance, FoodProvider::create));

    public static final FoodProvider EMPTY = new FoodProvider(Items.AIR, 1);

    protected final Item food;
    protected final int divider;

    private FoodProvider(Item food, int divider) {
        this.food = food;
        this.divider = divider;
    }

    public Item getFood() {
        return food;
    }

    public int getDivider() {
        return divider;
    }

    public boolean isEmpty() {
        return this == EMPTY;
    }

    /**
     * Consumes some fluid and gives the player the appropriate effect
     *
     * @return if one unit has been consumed
     */
    public boolean consume(Player player, Level world, @Nullable Consumer<ItemStack> nbtApplier) {

        ItemStack stack = this.food.getDefaultInstance();
        if (nbtApplier != null) nbtApplier.accept(stack);

        //food

        FoodProperties foodProperties = PlatformHelper.getFoodProperties(this.food, stack, player);
        //single items are handled by items themselves
        if (this.divider == 1) {
            this.food.finishUsingItem(stack.copy(), world, player);
            if (foodProperties == null || stack.getItem().isEdible()) {
                player.playSound(this.food.getDrinkingSound(), 1, 1);
            }
            //player already plays sound
            return true;
        }
        if (foodProperties != null && player.canEat(false)) {

            player.getFoodData().eat(foodProperties.getNutrition() / this.divider, foodProperties.getSaturationModifier() / (float) this.divider);
            player.playSound(this.food.getDrinkingSound(), 1, 1);
            return true;
        }
        return false;
    }

    public static FoodProvider create(Item item, int divider) {
        return CUSTOM_PROVIDERS.getOrDefault(item, new FoodProvider(item, divider));
    }

    private static final FoodProvider XP = new FoodProvider(Items.EXPERIENCE_BOTTLE, 1) {

        @Override
        public boolean consume(Player player, Level world, @Nullable Consumer<ItemStack> nbtApplier) {
            player.giveExperiencePoints(Utils.getXPinaBottle(1, world.random));
            player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1, 1);
            return true;
        }
    };

    private static final FoodProvider MILK = new FoodProvider(Items.MILK_BUCKET, 3) {

        @Override
        public boolean consume(Player player, Level world, @Nullable Consumer<ItemStack> nbtApplier) {
            ItemStack stack = this.food.getDefaultInstance();
            if (nbtApplier != null) nbtApplier.accept(stack);
            for (MobEffectInstance effect : player.getActiveEffectsMap().values()) {
                if ( PlatformHelper.isCurativeItem(stack,effect)) {
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

    public static final Map<Item, FoodProvider> CUSTOM_PROVIDERS = new HashMap<>() {{
        put(Items.AIR, EMPTY);
        put(Items.SUSPICIOUS_STEW, SUS_STEW);
        put(Items.MILK_BUCKET, MILK);
        put(Items.EXPERIENCE_BOTTLE, XP);
    }};
}
