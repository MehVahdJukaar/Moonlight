package net.mehvahdjukaar.moonlight.api.entity;

import net.mehvahdjukaar.moonlight.api.platform.ForgeHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
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
import org.jetbrains.annotations.Nullable;

// A class that combines and streamlines AbstractHurtingProjectile, AbstractArrow and ThrowableItemProjectile
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

        if (!this.leftOwner) {
            this.leftOwner = this.checkLeftOwner();
        }
        this.baseTick();

        // end of projectile tick stuff

        // some move() stuff
        this.wasOnFire = this.isOnFire();
        // end of move() stuff

        // AbstractArrow + ThrowableProjectile stuff

        //fixed vanilla arrow code. You're welcome
        Vec3 movement = this.getDeltaMovement();

        BlockPos blockpos = this.blockPosition();
        Level level = this.level();
        BlockState blockstate = level.getBlockState(blockpos);

        //sets on ground
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
        } else {
            this.inBlockTime = 0;

            this.updateRotation();

            Vec3 pos = this.position();
            boolean client = level.isClientSide;

            Vec3 newPos = pos.add(movement);

            //this just calculate the hit pos. Does NOT calculate our actual new position
            HitResult blockHitResult = level.clip(new ClipContext(pos, newPos,
                    ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));

            // actually moves
            if (this.collidesWithBlocks()) {
                //gets the actual new pos
                newPos = pos.add(this.collide(movement));
            }
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

            //calls on hit
            if (!this.isRemoved()) {
                //try hit entity
                EntityHitResult hitEntityResult = this.findHitEntity(pos, newPos);
                if (hitEntityResult != null) {
                    blockHitResult = hitEntityResult;
                }

                boolean portalHit = false;
                if (blockHitResult instanceof EntityHitResult ei) {
                    Entity hitEntity = ei.getEntity();
                    if (hitEntity == this.getOwner()) {
                        if (!canHarmOwner()) {
                            blockHitResult = null;
                        }
                    } else if (hitEntity instanceof Player p1 && this.getOwner() instanceof Player p2 && !p2.canHarmPlayer(p1)) {
                        blockHitResult = null;
                    }
                } else if (blockHitResult instanceof BlockHitResult bi) {
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

                if (!portalHit && blockHitResult != null && blockHitResult.getType() != HitResult.Type.MISS && !noPhysics &&
                        !ForgeHelper.onProjectileImpact(this, blockHitResult)) {
                    this.onHit(blockHitResult);
                    this.hasImpulse = true; //idk what this does
                }
            }
        }
        if (this.hasReachedEndOfLife() && !isRemoved()) {
            this.reachedEndOfLife();
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

    @Nullable
    protected EntityHitResult findHitEntity(Vec3 oPos, Vec3 pos) {
        return ProjectileUtil.getEntityHitResult(this.level(), this, oPos, pos,
                this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0D), this::canHitEntity);
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
}
