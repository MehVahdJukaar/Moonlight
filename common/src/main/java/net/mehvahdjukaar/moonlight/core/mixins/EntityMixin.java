package net.mehvahdjukaar.moonlight.core.mixins;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Mob.class)
public abstract class EntityMixin extends LivingEntity {

    protected EntityMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Redirect(method = "spawnAnim", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;getX(D)D"),
    require = 0)
    public double fixSpawnAnimX(Mob instance, double v){
        return instance.getRandomX(v);
    }
}
