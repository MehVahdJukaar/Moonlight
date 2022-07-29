package net.mehvahdjukaar.moonlight.api.platform.forge;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.crafting.ConditionalRecipe;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.event.ForgeEventFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class ForgeHelperImpl {

    public static boolean onProjectileImpact(Projectile projectile, HitResult blockHitResult) {
        return ForgeEventFactory.onProjectileImpact(projectile, blockHitResult);
    }

    public static FinishedRecipe addRecipeConditions(FinishedRecipe originalRecipe, List<Object> conditions) {
        boolean success = false;
        var builder = ConditionalRecipe.builder();
        for (var c : conditions) {
            if (c instanceof ICondition condition) {
                builder.addCondition(condition);
                success = true;
            }
        }
        if (success) {
            AtomicReference<FinishedRecipe> newRecipe = new AtomicReference<>();
            builder.addRecipe(originalRecipe);
            builder.build(newRecipe::set, originalRecipe.getId());
            return newRecipe.get();
        }
        return originalRecipe;
    }
}
