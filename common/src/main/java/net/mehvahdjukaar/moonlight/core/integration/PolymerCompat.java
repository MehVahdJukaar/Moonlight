package net.mehvahdjukaar.moonlight.core.integration;

public class PolymerCompat {

    private static final Class<?> POLYMER_SYNCED_OBJ;

    static {
        try {
            POLYMER_SYNCED_OBJ = Class.forName("eu.pb4.polymer.core.api.utils.PolymerSyncedObject");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isPolymerObj(Object obj) {
        return POLYMER_SYNCED_OBJ.isInstance(obj);
    }
}
