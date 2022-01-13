package net.mehvahdjukaar.selene.villager_ai;

import net.mehvahdjukaar.selene.mixins.VillagerMixin;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.IEventListener;
import net.minecraftforge.fml.common.Mod;

import java.util.Optional;

public class VillagerAIManager {
    PlaceBlo
    private static final Map<>

    //called by mixin
    public static void onRegisterBrainGoals(Brain<Villager> pVillagerBrain, AbstractVillager villagerMixin) {
        //not sure if it will work
        BlockEvent
        pVillagerBrain.getMemories().put(MemoryModuleType.ATTACK_TARGET, Optional.empty());
        pVillagerBrain.getMemories().put(ModRegistry.PUMPKIN_POS.get(), Optional.empty());
        pVillagerBrain.getMemories().put(ModRegistry.NEAREST_PUMPKIN.get(), Optional.empty());
        Halloween.addSensorToVillagers(pVillagerBrain, ModRegistry.PUMPKIN_POI_SENSOR.get());

        if (this.isBaby()) {

            pVillagerBrain.setSchedule(AI.INITIALIZED_BABY_VILLAGER_SCHEDULE);
            //use addActivityWithCondition
            //pVillagerBrain.addActivity(ModRegistry.EAT_CANDY.get(), AI.getEatCandyPackage(0.5f));
            pVillagerBrain.addActivity(ModRegistry.TRICK_OR_TREAT.get(), AI.getTrickOrTreatPackage(0.5f));
            //replaces play package
            pVillagerBrain.addActivity(Activity.PLAY, AI.getHalloweenPlayPackage(0.5F));
            pVillagerBrain.updateActivityFromSchedule(this.level.getDayTime(), this.level.getGameTime());
        } else {
            pVillagerBrain.addActivity(Activity.REST, AI.getHalloweenRestPackage(this.getVillagerData().getProfession(), 0.5F));
            pVillagerBrain.addActivity(Activity.IDLE, AI.getHalloweenIdlePackage(this.getVillagerData().getProfession(), 0.5F));
        }
    }

    public static class TestEvent extends Event{
        IEventListener e = VillagerAIManager::testListener;
    }

    public static class VillagerBrainListener implements IEventListener{

        @Override
        public void invoke(Event event) {

        }
    }

    public static void testListener(TestEvent event){

    }

}
