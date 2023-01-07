package net.mehvahdjukaar.moonlight.core.criteria_triggers;

import net.mehvahdjukaar.moonlight.api.platform.ForgeHelper;
import net.minecraft.advancements.CriteriaTriggers;

public class ModCriteriaTriggers {

    public static void register() {
    }

    public static final GrindItemTrigger GRIND = CriteriaTriggers.register(new GrindItemTrigger());

}
