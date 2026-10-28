package net.mehvahdjukaar.moonlight.core.mixins;


import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.mehvahdjukaar.moonlight.core.misc.IExtendedBrain;
import net.mehvahdjukaar.moonlight.core.misc.ScheduledActivity;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.EnvironmentAttributeSystem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.List;

@Mixin(Brain.class)
public abstract class BrainMixin implements IExtendedBrain {
    @Unique
    @Nullable
    private List<ScheduledActivity> moonlight$scheduledActivities = null;
    @Unique
    @Nullable
    private LivingEntity moonlight$scheduleOwner = null;

    @Override
    public void moonlight$scheduleActivity(LivingEntity owner, Activity activity, int startTime, int endTime) {
        if (moonlight$scheduledActivities == null) moonlight$scheduledActivities = new ArrayList<>();
        moonlight$scheduledActivities.add(new ScheduledActivity(activity, startTime, endTime));
        moonlight$scheduleOwner = owner;
    }

    @WrapOperation(method = "updateActivityFromSchedule", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/attribute/EnvironmentAttributeSystem;getValue(Lnet/minecraft/world/attribute/EnvironmentAttribute;Lnet/minecraft/world/phys/Vec3;)Ljava/lang/Object;"))
    private Object moonlight$applyScheduledActivities(EnvironmentAttributeSystem attributes, EnvironmentAttribute<?> schedule,
                                                      Vec3 pos, Operation<Object> original) {
        Object activity = original.call(attributes, schedule, pos);
        if (moonlight$scheduledActivities == null || moonlight$scheduleOwner == null) return activity;

        long dayTime = moonlight$scheduleOwner.level().getOverworldClockTime() % 24000;
        //later ones win when windows overlap
        for (ScheduledActivity s : moonlight$scheduledActivities) {
            if (s.isActiveAt(dayTime)) {
                activity = s.activity();
            }
        }
        return activity;
    }
}
