package net.mehvahdjukaar.selene.util;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.PrimitiveCodec;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;

import java.util.Random;


public class Utils {

    public static void swapItem(Player player, InteractionHand hand, ItemStack oldItem, ItemStack newItem, boolean bothsides) {
        if (!player.level.isClientSide || bothsides)
            player.setItemInHand(hand, ItemUtils.createFilledResult(oldItem.copy(), player, newItem, player.isCreative()));
    }

    public static void swapItem(Player player, InteractionHand hand, ItemStack oldItem, ItemStack newItem) {
        if (!player.level.isClientSide)
            player.setItemInHand(hand, ItemUtils.createFilledResult(oldItem.copy(), player, newItem, player.isCreative()));
    }

    public static void swapItemNBT(Player player, InteractionHand hand, ItemStack oldItem, ItemStack newItem) {
        if (!player.level.isClientSide)
            player.setItemInHand(hand, ItemUtils.createFilledResult(oldItem.copy(), player, newItem, false));
    }

    public static void swapItem(Player player, InteractionHand hand, ItemStack newItem) {
        if (!player.level.isClientSide)
            player.setItemInHand(hand, ItemUtils.createFilledResult(player.getItemInHand(hand).copy(), player, newItem, player.isCreative()));
    }

    public static void addStackToExisting(Player player, ItemStack stack){
        var inv = player.getInventory();
        boolean added = false;
        for(int j = 0; j<inv.items.size(); j++){
            if(inv.getItem(j).is(stack.getItem()) && inv.add(j,stack)){
                added = true;
                break;
            }
        }
        if(!added && inv.add(stack)){
            player.drop(stack, false);
        }
    }

    //xp bottle logic
    public static int getXPinaBottle(int bottleCount, Random rand) {
        int xp = 0;
        for (int i = 0; i < bottleCount; i++) xp += (3 + rand.nextInt(5) + rand.nextInt(5));
        return xp;
    }

    public static final PrimitiveCodec<Integer> HEX_CODEC = new PrimitiveCodec<>() {
        @Override
        public <T> DataResult<Integer> read(final DynamicOps<T> ops, final T input) {
            return ops.getStringValue(input)
                    .map(s -> {
                                if (s.contains("0x") || s.contains("#")) {
                                    return Integer.parseUnsignedInt(
                                            s.replace("0x", "").replace("#", ""), 16);
                                }
                                return Integer.parseUnsignedInt(s, 10);
                            }
                    ).map(Number::intValue);
        }

        @Override
        public <T> T write(final DynamicOps<T> ops, final Integer value) {
            String hex = Integer.toHexString(value);
            return ops.createString("#" + hex);
        }

        @Override
        public String toString() {
            return "Int";
        }
    };


}