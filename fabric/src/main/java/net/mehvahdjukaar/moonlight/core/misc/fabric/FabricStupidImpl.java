package net.mehvahdjukaar.moonlight.core.misc.fabric;

import net.fabricmc.fabric.impl.resource.conditions.ResourceConditionsImpl;
import net.minecraft.core.RegistryAccess;

public class FabricStupidImpl {
    public static void assignDumbStaticThreadLocal(RegistryAccess.Frozen a) {
        try {
            ResourceConditionsImpl.CURRENT_REGISTRIES.set(a);
        } catch (Exception ignored) {

        }
    }
}
