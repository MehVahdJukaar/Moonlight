package net.mehvahdjukaar.moonlight.core.mixins;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.mehvahdjukaar.moonlight.api.item.additional_placements.AdditionalItemPlacement;
import net.mehvahdjukaar.moonlight.core.misc.IExtendedItem;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

//makes any item potentially placeable
@Mixin(Item.class)
public abstract class ItemMixin implements IExtendedItem {

    @Unique
    @Nullable
    private AdditionalItemPlacement moonlight$additionalBehavior;

    @Environment(EnvType.CLIENT)
    @Nullable
    @Unique Object moonlight$clientAnimationProvider;

    @Shadow
    @Final
    @Nullable
    private FoodProperties foodProperties;

    //delegates stuff to internal blockItem
    @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
    private void onUseOnBlock(UseOnContext pContext, CallbackInfoReturnable<InteractionResult> cir) {
        AdditionalItemPlacement behavior = this.moonlight$getAdditionalBehavior();
        if (behavior != null) {
            var result = behavior.overrideUseOn(pContext, foodProperties);
            if (result.consumesAction()) cir.setReturnValue(result);
        }
    }

    @Nullable
    public AdditionalItemPlacement moonlight$getAdditionalBehavior() {
        return this.moonlight$additionalBehavior;
    }

    @Override
    public void moonlight$addAdditionalBehavior(AdditionalItemPlacement placementOverride) {
        this.moonlight$additionalBehavior = placementOverride;
    }

    @Override
    public @Nullable Object moonlight$getClientAnimationExtension() {
        return moonlight$clientAnimationProvider;
    }

    public void moonlight$setClientAnimationExtension(Object obj) {
        this.moonlight$clientAnimationProvider = obj;
    }
}
