package net.mehvahdjukaar.selene.util;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.PrimitiveCodec;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;


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

    public static VoxelShape rotateVoxelShape(VoxelShape source, Direction direction) {
        AtomicReference<VoxelShape> newShape = new AtomicReference<>(Shapes.empty());
        source.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ)-> {
            Vec3 min = new Vec3(minX - 0.5, minY - 0.5, minZ - 0.5);
            Vec3 max = new Vec3(maxX - 0.5, maxY - 0.5, maxZ - 0.5);
            Vec3 v1 = rotateVec3(min, direction);
            Vec3 v2 = rotateVec3(max, direction);
            VoxelShape s = Shapes.create(0.5 + Math.min(v1.x, v2.x), 0.5 + Math.min(v1.y, v2.y), 0.5 + Math.min(v1.z, v2.z),
                    0.5 + Math.max(v1.x, v2.x), 0.5 + Math.max(v1.y, v2.y), 0.5 + Math.max(v1.z, v2.z));
            newShape.set(Shapes.or(newShape.get(), s));
        });
        return newShape.get();
    }

    public static Vec3 rotateVec3(Vec3 vec, Direction dir) {
        double cos = 1;
        double sin = 0;
        switch (dir) {
            case SOUTH -> {
                cos = -1;
                sin = 0;
            }
            case WEST -> {
                cos = 0;
                sin = 1;
            }
            case EAST -> {
                cos = 0;
                sin = -1;
            }
        }
        double d0 = vec.x * cos + vec.z * sin;
        double d1 = vec.y;
        double d2 = vec.z * cos - vec.x * sin;
        return new Vec3(d0, d1, d2);
    }


}