package net.mehvahdjukaar.selene.villager_ai;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.mehvahdjukaar.selene.Selene;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.schedule.Schedule;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.ApiStatus;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Consumer;

public class VillagerAIManager {

    public static final DeferredRegister<Schedule> SCHEDULES = DeferredRegister.create(ForgeRegistries.SCHEDULES, Selene.MOD_ID);

    //schedule to which all the tasks are registered to
    protected static final RegistryObject<Schedule> CUSTOM_VILLAGER_SCHEDULE =
            SCHEDULES.register("custom_villager_schedule", Schedule::new);


    //called by mixin. Do not call
    @ApiStatus.Internal
    public static void onRegisterBrainGoals(Brain<Villager> brain, AbstractVillager villager) {
        if(villager instanceof Villager v) {
            var event = new VillagerBrainEvent(brain, v);
            MinecraftForge.EVENT_BUS.post(event);
            //dont waste time if it doesn't have a custom schedule
            if (event.hasCustomSchedule()) {
                //finalize schedule
                brain.setSchedule(event.buildFinalizedSchedule());
                brain.updateActivityFromSchedule(villager.level.getDayTime(), villager.level.getGameTime());
            }
        }
    }


    //just loads up the class to register the schedule
    @ApiStatus.Internal
    public static void init() {
    }


    /**
     * adds a memory module to the villager brain when it's created. Add here and not in the event if that memory needs to be saved, otherwise it will not be loaded since the event is called after the brain is deserialized from tag
     */
    public static void registerMemory(MemoryModuleType<?> memoryModuleType) {

        try {
            var oldValue = Villager.MEMORY_TYPES ;

            ImmutableList.Builder<MemoryModuleType<?>> builder = ImmutableList.builder();
            builder.addAll(oldValue);
            builder.add(memoryModuleType);
            Villager.MEMORY_TYPES = builder.build();

        } catch (Exception e) {
            Selene.LOGGER.warn("failed to register pumpkin sensor type for villagers: " + e);
        }
    }

}
