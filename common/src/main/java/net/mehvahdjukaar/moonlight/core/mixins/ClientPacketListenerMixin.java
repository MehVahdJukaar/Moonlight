package net.mehvahdjukaar.moonlight.core.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.mehvahdjukaar.moonlight.core.ClientConfigs;
import net.mehvahdjukaar.moonlight.core.misc.IMapDataPacketExtension;
import net.minecraft.client.gui.MapRenderer;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundMapItemDataPacket;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {

    @WrapOperation(method = "handleMapItemData",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/MapRenderer;update(Lnet/minecraft/world/level/saveddata/maps/MapId;Lnet/minecraft/world/level/saveddata/maps/MapItemSavedData;)V"))
    private void handleExtraData(MapRenderer instance, MapId mapId, MapItemSavedData mapData, Operation<Void> operation,
                                 @Local(argsOnly = true) ClientboundMapItemDataPacket packet) {
        IMapDataPacketExtension ext = (IMapDataPacketExtension) (Object) packet;
        var customServerData = ext.moonlight$getCustomMapDataBuf();
        boolean updateTexture = packet.colorPatch().isPresent();
        if (customServerData.isPresent()) {
            updateTexture = true;
        }
        updateTexture = updateTexture || !ClientConfigs.LAZY_MAP_DATA.get();
        //suppress un needed map rendered texture uploads
        if (updateTexture) {
            operation.call(instance, mapId, mapData);
        }
    }
}
