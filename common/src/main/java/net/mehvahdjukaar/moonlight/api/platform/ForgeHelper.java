package net.mehvahdjukaar.moonlight.api.platform;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.HitResult;

import java.util.List;

/**
 * Helper class dedicated to platform forge specific methods
 */
public class ForgeHelper {

    @ExpectPlatform
    public static FinishedRecipe addRecipeConditions(FinishedRecipe originalRecipe, List<Object> conditions){
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean onProjectileImpact(Projectile improvedProjectileEntity, HitResult blockHitResult) {
        throw new AssertionError();
    }
}
