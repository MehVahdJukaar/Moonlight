package net.mehvahdjukaar.moonlight.platform.forge;

import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.event.ForgeEventFactory;

public class EventPlatformHelperImpl {
    public static boolean onProjectileImpact(Projectile projectile, HitResult blockHitResult) {
       return ForgeEventFactory.onProjectileImpact(projectile, blockHitResult);
    }
}
