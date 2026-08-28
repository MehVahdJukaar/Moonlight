package net.mehvahdjukaar.moonlight.core.mixins;

import net.mehvahdjukaar.moonlight.api.map.ExpandedMapData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.atomic.AtomicBoolean;


@Mixin(MapItem.class)
public abstract class MapItemMixin {

    @Inject(method = "update", at = @At("HEAD"), cancellable = true)
    private void update(Level level, Entity entity, MapItemSavedData data, CallbackInfo ci) {
        AtomicBoolean b = new AtomicBoolean(false);
        if (data instanceof ExpandedMapData d) {
            d.ml$getCustomData().forEach((s, o) -> {
                if (o.onItemUpdate(data, entity)) b.set(true);
            });
        }
        if (b.get()) ci.cancel();
    }
}
