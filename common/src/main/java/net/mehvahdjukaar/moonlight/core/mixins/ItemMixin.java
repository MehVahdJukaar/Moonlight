package net.mehvahdjukaar.moonlight.core.mixins;

import net.mehvahdjukaar.moonlight.api.item.ClientAnimationExtension;
import net.mehvahdjukaar.moonlight.api.item.additional_placements.AdditionalItemPlacement;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.map.ExpandedMapData;
import net.mehvahdjukaar.moonlight.core.misc.IExtendedItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Consumer;

//makes any item potentially placeable
@Mixin(Item.class)
public abstract class ItemMixin implements IExtendedItem {

    @Unique
    @Nullable
    private AdditionalItemPlacement moonlight$additionalBehavior;

    @Nullable
    @Unique
    ClientAnimationExtension moonlight$clientAnimationProvider;

    //delegates stuff to internal blockItem
    @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
    private void onUseOnBlock(UseOnContext pContext, CallbackInfoReturnable<InteractionResult> cir) {
        AdditionalItemPlacement behavior = this.moonlight$getAdditionalBehavior();
        if (behavior != null) {
            var result = behavior.overrideUseOn(pContext, PlatHelper.getFoodProperties(pContext.getItemInHand(), pContext.getPlayer()));
            if (result.consumesAction()) cir.setReturnValue(result);
        }
    }

    //MapItem does not override this, so the tooltip hook for custom map data has to sit on Item
    @Inject(method = "appendHoverText", at = @At("HEAD"))
    private void moonlight$appendMapHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                              Consumer<Component> builder, TooltipFlag tooltipFlag, CallbackInfo ci) {
        if (!((Object) this instanceof MapItem)) return;
        MapItemSavedData mapData = context.mapData(stack.get(DataComponents.MAP_ID));
        if (mapData instanceof ExpandedMapData data) {
            data.ml$getCustomData().forEach((s, o) -> {
                Component c = o.onItemTooltip(mapData, stack);
                if (c != null) builder.accept(c);
            });
        }
    }

    @Nullable
    public AdditionalItemPlacement moonlight$getAdditionalBehavior() {
        return this.moonlight$additionalBehavior;
    }

    @Override
    public void moonlight$setAdditionalBehavior(AdditionalItemPlacement placementOverride) {
        this.moonlight$additionalBehavior = placementOverride;
    }

    @Override
    public @Nullable ClientAnimationExtension moonlight$getClientAnimationExtension() {
        return moonlight$clientAnimationProvider;
    }

    @Override
    public void moonlight$setClientAnimationExtension(ClientAnimationExtension obj) {
        this.moonlight$clientAnimationProvider = obj;
    }
}
