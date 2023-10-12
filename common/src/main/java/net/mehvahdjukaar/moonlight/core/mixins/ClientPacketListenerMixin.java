package net.mehvahdjukaar.moonlight.core.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.mehvahdjukaar.moonlight.core.ClientConfigs;
import net.mehvahdjukaar.moonlight.core.misc.IMapDataPacketExtension;
import net.minecraft.client.gui.MapRenderer;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundMapItemDataPacket;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {

    @WrapOperation(method = "handleMapItemData",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/MapRenderer;update(ILnet/minecraft/world/level/saveddata/maps/MapItemSavedData;)V"))
    private void handleExtraData(MapRenderer instance, int mapId, MapItemSavedData mapData, Operation<Void> operation,
                                 @Local ClientboundMapItemDataPacket packet) {
        IMapDataPacketExtension ext = (IMapDataPacketExtension) packet;
        var serverData = ext.moonlight$getCustomMapDataTag();
        boolean updateTexture = ext.moonlight$getColorPatch() != null;
        if (serverData != null) {
            updateTexture = true;
        }
        updateTexture = updateTexture || ClientConfigs.LAZY_MAP_DATA.get();
        //suppress un needed map rendered texture uploads
        if (updateTexture) {
            operation.call(instance, mapId, mapData);
        }
    }
}
