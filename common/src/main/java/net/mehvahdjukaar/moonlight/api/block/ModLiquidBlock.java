package net.mehvahdjukaar.moonlight.api.block;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.moonlight.api.fluids.ModFlowingFluid;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluids;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.function.Supplier;

public class ModLiquidBlock extends LiquidBlock {

    private static final Codec<FlowingFluid> FLOWING_FLUID = BuiltInRegistries.FLUID.byNameCodec().comapFlatMap((fluid) -> {
        DataResult<FlowingFluid> result;
        if (fluid instanceof FlowingFluid flowingFluid) {
            result = DataResult.success(flowingFluid);
        } else {
            result = DataResult.error(() -> "Not a flowing fluid: " + fluid);
        }
        return result;
    }, (flowingFluid) -> flowingFluid);

    public static final MapCodec<ModLiquidBlock> CODEC = RecordCodecBuilder.mapCodec((i) -> i.group(
            FLOWING_FLUID.fieldOf("fluid").forGetter(ModLiquidBlock::getFlowingFluid), propertiesCodec()
    ).apply(i, (f, s) -> new ModLiquidBlock(() -> f, s)));

    private static Field FORGE_BLOCK_SUPPLIER;
    private static Field INIT;
    private final Supplier<? extends FlowingFluid> fluidState;

    public ModLiquidBlock(Supplier<? extends FlowingFluid> supplier, Properties arg) {
        super(PlatHelper.getPlatform().isFabric() ? supplier.get() : Fluids.WATER, arg);
        if (PlatHelper.getPlatform().isForge()) {
            if (FORGE_BLOCK_SUPPLIER == null)
                FORGE_BLOCK_SUPPLIER = PlatHelper.findField(LiquidBlock.class, "supplier");
            if (INIT == null) INIT = PlatHelper.findField(LiquidBlock.class, "fluidStateCacheInitialized");
            try {
                for (var f : LiquidBlock.class.getDeclaredFields()) {
                    if (f.getType() == FlowingFluid.class) {
                        f.setAccessible(true);
                        f.set(this, null);
                    } else if (f.getType() == ArrayList.class) {
                        f.setAccessible(true);
                        f.set(this, Lists.newArrayList());
                    }
                }
                INIT.setAccessible(true);
                INIT.set(this, false);
                this.registerDefaultState(this.stateDefinition.any().setValue(LEVEL, 0));
                FORGE_BLOCK_SUPPLIER.setAccessible(true);
                FORGE_BLOCK_SUPPLIER.set(this, supplier);
                INIT.set(this, false);
            } catch (Exception e) {
                Moonlight.LOGGER.error("Failed to setup ModLiquidBlock class : " + e);
                throw new RuntimeException(e);
            }
        }
        this.fluidState = supplier;

    }

    public FlowingFluid getFlowingFluid() {
        return this.fluidState.get();
    }

    @SuppressWarnings("all")
    @Override
    public MapCodec codec() {
        return CODEC;
    }
}
