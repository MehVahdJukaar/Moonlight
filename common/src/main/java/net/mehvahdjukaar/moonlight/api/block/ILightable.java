package net.mehvahdjukaar.moonlight.api.block;

import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.Nullable;

/**
 * Author: MehVahdJukaar
 * Used for blocks that can be lit up. Implement for best compatibility. Do not modify
 */
public interface ILightable {

    TagKey<Item> FLINT_AND_STEELS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "tools/igniter"));

    boolean isLitUp(BlockState state, BlockGetter level, BlockPos pos);

    default void setLitUp(BlockState state, LevelAccessor world, BlockPos pos, boolean lit) {
        setLitUp(state, world, pos, null, lit);
    }

    void setLitUp(BlockState state, LevelAccessor world, BlockPos pos, @Nullable Entity entity,  boolean lit);


    default boolean tryLightUp(@Nullable Entity player, BlockState state, BlockPos pos, LevelAccessor world, FireSoundType fireSourceType) {
        if (!isLitUp(state, world, pos)) {
            if (!world.isClientSide()) {
                this.setLitUp(state, world, pos, true);
                playLightUpSound(world, pos, fireSourceType);
            }
            world.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
            return true;
        }
        return false;
    }

    default boolean tryExtinguish(@Nullable Entity player, BlockState state, BlockPos pos, LevelAccessor world) {
        if (this.isLitUp(state, world, pos)) {
            if (!world.isClientSide()) {
                playExtinguishSound(world, pos);
                this.setLitUp(state, world, pos, false);
            } else {
                spawnSmokeParticles(state, pos, world);
            }
            world.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
            return true;
        }
        return false;
    }

    default boolean lightableInteractWithEntity(Level level, BlockState state, Entity projectile, BlockPos pos) {
        if (projectile.isOnFire()) {
            Entity owner = projectile instanceof TraceableEntity te ? te.getOwner() : null;
            if (owner == null || owner instanceof Player || PlatHelper.isMobGriefingOn(level, owner)) {
                return tryLightUp(projectile, state, pos, level, FireSoundType.FLAMING_ARROW);
            }
        }
        // Now handled by mixin since it needs bigger radius
        /*
        else if (projectile instanceof ThrownPotion potion &&
                potion.getItem().getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).is(Potions.WATER)) {
            Entity entity = projectile.getOwner();
            boolean flag = entity == null || entity instanceof Player || PlatHelper.isMobGriefingOn(level, entity);
            return flag && extinguish(projectile, state, pos, level);
        }*/
        return false;
    }

    //call on use
    default InteractionResult lightableInteractWithPlayerItem(BlockState state, Level level, BlockPos pos, Player player,
                                                         InteractionHand hand, ItemStack stack) {
        if (Utils.mayPerformBlockAction(player, pos, stack)) {
            if (!this.isLitUp(state, level, pos)) {
                Item item = stack.getItem();
                // fire charges also have the light fire ability so they must be checked first
                if (item instanceof FireChargeItem) {
                    if (tryLightUp(player, state, pos, level, FireSoundType.FIRE_CHANGE)) {
                        stack.consume(1, player);
                        return InteractionResult.SUCCESS;
                    }
                } else if (isIgniter(stack)) {
                    if (tryLightUp(player, state, pos, level, FireSoundType.FLINT_AND_STEEL)) {
                        stack.hurtAndBreak(1, player, hand.asEquipmentSlot());
                        return InteractionResult.SUCCESS;
                    }
                }
            } else if (this.canBeExtinguishedBy(stack)) {
                if (tryExtinguish(player, state, pos, level)) {
                    if (!(stack.getItem() instanceof BrushItem)) {
                        return InteractionResult.SUCCESS;
                    }
                }
            }
        }
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    /**
     * Checks if this item can be used to light blocks on fire. Item abilities are checked too so tools that can
     * only sometimes light fires (broken tinkers tools for example) are respected
     */
    static boolean isIgniter(ItemStack stack) {
        return stack.getItem() instanceof FlintAndSteelItem || stack.is(FLINT_AND_STEELS) ||
                PlatHelper.canLightFire(stack);
    }

    default boolean canBeExtinguishedBy(ItemStack item) {
        return item.getItem() instanceof ShovelItem || item.getItem() instanceof BrushItem;
    }

    default void playLightUpSound(LevelAccessor world, BlockPos pos, FireSoundType type) {
        type.play(world, pos);
    }

    default void playExtinguishSound(LevelAccessor world, BlockPos pos) {
        world.playSound(null, pos, SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 0.5F, 1.5F);
    }

    default void spawnSmokeParticles(BlockState state, BlockPos pos, LevelAccessor world) {
        RandomSource random = world.getRandom();
        for (int i = 0; i < 10; ++i) {
            //particle offset
            world.addParticle(ParticleTypes.SMOKE, pos.getX() + 0.25f + random.nextFloat() * 0.5f, pos.getY() + 0.35f + random.nextFloat() * 0.5f, pos.getZ() + 0.25f + random.nextFloat() * 0.5f, 0, 0.005, 0);
        }
    }

    @FunctionalInterface
    interface FireSoundType {
        FireSoundType FLINT_AND_STEEL = (level, pos) ->
                level.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.4F + 0.8F);

        FireSoundType FIRE_CHANGE = (level, pos) ->
                level.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 1.0F, (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.2F + 1.0F);

        FireSoundType FLAMING_ARROW = (level, pos) ->
                level.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 0.5F, 1.4F);

        void play(LevelAccessor level, BlockPos pos);

    }

}
