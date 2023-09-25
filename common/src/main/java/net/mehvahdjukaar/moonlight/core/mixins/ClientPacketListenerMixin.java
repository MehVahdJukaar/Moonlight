package net.mehvahdjukaar.moonlight.core.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.mehvahdjukaar.moonlight.api.map.CustomMapData;
import net.mehvahdjukaar.moonlight.api.map.CustomMapDecoration;
import net.mehvahdjukaar.moonlight.api.map.ExpandedMapData;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.MoonlightClient;
import net.mehvahdjukaar.moonlight.core.misc.IMapDataPacketExtension;
import net.minecraft.client.gui.MapRenderer;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundMapItemDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {

    @WrapOperation(method = "handleMapItemData",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/MapRenderer;update(ILnet/minecraft/world/level/saveddata/maps/MapItemSavedData;)V"))
    private void handleExtraData(MapRenderer instance, int mapId, MapItemSavedData mapData, Operation<Void> operation,
                                 @Local ClientboundMapItemDataPacket packet) {
        IMapDataPacketExtension ext = (IMapDataPacketExtension) packet;
        var serverDeco = ext.moonlight$getCustomDecorations();
        var serverData = ext.moonlight$getCustomMapData();

        var center = ext.moonlight$getMapCenter();
        mapData.centerX = center.x;
        mapData.centerZ = center.y;
        mapData.dimension = ext.moonlight$getDimension();

        boolean updateTexture = ext.moonlight$getColorPatch() != null;

        if (serverDeco != null || serverData != null) {

            if (mapData instanceof ExpandedMapData ed) {
                //mapData = MapItemSavedData.createForClient(message.scale, message.locked, Minecraft.getInstance().level.dimension());
                //Minecraft.getInstance().level.setMapData(string, mapData);

                if (serverDeco != null) {
                    Map<String, CustomMapDecoration> decorations = ed.getCustomDecorations();
                    decorations.clear();
                    for (int i = 0; i < serverDeco.length; ++i) {
                        CustomMapDecoration customDecoration = serverDeco[i];
                        if (customDecoration != null) decorations.put("icon-" + i, customDecoration);
                        else {
                            Moonlight.LOGGER.warn("Failed to load custom map decoration, skipping");
                        }
                    }
                }
                if (serverData != null) {
                    Map<ResourceLocation, CustomMapData> customData = ed.getCustomData();
                    customData.clear();
                    for (CustomMapData d : serverData) {
                        if (d != null) customData.put(d.getType().id(), d);
                        else {
                            Moonlight.LOGGER.warn("Failed to load custom map mapData, skipping");
                        }
                    }
                    updateTexture = true;
                }
            }
        }

        updateTexture = updateTexture || MoonlightClient.LAZY_MAP_DATA.get();

        if (updateTexture) {
            operation.call(instance, mapId, mapData);
        }
    }
}
