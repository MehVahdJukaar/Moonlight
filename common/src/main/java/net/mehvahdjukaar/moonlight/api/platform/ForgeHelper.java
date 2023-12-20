package net.mehvahdjukaar.moonlight.api.platform;

import com.mojang.serialization.DynamicOps;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Helper class dedicated to platform forge specific methods. Usually fabric methods here just call vanilla stuff while forge have extra logic usually calling events
 */
public class ForgeHelper {

    @Deprecated(forRemoval = true)
    @ExpectPlatform
    public static RecipeOutput addRecipeConditions(RecipeOutput originalRecipe, List<Object> conditions) {
        throw new AssertionError();
    }

    public static <T> DynamicOps<T> addConditionOps(DynamicOps<T> ops) {
        return ops; //TODO: 1.20.4
    }

    @Contract
    @ExpectPlatform
    public static boolean onProjectileImpact(Projectile improvedProjectileEntity, HitResult blockHitResult) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean isCurativeItem(ItemStack stack, MobEffectInstance effect) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean canHarvestBlock(BlockState state, ServerLevel level, BlockPos pos, ServerPlayer player) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static float getFriction(BlockState state, LevelReader level, BlockPos pos, @Nullable Entity entity) {
        throw new AssertionError();
    }


    @Contract
    @ExpectPlatform
    public static boolean canEquipItem(LivingEntity entity, ItemStack stack, EquipmentSlot slot) {
        throw new AssertionError();
    }

    @Contract
    @ExpectPlatform
    public static boolean canEntityDestroy(Level level, BlockPos blockPos, Animal animal) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean onExplosionStart(Level level, Explosion explosion) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void onLivingConvert(LivingEntity frFom, LivingEntity to) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean canLivingConvert(LivingEntity entity, EntityType<? extends LivingEntity> outcome, Consumer<Integer> timer) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void onExplosionDetonate(Level level, Explosion explosion, List<Entity> entities, double diameter) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static double getReachDistance(LivingEntity entity) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static float getExplosionResistance(BlockState state, Level level, BlockPos pos, Explosion explosion) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void onBlockExploded(BlockState blockstate, Level level, BlockPos blockpos, Explosion explosion) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean isFireSource(BlockState blockState, Level level, BlockPos pos, Direction up) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean canDropFromExplosion(BlockState blockstate, Level level, BlockPos blockpos, Explosion explosion) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean isDye(ItemStack itemstack) {
        throw new AssertionError();
    }

    @org.jetbrains.annotations.Nullable
    @ExpectPlatform
    public static DyeColor getColor(ItemStack stack) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static BlockState rotateBlock(BlockState state, Level world, BlockPos targetPos, Rotation rot) {
        throw new AssertionError();
    }


    @Contract
    @ExpectPlatform
    public static boolean isMultipartEntity(Entity e) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void setPoolName(LootPool.Builder pool, String name) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static RailShape getRailDirection(BaseRailBlock railBlock, BlockState blockstate, Level level, BlockPos blockpos, @org.jetbrains.annotations.Nullable AbstractMinecart o) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static Optional<ItemStack> getCraftingRemainingItem(ItemStack itemstack) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void reviveEntity(Entity entity) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean onCropsGrowPre(ServerLevel level, BlockPos pos, BlockState state, boolean b) {
        throw new AssertionError();

    }

    @ExpectPlatform
    public static void onCropsGrowPost(ServerLevel level, BlockPos pos, BlockState state) {
    }

    @ExpectPlatform
    public static void onEquipmentChange(LivingEntity entity, EquipmentSlot slot, ItemStack from, ItemStack to) {
    }

    @ExpectPlatform
    @org.jetbrains.annotations.Nullable
    public static InteractionResult onRightClickBlock(Player player, InteractionHand hand, BlockPos below, BlockHitResult rayTraceResult) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean canItemStack(ItemStack i, ItemStack i1) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static int getLightEmission(BlockState state, Level level, BlockPos pos) {
        throw new ArrayStoreException();
    }

    @ExpectPlatform
    public static Map<Block, Item> getBlockItemMap() {
        throw new AssertionError();
    }

    @Contract
    @ExpectPlatform
    public static boolean isInFluidThatCanExtinguish(Entity entity) {
        throw new AssertionError();
    }


    @ExpectPlatform
    public static void registerDefaultContainerCap(BlockEntityType<? extends Container> container) {

    }
}
