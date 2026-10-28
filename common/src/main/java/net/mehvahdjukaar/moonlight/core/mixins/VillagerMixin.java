package net.mehvahdjukaar.moonlight.core.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.mehvahdjukaar.moonlight.core.misc.VillagerAIInternal;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Villager.class)
public abstract class VillagerMixin extends AbstractVillager {

    protected VillagerMixin(EntityType<? extends AbstractVillager> entityType, Level level) {
        super(entityType, level);
    }

    @WrapOperation(method = {"makeBrain", "refreshBrain"}, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/ai/Brain$Provider;makeBrain(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/ai/Brain$Packed;)Lnet/minecraft/world/entity/ai/Brain;"))
    protected Brain<Villager> ml$addExtraBrainGoals(Brain.Provider<Villager> provider, LivingEntity body, Brain.Packed packed,
                                                    Operation<Brain<Villager>> original) {
        Brain<Villager> brain = original.call(provider, body, packed);
        VillagerAIInternal.onBrainMade(brain, this, packed);

        return brain;
    }

}
