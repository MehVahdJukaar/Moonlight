package net.mehvahdjukaar.moonlight.api.block;

import com.google.common.collect.Lists;
import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluids;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.function.Supplier;

public class ModLiquidBlock extends LiquidBlock {

    private static Field FORGE_BLOCK_SUPPLIER;
    private static Field INIT;

    public ModLiquidBlock(Supplier<? extends FlowingFluid> supplier, Properties arg) {
        super(PlatformHelper.getPlatform().isFabric() ? supplier.get() : Fluids.WATER, arg);
        if (PlatformHelper.getPlatform().isForge()) {
            if (FORGE_BLOCK_SUPPLIER == null) FORGE_BLOCK_SUPPLIER = PlatformHelper.findField(LiquidBlock.class, "supplier");
            if (INIT == null) INIT = PlatformHelper.findField(LiquidBlock.class, "fluidStateCacheInitialized");
            try {
                for(var f : LiquidBlock.class.getDeclaredFields()){
                    if(f.getType() == FlowingFluid.class){
                        f.setAccessible(true);
                        f.set(this, null);
                    }else if(f.getType() == ArrayList.class){
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
                Moonlight.LOGGER.error("Failed to setup ModLiquidBlock class : "+e);
                throw new RuntimeException(e);
            }
        }

    }
}
