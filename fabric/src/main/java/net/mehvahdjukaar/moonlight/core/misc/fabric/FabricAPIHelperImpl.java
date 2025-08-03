package net.mehvahdjukaar.moonlight.core.misc.fabric;

import net.fabricmc.fabric.impl.resource.conditions.ResourceConditionsImpl;
import net.minecraft.core.RegistryAccess;

public class FabricAPIHelperImpl {
    public static void assignDumbStaticThreadLocal(RegistryAccess.Frozen a) {
        ResourceConditionsImpl.CURRENT_REGISTRIES.set(a);
    }
}
