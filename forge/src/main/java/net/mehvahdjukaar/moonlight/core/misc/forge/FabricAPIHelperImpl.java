package net.mehvahdjukaar.moonlight.core.misc.forge;

import net.minecraft.core.RegistryAccess;

import java.lang.reflect.Field;

public class FabricAPIHelperImpl {

    private static final Field FIELD;

    static{
        Field f = null;
        try {
            var clazz =Class.forName("net.fabricmc.fabric.impl.resource.conditions.ResourceConditionsImpl");
            f = clazz.getDeclaredField("CURRENT_REGISTRIES");
            f.setAccessible(true);
        }catch (Exception ignored){
        }
        FIELD = f;
    }

    public static void assignDumbStaticThreadLocal(RegistryAccess.Frozen registryAccess) {
        //WHY the fuck do we have to fix FABRIC ON FORGE!!!
        try{
            FIELD.set(null, registryAccess);
        }catch (Exception ignored){

        }

    }
}
