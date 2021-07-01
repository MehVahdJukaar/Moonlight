package net.mehvahdjukaar.selene.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.util.*;
import java.util.Random;


public class Utils {

    public static void swapItem(PlayerEntity player, Hand hand, ItemStack oldItem, ItemStack newItem, boolean bothsides){
        if(!player.level.isClientSide || bothsides)
            player.setItemInHand(hand, DrinkHelper.createFilledResult(oldItem.copy(), player, newItem, player.isCreative()));
    }

    public static void swapItem(PlayerEntity player, Hand hand, ItemStack oldItem, ItemStack newItem){
        if(!player.level.isClientSide)
        player.setItemInHand(hand, DrinkHelper.createFilledResult(oldItem.copy(), player, newItem, player.isCreative()));
    }
    public static void swapItemNBT(PlayerEntity player, Hand hand, ItemStack oldItem, ItemStack newItem){
        if(!player.level.isClientSide)
            player.setItemInHand(hand, DrinkHelper.createFilledResult(oldItem.copy(), player, newItem,false));
    }
    public static void swapItem(PlayerEntity player, Hand hand, ItemStack newItem){
        if(!player.level.isClientSide)
        player.setItemInHand(hand, DrinkHelper.createFilledResult(player.getItemInHand(hand).copy(), player, newItem, player.isCreative()));
    }

    public static int bottleToXP(int bottleCount, Random rand){
        int xp = 0;
        for(int i = 0; i<bottleCount; i++) xp += (3 + rand.nextInt(5) + rand.nextInt(5));
        return xp;
    }

}