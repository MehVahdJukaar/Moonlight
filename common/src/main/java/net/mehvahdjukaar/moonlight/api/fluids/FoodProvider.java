package net.mehvahdjukaar.moonlight.api.fluids;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;
import java.util.function.Consumer;

public class FoodProvider {

    public static final Codec<FoodProvider> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
            Registry.ITEM.byNameCodec().fieldOf("item").forGetter(f -> f.food),
            Codec.INT.fieldOf("divider").forGetter(f -> f.divider)
    ).apply(instance, FoodProvider::create));


    protected final Item food;
    protected final int divider;

    FoodProvider(Item food, int divider) {
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
        return this == FoodProvider.EMPTY;
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
        return FoodProviders.CUSTOM_PROVIDERS.getOrDefault(item, new FoodProvider(item, divider));
    }


    public static final FoodProvider EMPTY = new FoodProvider(null, 1) {

        @Override
        public Item getFood() {
            return Items.AIR;
        }
    };


}
