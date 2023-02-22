package net.mehvahdjukaar.moonlight.api.item;

import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.level.material.Fluid;

import java.lang.reflect.Field;

public class ModBucketItem extends BucketItem {

    private static final Field CONTENT = PlatformHelper.findField(BucketItem.class, "content");

    public ModBucketItem(Fluid fluid, Properties properties) {
        super(fluid, properties);
        if(PlatformHelper.getPlatform().isForge()){
            try {
                CONTENT.setAccessible(true);
                CONTENT.set(this, null);
            }catch (Exception e){
                throw new RuntimeException(e);
            }
        }

    }
}
