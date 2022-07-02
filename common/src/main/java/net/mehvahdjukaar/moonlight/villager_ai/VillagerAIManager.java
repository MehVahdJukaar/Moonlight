package net.mehvahdjukaar.moonlight.villager_ai;

import com.google.common.collect.ImmutableList;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.moonlight.Moonlight;
import net.mehvahdjukaar.moonlight.mixins.accessor.VillagerAccessor;
import net.mehvahdjukaar.moonlight.platform.registry.RegHelper;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.schedule.Schedule;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class VillagerAIManager {


    //schedule to which all the tasks are registered to
    public static final Supplier<Schedule> CUSTOM_VILLAGER_SCHEDULE =
            RegHelper.register("custom_villager_schedule", Schedule::new, Registry.SCHEDULE);


    //called by mixin. Do not call
    @ApiStatus.Internal
    @ExpectPlatform
    public static void onRegisterBrainGoals(Brain<Villager> brain, AbstractVillager villager) {
        throw new AssertionError();
    }

    //on forge use SubscribeEvent annotations instead
    @ExpectPlatform
    public static void addListener(Consumer<IVillagerBrainEvent> eventConsumer){
        throw new AssertionError();
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

            var oldValue = VillagerAccessor.getMemoryTypes();

            ImmutableList.Builder<MemoryModuleType<?>> builder = ImmutableList.builder();
            builder.addAll(oldValue);
            builder.add(memoryModuleType);
            VillagerAccessor.setMemoryTypes(builder.build());

        } catch (Exception e) {
            Moonlight.LOGGER.warn("failed to register pumpkin sensor type for villagers: " + e);
        }
    }

}
