package net.mehvahdjukaar.moonlight.core.mixins.fabric;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.mehvahdjukaar.moonlight.fabric.FabricRecipeConditionManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin {

    @Unique
    private static final Set<ResourceLocation> DISABLED_RECIPES = new HashSet<>();


    @Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At("TAIL"))
    public void clearDisabledRecipes(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler, CallbackInfo ci) {
        DISABLED_RECIPES.clear();
    }

    @Inject(method = "fromJson", at = @At("HEAD"))
    private static void handleForgeConditions(ResourceLocation recipeId, JsonObject json, CallbackInfoReturnable<Recipe<?>> cir) {
        if (FabricRecipeConditionManager.isRecipeDisabled(json.get("conditions"), recipeId)) {
            DISABLED_RECIPES.add(recipeId);
            throw new IllegalArgumentException("Skipping " + recipeId + " as its conditions were not met");
        }
    }

    @Redirect(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;error(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V"),
            require = 0)
    public void disableExceptions(Logger instance, String s, Object o1, Object o2) {
        if (!(o1 instanceof ResourceLocation res) || !DISABLED_RECIPES.contains(res)) {
            instance.error(s, o1, o2);
        }
    }

}
