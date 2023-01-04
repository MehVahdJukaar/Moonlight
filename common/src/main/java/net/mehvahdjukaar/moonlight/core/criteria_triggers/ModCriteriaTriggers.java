package net.mehvahdjukaar.moonlight.core.criteria_triggers;

import net.minecraft.advancements.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class ModCriteriaTriggers {

    public static void register() {
    }

    public static final GrindItemTrigger GRIND = CriteriaTriggers.register(new GrindItemTrigger());


    public static Advancement.Builder getEmptyBuilder() {
        var builder = Advancement.Builder.advancement();
        builder.display(new DisplayInfo(ItemStack.EMPTY, Component.empty(), Component.empty(), null, FrameType.TASK, false, false, true));
        return builder;
    }
}
