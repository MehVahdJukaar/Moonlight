package net.mehvahdjukaar.moonlight.core.misc;

import net.minecraft.world.entity.schedule.Activity;

public record ScheduledActivity(Activity activity, int startTime, int endTime) {
    public boolean isActiveAt(long dayTime) {
        if (startTime < endTime) return dayTime >= startTime && dayTime < endTime;

        return dayTime >= startTime || dayTime < endTime;
    }

}