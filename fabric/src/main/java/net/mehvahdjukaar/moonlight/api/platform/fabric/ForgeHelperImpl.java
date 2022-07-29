package net.mehvahdjukaar.moonlight.api.platform.fabric;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.HitResult;

import java.util.List;

public class ForgeHelperImpl {

    public static boolean onProjectileImpact(Projectile improvedProjectileEntity, HitResult blockHitResult) {
        return true;
    }

    public static FinishedRecipe addRecipeConditions(FinishedRecipe originalRecipe, List<Object> conditions) {
        return originalRecipe;
    }
}
