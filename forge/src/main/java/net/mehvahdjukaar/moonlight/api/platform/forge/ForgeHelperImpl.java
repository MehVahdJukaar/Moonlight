package net.mehvahdjukaar.moonlight.api.platform.forge;

import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.crafting.ConditionalRecipe;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.registries.GameData;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class ForgeHelperImpl {

    public static boolean onProjectileImpact(Projectile projectile, HitResult blockHitResult) {
        return ForgeEventFactory.onProjectileImpact(projectile, blockHitResult);
    }

    public static FinishedRecipe addRecipeConditions(FinishedRecipe originalRecipe, List<Object> conditions) {
        boolean success = false;
        var builder = ConditionalRecipe.builder();
        for (var c : conditions) {
            if (c instanceof ICondition condition) {
                builder.addCondition(condition);
                success = true;
            }
        }
        if (success) {
            AtomicReference<FinishedRecipe> newRecipe = new AtomicReference<>();
            builder.addRecipe(originalRecipe);
            builder.build(r -> newRecipe.set(new Wrapper(r, originalRecipe)), originalRecipe.getId());
            return newRecipe.get();
        }
        return originalRecipe;
    }

    private record Wrapper(FinishedRecipe cond, FinishedRecipe original) implements FinishedRecipe {

        @Override
        public void serializeRecipeData(JsonObject json) {
            cond.serializeRecipeData(json);
        }

        @Override
        public ResourceLocation getId() {
            return cond.getId();
        }

        @Override
        public RecipeSerializer<?> getType() {
            return cond.getType();
        }

        @Nullable
        @Override
        public JsonObject serializeAdvancement() {
            return original.serializeAdvancement();
        }

        @Nullable
        @Override
        public ResourceLocation getAdvancementId() {
            return original.getAdvancementId();
        }
    }

    public static boolean isCurativeItem(ItemStack stack, MobEffectInstance effect) {
        return effect.isCurativeItem(stack);
    }


    public static boolean canHarvestBlock(BlockState state, ServerLevel level, BlockPos pos, ServerPlayer player) {
        return state.canHarvestBlock(level, pos, player);
    }

    public static float getFriction(BlockState state, LevelReader level, BlockPos pos, @Nullable Entity entity) {
        return state.getFriction(level, pos, entity);
    }


    public static boolean canEquipItem(LivingEntity entity, ItemStack stack, EquipmentSlot slot) {
        return stack.canEquip(slot, entity);
    }

    public static boolean canEntityDestroy(Level level, BlockPos blockPos, Animal animal) {
        return ForgeHooks.canEntityDestroy(level, blockPos, animal);
    }

    public static boolean onExplosionStart(Level level, Explosion explosion) {
        return ForgeEventFactory.onExplosionStart(level, explosion);
    }

    public static void onExplosionDetonate(Level level, Explosion explosion, List<Entity> entities, double diameter) {
        ForgeEventFactory.onExplosionDetonate(level, explosion, entities, diameter);
    }

    public static void onLivingConvert(LivingEntity skellyHorseMixin, LivingEntity newHorse) {
        ForgeEventFactory.onLivingConvert(newHorse, newHorse);
    }

    public static boolean canLivingConvert(LivingEntity entity, EntityType<? extends LivingEntity> outcome, Consumer<Integer> timer) {
        return ForgeEventFactory.canLivingConvert(entity, outcome, timer);
    }

    public static double getReachDistance(LivingEntity entity) {
        return entity.getAttribute(ForgeMod.BLOCK_REACH.get()).getValue();
    }

    public static float getExplosionResistance(BlockState state, Level level, BlockPos pos, Explosion explosion) {
        return state.getExplosionResistance(level, pos, explosion);
    }

    public static void onBlockExploded(BlockState blockstate, Level level, BlockPos blockpos, Explosion explosion) {
        blockstate.onBlockExploded(level, blockpos, explosion);
    }

    public static boolean areStacksEqual(ItemStack stack, ItemStack other, boolean sameNbt) {
        return stack.equals(other, sameNbt);
    }

    public static boolean canDropFromExplosion(BlockState blockstate, Level level, BlockPos blockpos, Explosion explosion) {
        return blockstate.canDropFromExplosion(level, blockpos, explosion);
    }

    public static boolean isDye(ItemStack itemstack) {
        return itemstack.is(Tags.Items.DYES);
    }

    public static DyeColor getColor(ItemStack stack) {
        return DyeColor.getColor(stack);
    }

    public static BlockState rotateBlock(BlockState state, Level world, BlockPos targetPos, Rotation rot) {
        return state.rotate(world, targetPos, rot);
    }

    public static boolean isMultipartEntity(Entity e) {
        return e.isMultipartEntity();
    }

    public static RailShape getRailDirection(BaseRailBlock railBlock, BlockState blockstate, Level level, BlockPos blockpos, AbstractMinecart o) {
        return railBlock.getRailDirection(blockstate, level, blockpos, o);
    }

    public static Optional<ItemStack> getCraftingRemainingItem(ItemStack itemstack) {
        return itemstack.hasCraftingRemainingItem() ? Optional.of(itemstack.getCraftingRemainingItem()) : Optional.empty();
    }

    public static void reviveEntity(Entity entity) {
        entity.revive();
    }


    public static boolean onCropsGrowPre(ServerLevel level, BlockPos pos, BlockState state, boolean b) {
        return ForgeHooks.onCropsGrowPre(level, pos, state, b);
    }

    public static void onCropsGrowPost(ServerLevel level, BlockPos pos, BlockState state) {
        ForgeHooks.onCropsGrowPost(level, pos, state);
    }

    public static void onEquipmentChange(LivingEntity entity, EquipmentSlot slot, ItemStack from, ItemStack to) {
        MinecraftForge.EVENT_BUS.post(new LivingEquipmentChangeEvent(entity, slot, from, to));
    }

    @Nullable
    public static InteractionResult onRightClickBlock(Player player, InteractionHand hand, BlockPos below, BlockHitResult rayTraceResult) {
        var ev = ForgeHooks.onRightClickBlock(player, hand, below, rayTraceResult);
        if (ev.isCanceled()) return ev.getCancellationResult();
        return null;
    }

    public static boolean canItemStack(ItemStack selected, ItemStack item) {
        return ItemHandlerHelper.canItemStacksStack(selected, item);
    }

    public static int getLightEmission(BlockState state, Level level, BlockPos pos) {
        return state.getLightEmission(level, pos);
    }

    public static Map<Block, Item> getBlockItemMap() {
        return GameData.getBlockItemMap();
    }

    public static boolean isInFluidThatCanExtinguish(Entity entity) {
        return entity.isInFluidType((a, b) -> a.canExtinguish(entity));
    }
}

