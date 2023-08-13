package net.mehvahdjukaar.moonlight.api.util;

import io.netty.util.internal.UnstableApi;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.moonlight.api.map.MapDecorationRegistry;
import net.mehvahdjukaar.moonlight.api.map.type.MapDecorationType;
import net.mehvahdjukaar.moonlight.api.platform.ForgeHelper;
import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.mehvahdjukaar.moonlight.api.util.math.MthUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;


public class Utils {

    public static void swapItem(Player player, InteractionHand hand, ItemStack oldItem, ItemStack newItem, boolean bothSides) {
        if (!player.level.isClientSide || bothSides)
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

    public static void addStackToExisting(Player player, ItemStack stack) {
        var inv = player.getInventory();
        boolean added = false;
        for (int j = 0; j < inv.items.size(); j++) {
            if (inv.getItem(j).is(stack.getItem()) && inv.add(j, stack)) {
                added = true;
                break;
            }
        }
        if (!added && inv.add(stack)) {
            player.drop(stack, false);
        }
    }

    //xp bottle logic
    public static int getXPinaBottle(int bottleCount, RandomSource rand) {
        int xp = 0;
        for (int i = 0; i < bottleCount; i++) xp += (3 + rand.nextInt(5) + rand.nextInt(5));
        return xp;
    }

    public static VoxelShape rotateVoxelShape(VoxelShape source, Direction direction) {
        AtomicReference<VoxelShape> newShape = new AtomicReference<>(Shapes.empty());
        source.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
            Vec3 min = new Vec3(minX - 0.5, minY - 0.5, minZ - 0.5);
            Vec3 max = new Vec3(maxX - 0.5, maxY - 0.5, maxZ - 0.5);
            Vec3 v1 = MthUtils.rotateVec3(min, direction);
            Vec3 v2 = MthUtils.rotateVec3(max, direction);
            VoxelShape s = Shapes.create(0.5 + Math.min(v1.x, v2.x), 0.5 + Math.min(v1.y, v2.y), 0.5 + Math.min(v1.z, v2.z),
                    0.5 + Math.max(v1.x, v2.x), 0.5 + Math.max(v1.y, v2.y), 0.5 + Math.max(v1.z, v2.z));
            newShape.set(Shapes.or(newShape.get(), s));
        });
        return newShape.get();
    }

    public static ResourceLocation getID(Block object) {
        return Registry.BLOCK.getKey(object);
    }

    public static ResourceLocation getID(EntityType<?> object) {
        return Registry.ENTITY_TYPE.getKey(object);
    }

    //TODO: not sure if this is correct
    public static ResourceLocation getID(Biome object) {
        return BuiltinRegistries.BIOME.getKey(object);
    }

    public static ResourceLocation getID(ConfiguredFeature<?, ?> object) {
        return BuiltinRegistries.CONFIGURED_FEATURE.getKey(object);
    }

    public static ResourceLocation getID(Item object) {
        return Registry.ITEM.getKey(object);
    }

    public static ResourceLocation getID(Fluid object) {
        return Registry.FLUID.getKey(object);
    }

    public static ResourceLocation getID(BlockEntityType<?> object) {
        return Registry.BLOCK_ENTITY_TYPE.getKey(object);
    }

    public static ResourceLocation getID(RecipeSerializer<?> object) {
        return Registry.RECIPE_SERIALIZER.getKey(object);
    }

    public static ResourceLocation getID(SoftFluid object) {
        return SoftFluidRegistry.getID(object);
    }

    public static ResourceLocation getID(MapDecorationType<?, ?> object) {
        return MapDecorationRegistry.getID(object);
    }

    @UnstableApi
    public static ResourceLocation getID(Object object) {
        if (object instanceof Block b) return getID(b);
        if (object instanceof Item b) return getID(b);
        if (object instanceof EntityType<?> b) return getID(b);
        if (object instanceof Biome b) return getID(b);
        if (object instanceof Fluid b) return getID(b);
        if (object instanceof BlockEntityType<?> b) return getID(b);
        if (object instanceof RecipeSerializer<?> b) return getID(b);
        if (object instanceof ConfiguredFeature<?, ?> c) return getID(c);
        if (object instanceof Supplier<?> s) return getID(s.get());
        if (object instanceof SoftFluid s) return getID(s);
        if (object instanceof MapDecorationType<?, ?> s) return getID(s);
        throw new UnsupportedOperationException("Unknown class type " + object.getClass());
    }

    //very hacky
    public static RegistryAccess hackyGetRegistryAccess() {
        var s = PlatformHelper.getCurrentServer();
        if (s != null) return s.registryAccess();
        if (!PlatformHelper.getEnv().isServer()) {
            var level = Minecraft.getInstance().level;
            if (level != null) return level.registryAccess();
            throw new UnsupportedOperationException("Failed to get registry access: Minecraft level was null");
        }
        throw new UnsupportedOperationException("Failed to get registry access. This is a bug");
    }

    /**
     * Copies block properties without keeping stupid lambdas that could include references to the wrong blockstate properties
     */
    public static BlockBehaviour.Properties copyPropertySafe(BlockBehaviour blockBehaviour) {
        var p = BlockBehaviour.Properties.copy(blockBehaviour);
        p.lightLevel(s -> 0);
        p.offsetType(BlockBehaviour.OffsetType.NONE);
        p.color(blockBehaviour.defaultMaterialColor());
        return p;
    }

    public static HitResult rayTrace(LivingEntity entity, Level world, ClipContext.Block blockMode, ClipContext.Fluid fluidMode) {
        return rayTrace(entity, world, blockMode, fluidMode, ForgeHelper.getReachDistance(entity));
    }

    public static HitResult rayTrace(Entity entity, Level world, ClipContext.Block blockMode, ClipContext.Fluid fluidMode, double range) {
        Vec3 startPos = entity.getEyePosition();
        Vec3 ray = entity.getViewVector(1).scale(range);
        Vec3 endPos = startPos.add(ray);
        ClipContext context = new ClipContext(startPos, endPos, blockMode, fluidMode, entity);
        return world.clip(context);
    }

    @Nullable
    public static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> getTicker(BlockEntityType<A> type, BlockEntityType<E> targetType, BlockEntityTicker<? super E> ticker) {
        return targetType == type ? (BlockEntityTicker<A>) ticker : null;
    }

    /**
     * Call this instead of player.abilities.mayBuild. Mainly used for adventure mode ege case
     * This is needed as vanilla handles most of its block altering actions from the item class which calls this.
     * In a Block class this should be called instead to allow adventure mode to work properly
     */
    public static boolean mayBuild(Player player, BlockPos pos) {
        if (player.getAbilities().mayBuild) return true; //Exit early
        if (player instanceof ServerPlayer sp) {
            return !player.blockActionRestricted(player.level, pos, sp.gameMode.getGameModeForPlayer());
        } else {
            return !player.blockActionRestricted(player.level, pos, Minecraft.getInstance().gameMode.getPlayerMode());
        }
    }
}