package net.mehvahdjukaar.moonlight.core.mixins;

import net.mehvahdjukaar.moonlight.core.misc.VillagerAIInternal;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Villager.class)
public abstract class VillagerMixin extends AbstractVillager {

    protected VillagerMixin(EntityType<? extends AbstractVillager> entityType, Level level) {
        super(entityType, level);
    }

    //makeBrain and not registerBrainGoals since we need the packed memories
    @Inject(method = "makeBrain", at = @At("RETURN"))
    protected void ml$addExtraBrainGoals(Brain.Packed packed, CallbackInfoReturnable<Brain<Villager>> cir) {
        VillagerAIInternal.onBrainMade(cir.getReturnValue(), this, packed);
    }


}