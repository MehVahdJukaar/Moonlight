package net.mehvahdjukaar.moonlight.core.misc.forge;

import net.minecraft.core.RegistryAccess;

import java.lang.reflect.Field;

public class FabricAPIHelperImpl {

    private static final Field FIELD;

    static {
        Field f = null;
        try {
            var clazz = Class.forName("net.fabricmc.fabric.impl.resource.conditions.ResourceConditionsImpl");
            f = clazz.getDeclaredField("CURRENT_REGISTRIES");
            f.setAccessible(true);
        } catch (Exception ignored) {
        }
        FIELD = f;
    }

    public static void assignDumbStaticThreadLocal(RegistryAccess.Frozen registryAccess) {
        //WHY the fuck do we have to fix FABRIC ON FORGE!!!
        if (FIELD == null) return;
        try {
            //Crappy thread local object propagation atchitecture. super bad!! Pass fields properly instead of making assumptions about threads. Heck a static global field would have even been better
            ((ThreadLocal<RegistryAccess>) FIELD.get(null)).set(registryAccess);
        } catch (Exception ignored) {

        }

    }
}
