package net.mehvahdjukaar.moonlight.api.item;

import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import java.lang.reflect.Field;
import java.util.function.Supplier;

//no clue why this class even exists
public class ModBucketItem extends BucketItem {

    private static final Field CONTENT;
    static {
        Field c = null;
        for (var field : BucketItem.class.getDeclaredFields()) {
            if (field.getType() == Fluid.class) {
                c = field;
                break;
            }
        }
        CONTENT = c;

    }
    private final Supplier<Fluid> supplier;

    public ModBucketItem(Supplier<Fluid> fluid, Properties properties) {
        super(PlatHelper.getPlatform().isForge() ? Fluids.EMPTY : fluid.get(), properties);
        supplier = fluid;
        if (PlatHelper.getPlatform().isForge()) {
            try {
                //forge needs this to null
                CONTENT.setAccessible(true);
                CONTENT.set(this, null);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }


    @Deprecated(forRemoval = true)
    public ModBucketItem(Fluid fluid, Properties properties) {
        this(() -> fluid, properties);
    }

    public Fluid getFluid() {
        return this.supplier.get();
    }

}
