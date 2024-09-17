package net.mehvahdjukaar.moonlight.api.entity;

import net.mehvahdjukaar.moonlight.api.platform.ForgeHelper;
import net.mehvahdjukaar.moonlight.api.util.math.MthUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.TheEndGatewayBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

/**
 * Improved version of the projectile entity. Combines the functionality of AbstractArrow and ThrowableItemProjectile
 * Main features are:
 * - Improved collision handling, onHit and onBlockHit are called after the pos has been set and not before
 * - Correctly handles weird cases like portal and end portal hit, which arrows and snowballs both do only one of
 * - Collision can now use swept AABB collision instead of ray collision, greatly improving accuracy for blocks
 * - Handy overrides such as spawnTrailParticles and hasReachedEndOfLife
 * - Streamlined deceleration and gravity logic
 */
//TODO: update to 1.21!!!!
public abstract class ImprovedProjectileEntity extends ThrowableItemProjectile {

    private static final EntityDataAccessor<Byte> ID_FLAGS = SynchedEntityData.defineId(ImprovedProjectileEntity.class, EntityDataSerializers.BYTE);

    protected Vec3 movementOld;

    // Renamed inGround. This is used to check if the projectile is not moving
    protected boolean isStuck = false;
    protected int stuckTime = 0;

    protected int maxAge = 300;
    protected int maxStuckTime = 20;

