package net.mehvahdjukaar.moonlight.core.misc;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.schedule.Activity;

public interface IExtendedBrain {

    void moonlight$scheduleActivity(LivingEntity owner, Activity activity, int startTime, int endTime);
}
