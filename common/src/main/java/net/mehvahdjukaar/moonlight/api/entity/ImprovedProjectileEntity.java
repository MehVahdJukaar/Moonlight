package net.mehvahdjukaar.moonlight.api.entity;

import net.mehvahdjukaar.moonlight.api.platform.ForgeHelper;
import net.mehvahdjukaar.moonlight.api.util.math.MthUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.TheEndGatewayBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/**
 * Improved version of the projectile entity. Combines the functionality of AbstractArrow and ThrowableItemProjectile
 * Main features are:
 * - Improved collision handling, onHit and onBlockHit are called after the pos has been set and not before
 * - Correctly handles weird cases like portal and end portal hit, which arrows and snowballs both do only one of
 * - Collision can now use swept AABB collision instead of ray collision, greatly improving accuracy for blocks
 * - Handy overrides such as spawnTrailParticles and hasReachedEndOfLife
 * - Streamlined deceleration and gravity logic
 */
public abstract class ImprovedProjectileEntity extends ThrowableItemProjectile {

    // Renamed inGround. This is used to check if the projectile has its center inside a bock
    protected boolean isInBlock = false;
    protected int inBlockTime = 0;

    protected int maxAge = 300;
    protected int maxInBlockTime = 20;

    protected ImprovedProjectileEntity(EntityType<? extends ThrowableItemProjectile> type, Level world) {
        super(type, world);
        this.setMaxUpStep(0);
    }

    protected ImprovedProjectileEntity(EntityType<? extends ThrowableItemProjectile> type, double x, double y, double z, Level world) {
        this(type, world);
        this.setPos(x, y, z);
    }

    protected ImprovedProjectileEntity(EntityType<? extends ThrowableItemProjectile> type, LivingEntity thrower, Level world) {
        this(type, thrower.getX(), thrower.getEyeY() - 0.1F, thrower.getZ(), world);
        this.setOwner(thrower);
    }

    @Override
    public float maxUpStep() {
        return super.maxUpStep();
    }

    @Override
    protected float getEyeHeight(Pose pose, EntityDimensions dimensions) {
        return dimensions.height * 0.5f;
    }

    // If its motion will be stopped by blocks. Will make this block call collide instead of setpos
    public boolean collidesWithBlocks() {
        return false;
    }