    protected ImprovedProjectileEntity(EntityType<? extends ThrowableItemProjectile> type, Level world) {
        super(type, world);
        this.movementOld = this.getDeltaMovement();
        //remember to add STEP_HEIGHT attribute!
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
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ID_FLAGS, (byte) 0);
    }

    public boolean hasLeftOwner() {
        return this.leftOwner;
    }

    //mix of projectile + arrow code to do what both do+  fix some issues
    @SuppressWarnings("ConstantConditions")
    @Override
    public void tick() {
        //entity has this param. we sync them for consistency
        this.noPhysics = this.isNoPhysics();

        // Projectile tick stuff
        if (!this.hasBeenShot) {
            this.gameEvent(GameEvent.PROJECTILE_SHOOT, this.getOwner());
            this.hasBeenShot = true;
        }
        if (!this.leftOwner) {
            this.leftOwner = this.checkLeftOwner();
        }

        this.baseTick();

        if (this.hasReachedEndOfLife() && !isRemoved()) {
            this.reachedEndOfLife();
        }

        // end of projectile tick stuff

        // AbstractArrow + ThrowableProjectile stuff

        //fixed vanilla arrow code. You're welcome
        Level level = this.level();
        Vec3 movement = this.getDeltaMovement();
        this.movementOld = movement;

        if (this.stuckSpeedMultiplier.lengthSqr() > 1.0E-7) {
            movement = movement.multiply(this.stuckSpeedMultiplier);
            this.stuckSpeedMultiplier = Vec3.ZERO;
            this.setDeltaMovement(Vec3.ZERO);
        }

        if (!this.noPhysics && this.isStuck) {
            this.stuckTime++;
        }else {
            this.stuckTime = 0;
        }

        this.move(MoverType.SELF, movement);

        // rest stuff
        this.tryCheckInsideBlocks();
        this.updateFireState();

        // after we finished moving we can apply forces and  particles

        // update movement and particles
        float deceleration = this.isInWater() ? this.getWaterInertia() : this.getInertia();

        this.setDeltaMovement(this.getDeltaMovement().scale(deceleration));
        if (!this.isNoGravity() && !noPhysics) {
            this.setDeltaMovement(this.getDeltaMovement().subtract(0, this.getGravity(), 0));
        }

        if (!isStuck) {
            if (level.isClientSide) {
                this.spawnTrailParticles();
            }

            this.updateRotation();
        }

        // check if stuck
        this.isStuck = !this.noPhysics && this.position().subtract(this.xo, this.yo, this.zo).lengthSqr() < (0.0001 * 0.0001);

    }

    private void updateFireState() {
        //copied bit from move method. Extracted for clarity
        this.wasOnFire = this.isOnFire();

        if (this.level().getBlockStatesIfLoaded(this.getBoundingBox().deflate(1.0E-6)).noneMatch((arg) ->
                arg.is(BlockTags.FIRE) || arg.is(Blocks.LAVA))) {
            if (this.getRemainingFireTicks() <= 0) {
                this.setRemainingFireTicks(-this.getFireImmuneTicks());
            }

            if (this.wasOnFire && (this.isInPowderSnow || this.isInWaterRainOrBubble() ||
                    ForgeHelper.isInFluidThatCanExtinguish(this))) {
                this.playEntityOnFireExtinguishedSound();
            }
        }

        if (this.isOnFire() && (this.isInPowderSnow || this.isInWaterRainOrBubble() || ForgeHelper.isInFluidThatCanExtinguish(this))) {
            this.setRemainingFireTicks(-this.getFireImmuneTicks());
        }
    }

    /**
     * Tries moving this entity by movement amount.
     * Does all the checks it needs and calls onHit if it hits something.
     * This was made from entity.move  + much stuff from both arrow and projectile code.
     * Does not check for fall damage or other living entity specific stuff
     * If blocks are hit movement is not modified. You need to react in onHitBlock if you wish to do so
     * Movement isn't modified at all here.
     * On fallOn isn't called as we call onProjectile hit. Projectiles dont fall.
     *
     * @param movement amount to travel by
     */
    @Override
    public void move(MoverType moverType, Vec3 movement) {
        // use normal movement logic if not self.. idk why compat i guess incase we were to use ray collider
        // also ued for no physics as that will just set the pos without doing any collision
        if (moverType != MoverType.SELF || this.noPhysics) {
            super.move(moverType, movement);
            return;
        }

        movement = this.maybeBackOffFromEdge(movement, moverType);

        Level level = this.level();
        Vec3 pos = this.position();

        // Applies collisions calculating hit face and new pos
        ColliderType colliderType = this.getColliderType();

        HitResult hitResult = switch (colliderType) {
            case RAY -> level.clip(new ClipContext(pos, pos.add(movement),
                    ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
            case AABB -> MthUtils.collideWithSweptAABB(this, movement, 2);
            case ENTITY_COLLIDE -> {
                Vec3 vec3 = this.collide(movement);
                Vec3 sub = vec3.subtract(movement);
                yield vec3 == movement ? BlockHitResult.miss(pos.add(vec3), Direction.UP,
                        BlockPos.containing(pos.add(vec3))) : new BlockHitResult(pos.add(vec3),
                        Direction.getNearest(sub.x, sub.y, sub.z), BlockPos.containing(pos.add(vec3)), false);
            }
        };

        Vec3 newPos = hitResult.getLocation();
        Vec3 newMovement = newPos.subtract(pos);
        this.setPos(newPos.x, newPos.y, newPos.z);

        //this is mainly used for players
        boolean bl = !Mth.equal(newMovement.x, movement.x);
        boolean bl2 = !Mth.equal(newMovement.z, movement.z);
        this.horizontalCollision = bl || bl2;
        this.verticalCollision = newMovement.y != movement.y;
        this.verticalCollisionBelow = this.verticalCollision && newMovement.y < 0.0;
        if (this.horizontalCollision) {
            this.minorHorizontalCollision = this.isHorizontalCollisionMinor(newMovement);
        } else {
            this.minorHorizontalCollision = false;
        }

        //try hit entity
        EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult(level, this, pos, newPos,
                this.getBoundingBox().expandTowards(newPos.subtract(pos)).inflate(1.0D), this::canHitEntity);

        if (entityHitResult != null) {
            hitResult = entityHitResult;
        }

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
            //ThrowableProjectile
            //TODO:!!!! fafactor this whole class!!
            if (hitState.is(Blocks.NETHER_PORTAL)) {
                //this.handleInsidePortal(hitPos);
              //  portalHit = true;
            } else if (hitState.is(Blocks.END_GATEWAY)) {
                //if (level.getBlockEntity(hitPos) instanceof TheEndGatewayBlockEntity tile && TheEndGatewayBlockEntity.canEntityTeleport(this)) {
                  //  TheEndGatewayBlockEntity.teleportEntity(level, hitPos, hitState, this, tile);
                //}
               // portalHit = true;
            }
        }

        if (!portalHit && hitResult != null && hitResult.getType() != HitResult.Type.MISS &&
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
        return this.tickCount > this.maxAge || this.stuckTime > maxStuckTime;
    }

    /**
     * remove condition
     */
    public void reachedEndOfLife() {
        this.remove(RemovalReason.DISCARDED);
    }

    public void spawnTrailParticles() {

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
        tag.putBoolean("stuck", this.isStuck);
        tag.putInt("stuckTime", this.stuckTime);
        tag.putBoolean("noPhysics", this.isNoPhysics());
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.isStuck = tag.getBoolean("stuck");
        this.stuckTime = tag.getInt("stuckTime");
        this.setNoPhysics(tag.getBoolean("noPhysics"));
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

    protected void setFlag(int id, boolean value) {
        byte b0 = this.entityData.get(ID_FLAGS);
        if (value) {
            this.entityData.set(ID_FLAGS, (byte) (b0 | id));
        } else {
            this.entityData.set(ID_FLAGS, (byte) (b0 & ~id));
        }
    }

    protected boolean getFlag(int id) {
        return (this.entityData.get(ID_FLAGS) & id) != 0;
    }

    // 2 cause its same as arrows for consistency
    public void setNoPhysics(boolean noPhysics) {
        this.noPhysics = noPhysics;
        this.setFlag(2, noPhysics);
    }

    public boolean isNoPhysics() {
        return this.getFlag(2);
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
        AABB,
        ENTITY_COLLIDE
    }
}
