package net.mehvahdjukaar.moonlight.api.platform.platform;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.mixins.platform.ContextAwareReloadListenerAccessor;
import net.mehvahdjukaar.moonlight.platform.MoonlightForge;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerExplosion;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.*;
import net.neoforged.neoforge.common.conditions.ConditionalOps;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.registries.GameData;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;
import net.neoforged.neoforge.transfer.item.WorldlyContainerWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class ForgeHelperImpl {

    public static boolean fireOnProjectileImpact(Projectile projectile, HitResult blockHitResult) {
        return EventHooks.onProjectileImpact(projectile, blockHitResult);
    }

    public static <T> RegistryOps<T> conditionalOps(DynamicOps<T> ops, HolderLookup.Provider provider, SimplePreparableReloadListener<?> reloader) {
        var rel = ((ContextAwareReloadListenerAccessor) reloader);
        return new ConditionalOps<>(RegistryOps.create(ops, provider), rel.invokeGetContext());
    }

    public static <T> Codec<Optional<T>> conditionalCodec(Codec<T> codec) {
        return ConditionalOps.createConditionalCodec(codec);
    }

    public static boolean isCurativeItem(ItemStack stack, MobEffectInstance effect) {
        // mirrors the vanilla consume effects: milk clears all, honey cures poison
        if (stack.is(Items.MILK_BUCKET)) return true;
        if (stack.is(Items.HONEY_BOTTLE)) return effect.is(MobEffects.POISON);
        return false;
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
        if (level instanceof ServerLevel serverLevel) {
            return CommonHooks.canEntityDestroy(serverLevel, blockPos, animal);
        }
        return true;
    }

    public static boolean fireOnExplosionStart(Level level, Explosion explosion) {
        if (explosion instanceof ServerExplosion serverExplosion) {
            return EventHooks.onExplosionStart(level, serverExplosion);
        }
        return false;
    }

    public static void fireOnExplosionDetonate(Level level, Explosion explosion, List<Entity> entities, double diameter) {
        if (explosion instanceof ServerExplosion serverExplosion) {
            // the affected block list is computed inside vanilla's explode() and not exposed here
            EventHooks.onExplosionDetonate(level, serverExplosion, entities, List.of());
        }
    }

    public static void fireOnLivingConvert(LivingEntity skellyHorseMixin, LivingEntity newHorse) {
        EventHooks.onLivingConvert(newHorse, newHorse);
    }

    public static boolean canLivingConvert(LivingEntity entity, EntityType<? extends LivingEntity> outcome, Consumer<Integer> timer) {
        return EventHooks.canLivingConvert(entity, outcome, timer);
    }

    public static float getExplosionResistance(BlockState state, Level level, BlockPos pos, Explosion explosion) {
        return state.getExplosionResistance(level, pos, explosion);
    }

    public static void fireOnBlockExploded(BlockState blockstate, Level level, BlockPos blockpos, Explosion explosion) {
        if (level instanceof ServerLevel serverLevel) {
            blockstate.onBlockExploded(serverLevel, blockpos, explosion);
        }
    }

    public static boolean isFireSource(BlockState blockState, Level level, BlockPos pos, Direction up) {
        return blockState.isFireSource(level, pos, up);
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
        var remainder = itemstack.getCraftingRemainder();
        return remainder == null ? Optional.empty() : Optional.of(remainder.create());
    }

    public static void reviveEntity(Entity entity) {
        entity.revive();
    }


    public static boolean fireOnCropsGrowPre(ServerLevel level, BlockPos pos, BlockState state, boolean b) {
        return CommonHooks.canCropGrow(level, pos, state, b);
    }

    public static void fireOnCropsGrowPost(ServerLevel level, BlockPos pos, BlockState state) {
        CommonHooks.fireCropGrowPost(level, pos, state);
    }

    public static void fireOnEquipmentChange(LivingEntity entity, EquipmentSlot slot, ItemStack from, ItemStack to) {
        NeoForge.EVENT_BUS.post(new LivingEquipmentChangeEvent(entity, slot, from, to));
    }

    @Nullable
    public static InteractionResult fireOnRightClickBlock(Player player, InteractionHand hand, BlockPos below, BlockHitResult rayTraceResult) {
        var ev = CommonHooks.onRightClickBlock(player, hand, below, rayTraceResult);
        if (ev.isCanceled()) return ev.getCancellationResult();
        return null;
    }

    public static int getLightEmission(BlockState state, Level level, BlockPos pos) {
        return state.getLightEmission(level, pos);
    }

    public static Map<Block, Item> getBlockItemMap() {
        return GameData.getBlockItemMap();
    }

    public static void registerDefaultContainerCap(BlockEntityType<? extends Container> type) {
        Moonlight.assertInitPhase();

        Consumer<RegisterCapabilitiesEvent> eventConsumer = event -> {
            event.registerBlockEntity(Capabilities.Item.BLOCK, type, ForgeHelperImpl::makeDefaultInvHandler);
        };
        MoonlightForge.getCurrentBus().addListener(eventConsumer);
    }

    public static @NotNull ResourceHandler<ItemResource> makeDefaultInvHandler(Container container, Direction side) {
        if (container instanceof WorldlyContainer wc) {
            return new WorldlyContainerWrapper(wc, side == null ? Direction.UP : side);
        } else {
            return VanillaContainerWrapper.of(container);
        }
    }


    public static boolean isInFluidThatCanExtinguish(Entity entity) {
        var fluid = entity.level().getFluidState(entity.blockPosition());
        return !fluid.isEmpty() && fluid.getFluidType().canExtinguish(entity);
    }

    public static Identifier getQueriedLootTableId(LootContext lootContext) {
        return lootContext.getQueriedLootTableId();
    }
}