    //mix of projectile + arrow code to do what both do+  fix some issues
    @SuppressWarnings("ConstantConditions")
    @Override
    public void tick() {
        // Projectile tick stuff
        if (!this.hasBeenShot) {
            this.gameEvent(GameEvent.PROJECTILE_SHOOT, this.getOwner());
            this.hasBeenShot = true;
        }

        this.baseTick();

        // end of projectile tick stuff

        // some move() stuff
        this.wasOnFire = this.isOnFire();
        // end of move() stuff

        // AbstractArrow + ThrowableProjectile stuff

        //fixed vanilla arrow code. You're welcome
        Level level = this.level();
        Vec3 pos = this.position();
        BlockPos blockpos = this.blockPosition();
        Vec3 movement = this.getDeltaMovement();
        boolean client = level.isClientSide;

        //sets on ground. Used for arrows stuck in block. Probably not needed anymore
        BlockState blockstate = level.getBlockState(blockpos);
        if (!blockstate.isAir()) {
            VoxelShape voxelshape = blockstate.getCollisionShape(level, blockpos, CollisionContext.of(this));
            if (!voxelshape.isEmpty()) {
                Vec3 centerPos = this.getEyePosition();

                for (AABB aabb : voxelshape.toAabbs()) {
                    if (aabb.move(blockpos).contains(centerPos)) {
                        this.isInBlock = true;
                        break;
                    }
                }
            }
        }

        if (this.isInWaterOrRain() || blockstate.is(Blocks.POWDER_SNOW)) {
            this.clearFire();
        }

        if (this.isInBlock && !noPhysics) {
            this.inBlockTime++;
            return;
        }
        this.inBlockTime = 0;

        this.updateRotation();

        // Applies collisions calculating hit face and new pos
        HitResult hitResult;
        if (this.getColliderType() == ColliderType.RAY) {
            hitResult = level.clip(new ClipContext(pos, pos.add(movement),
                    ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        } else {
            //Vec3 newMovement = this.collide(movement);
            hitResult = MthUtils.collideWithSweptAABB(this, movement, 2);
        }
        Vec3 newPos = hitResult.getLocation();
        this.setPos(newPos.x, newPos.y, newPos.z);


        // update movement and particles
        float deceleration = this.isInWater() ? this.getWaterInertia() : this.getInertia();
        if (client) {
            this.spawnTrailParticles();
        }

        this.setDeltaMovement(this.getDeltaMovement().scale(deceleration));
        if (!this.isNoGravity() && !noPhysics) {
            this.setDeltaMovement(this.getDeltaMovement().subtract(0, this.getGravity(), 0));
        }

        this.checkInsideBlocks();

        if (this.hasReachedEndOfLife() && !isRemoved()) {
            this.reachedEndOfLife();
        }

        if (this.isRemoved()) return;

        //try hit entity
        EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult(level, this, pos, newPos,
                this.getBoundingBox().expandTowards(newPos.subtract(pos)).inflate(1.0D), this::canHitEntity);

        if (entityHitResult != null) {
            hitResult = entityHitResult;
        }

        if (hitResult.getType() == HitResult.Type.MISS) return;

        boolean portalHit = false;
        if (hitResult instanceof EntityHitResult ei) {
            Entity hitEntity = ei.getEntity();
            if (hitEntity == this.getOwner()) {
                if (!canHarmOwner()) {
                    hitResult = null;
                }
            } else if (hitEntity instanceof Player p1 && this.getOwner() instanceof Player p2 && !p2.canHarmPlayer(p1)) {
                hitResult = null;
            }
        } else if (hitResult instanceof BlockHitResult bi) {
            //portals. done here and not in onBlockHit to prevent any further calls
            BlockPos hitPos = bi.getBlockPos();
            BlockState hitState = level.getBlockState(hitPos);

            if (hitState.is(Blocks.NETHER_PORTAL)) {
                this.handleInsidePortal(hitPos);
                portalHit = true;
            } else if (hitState.is(Blocks.END_GATEWAY)) {
                if (level.getBlockEntity(hitPos) instanceof TheEndGatewayBlockEntity tile && TheEndGatewayBlockEntity.canEntityTeleport(this)) {
                    TheEndGatewayBlockEntity.teleportEntity(level, hitPos, hitState, this, tile);
                }
                portalHit = true;
            }
        }

        if (!portalHit && hitResult != null && hitResult.getType() != HitResult.Type.MISS && !noPhysics &&
                !ForgeHelper.onProjectileImpact(this, hitResult)) {
            this.onHit(hitResult);
        }


    }


    public boolean canHarmOwner() {
        if (getOwner() instanceof Player) {
            return level().getDifficulty().getId() >= 1;
        }
        return false;
    }

    protected float getInertia() {
        // normally 0.99 for everything
        return 0.99F;
    }

    protected float getWaterInertia() {
        // normally 0.6 for arrows and 0.99 for tridents and 0.8 for other projectiles
        return 0.6F;
    }

    /**
     * do stuff before removing, then call remove. Called when age reaches max age
     */
    public boolean hasReachedEndOfLife() {
        return this.tickCount > this.maxAge || this.inBlockTime > maxInBlockTime;
    }

    /**
     * remove condition
     */
    public void reachedEndOfLife() {
        this.remove(RemovalReason.DISCARDED);
    }

    @Deprecated(forRemoval = true)
    public void spawnTrailParticles(Vec3 oldPos, Vec3 newPos) {
    }

    public void spawnTrailParticles() {
        spawnTrailParticles(new Vec3(xo, yo, zo), this.position());

        if (this.isInWater()) {
            // Projectile particle code
            var movement = this.getDeltaMovement();
            double velX = movement.x;
            double velY = movement.y;
            double velZ = movement.z;
            for (int j = 0; j < 4; ++j) {
                double pY = this.getEyeY();
                level().addParticle(ParticleTypes.BUBBLE,
                        getX() - velX * 0.25D, pY - velY * 0.25D, getZ() - velZ * 0.25D,
                        velX, velY, velZ);
            }
        }
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("inBlock", this.isInBlock);
        tag.putInt("inBlockTime", this.inBlockTime);
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.isInBlock = tag.getBoolean("inBlock");
        this.inBlockTime = tag.getInt("inBlockTime");
    }

    @Override
    public void shootFromRotation(Entity shooter, float x, float y, float z, float velocity, float inaccuracy) {
        super.shootFromRotation(shooter, x, y, z, velocity, inaccuracy);
    }

    @Override
    public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
        super.shoot(x, y, z, velocity, inaccuracy);
    }

    // Has no effect. Give this to shoot method manually
    public float getDefaultShootVelocity() {
        return 1.5F;
    }


    @Deprecated(forRemoval = true)
    public boolean touchedGround;
    @Deprecated(forRemoval = true)
    public int groundTime = 0;

    @Deprecated(forRemoval = true)
    public void setNoPhysics(boolean noGravity) {
        super.setNoGravity(noGravity);
    }

    @Deprecated(forRemoval = true)
    public boolean isNoPhysics() {
        return super.isNoGravity();
    }

    @Deprecated(forRemoval = true)
    protected float getDeceleration() {
        return 0.99F;
    }

    /**
     * AABB: Swept AABB collision, gives very accurate block collisions and stops the entity on the first detected collision
     * RAY: Ray collision, fast but only accurate in the center of the projectile. Ok to use for small projectiles. What arrows use
     */
    protected ColliderType getColliderType() {
        return ColliderType.AABB;
    }

    protected enum ColliderType {
        RAY,
        AABB
    }
}
