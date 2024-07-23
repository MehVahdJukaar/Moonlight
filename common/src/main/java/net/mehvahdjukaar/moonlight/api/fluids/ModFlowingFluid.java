package net.mehvahdjukaar.moonlight.api.fluids;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.mehvahdjukaar.moonlight.api.client.ModFluidRenderProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathType;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Supplier;

//I hate this class
public abstract class ModFlowingFluid extends FlowingFluid {

    @Nullable
    private final Supplier<? extends LiquidBlock> block;
    private final boolean convertsToSource;
    public final boolean hasCustomFluidType;

    protected ModFlowingFluid(Properties properties, Supplier<? extends LiquidBlock> block) {
        this.block = block;
        this.convertsToSource = properties.canConvertToSource;
        this.hasCustomFluidType = properties.copyFluid == null;
        this.afterInit(properties);
    }

    private void afterInit(ModFlowingFluid.Properties properties) {
    }

    public static Properties properties() {
        return new Properties();
    }

    @Override
    protected boolean canConvertToSource(Level level) {
        return convertsToSource;
    }

    @Override
    protected void beforeDestroyingBlock(LevelAccessor worldIn, BlockPos pos, BlockState state) {
        BlockEntity blockEntity = state.hasBlockEntity() ? worldIn.getBlockEntity(pos) : null;
        Block.dropResources(state, worldIn, pos, blockEntity);
    }

    @Override
    protected boolean canBeReplacedWith(FluidState state, BlockGetter level, BlockPos pos, Fluid fluidIn, Direction direction) {
        // Based on the water implementation, may need to be overriden for mod fluids that shouldn't behave like water.
        return direction == Direction.DOWN && !isSame(fluidIn);
    }

    @Override
    protected BlockState createLegacyBlock(FluidState state) {
        if (block != null)
            return block.get().defaultBlockState().setValue(LiquidBlock.LEVEL, getLegacyLevel(state));
        return Blocks.AIR.defaultBlockState();
    }

    @Override
    public boolean isSame(Fluid fluidIn) {
        return fluidIn == getSource() || fluidIn == getFlowing();
    }

    @Override
    public abstract Fluid getSource();

    @Override
    public abstract Fluid getFlowing();

    @Environment(EnvType.CLIENT)
    public abstract ModFluidRenderProperties createRenderProperties();

    /**
     * This is for normal fluids. Just wraps forge stuff. I'll figure out fabric implementation
     */
    @SuppressWarnings("All") //dont care here as this object wont even be used
    public static final class Properties {
        public String descriptionId;
        public double motionScale = 0.014D;
        public boolean canPushEntity = true;
        public boolean canSwim = true;
        public boolean canDrown = true;
        public float fallDistanceModifier = 0.5F;
        public boolean canExtinguish = false;
        public boolean supportsBoating = false;
        public boolean canConvertToSource = false;
        @Nullable
        public PathType pathType = PathType.WATER,
                adjacentPathType = PathType.WATER_BORDER;
        public boolean canHydrate = false;
        public int lightLevel = 0,
                density = 1000,
                temperature = 300,
                viscosity = 1000;
        public Rarity rarity = Rarity.COMMON;
        public Map<String, SoundEvent> sounds;
        @Deprecated
        public Fluid copyFluid = null;

        @Deprecated(forRemoval = true)
        public Properties copyFluid(Fluid fluid) {
            //this.copyFluid = fluid; //causes issues
            return this;
        }

        /**
         * Sets the identifier representing the name of the fluid type.
         */
        public Properties descriptionId(String descriptionId) {
            this.descriptionId = descriptionId;
            return this;
        }

        /**
         * Sets how much the velocity of the fluid should be scaled by.
         */
        public Properties motionScale(double motionScale) {
            this.motionScale = motionScale;
            return this;
        }

        public Properties setCanConvertToSource(boolean canConvertToSource) {
            this.canConvertToSource = canConvertToSource;
            return this;
        }

        /**
         * Sets whether the fluid can push an entity.
         */
        public Properties canPushEntity(boolean canPushEntity) {
            this.canPushEntity = canPushEntity;
            return this;
        }

        /**
         * Sets whether the fluid can be swum in.
         */
        public Properties canSwim(boolean canSwim) {
            this.canSwim = canSwim;
            return this;
        }

        /**
         * Sets whether the fluid can drown something.
         */
        public Properties canDrown(boolean canDrown) {
            this.canDrown = canDrown;
            return this;
        }

        /**
         * Sets how much the fluid should scale the damage done when hitting
         * the ground per tick.
         */
        public Properties fallDistanceModifier(float fallDistanceModifier) {
            this.fallDistanceModifier = fallDistanceModifier;
            return this;
        }

        /**
         * Sets whether the fluid can extinguish.
         */
        public Properties canExtinguish(boolean canExtinguish) {
            this.canExtinguish = canExtinguish;
            return this;
        }

        /**
         * Sets whether the fluid supports boating.
         */
        public Properties supportsBoating(boolean supportsBoating) {
            this.supportsBoating = supportsBoating;
            return this;
        }

        /**
         * Sets the path type of this fluid.
         *
         * @param pathType the path type of this fluid
         * @return the property holder instance
         */
        public Properties pathType(@Nullable PathType pathType) {
            this.pathType = pathType;
            return this;
        }

        /**
         * Sets the path type of the adjacent fluid. Path types with a negative
         * malus are not traversable. Pathfinding will favor paths consisting of
         * a lower malus.
         *
         * @param adjacentPathType the path type of this fluid
         * @return the property holder instance
         */
        public Properties adjacentPathType(@Nullable PathType adjacentPathType) {
            this.adjacentPathType = adjacentPathType;
            return this;
        }

        /**
         * Sets a sound to play when a certain action is performed. Actions id have to match forge ones. I.e: "bucket_fill"
         */
        public Properties sound(String soundActionId, SoundEvent sound) {
            this.sounds.put(soundActionId, sound);
            return this;
        }

        /**
         * Sets whether the fluid can hydrate.
         *
         * <p>Hydration is an arbitrary word which depends on the implementation.
         */
        public Properties canHydrate(boolean canHydrate) {
            this.canHydrate = canHydrate;
            return this;
        }

        /**
         * Sets the light level emitted by the fluid.
         */
        public Properties lightLevel(int lightLevel) {
            this.lightLevel = lightLevel;
            return this;
        }

        /**
         * Sets the density of the fluid.
         */
        public Properties density(int density) {
            this.density = density;
            return this;
        }

        /**
         * Sets the temperature of the fluid.
         */
        public Properties temperature(int temperature) {
            this.temperature = temperature;
            return this;
        }

        /**
         * Sets the viscosity, or thickness, of the fluid.
         */
        public Properties viscosity(int viscosity) {
            this.viscosity = viscosity;
            return this;
        }

        /**
         * Sets the rarity of the fluid.
         */
        public Properties rarity(Rarity rarity) {
            this.rarity = rarity;
            return this;
        }
    }

}
