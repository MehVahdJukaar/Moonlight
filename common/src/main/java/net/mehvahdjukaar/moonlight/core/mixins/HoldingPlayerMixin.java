package net.mehvahdjukaar.moonlight.core.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.mehvahdjukaar.moonlight.api.map.CustomMapDecoration;
import net.mehvahdjukaar.moonlight.api.map.ExpandedMapData;
import net.mehvahdjukaar.moonlight.api.map.MapDecorationRegistry;
import net.mehvahdjukaar.moonlight.core.misc.IHoldingPlayerExtension;
import net.mehvahdjukaar.moonlight.core.misc.IMapDataPacketExtension;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundMapItemDataPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.List;

@Mixin(MapItemSavedData.HoldingPlayer.class)
public class HoldingPlayerMixin implements IHoldingPlayerExtension {

    @Unique
    private boolean moonlight$customDataDirty = false;
    @Unique
    private boolean moonlight$customMarkersDirty = false;

    @Unique
    private int moonlight$dirtyDecorationTicks = 0;

    @Final
    @Shadow
    MapItemSavedData field_132;

    @Shadow
    @Final
    public Player player;

    @ModifyReturnValue(method = "nextUpdatePacket", at = @At("TAIL"))
    public Packet<?> addExtraPacketData(@Nullable Packet<?> packet, int mapId) {
        MapItemSavedData data = field_132;
        ExpandedMapData ed = ((ExpandedMapData) data);

        boolean updateData = false;
        boolean updateDeco = false;

        // same logic as vanilla just for custom stuff
        if (this.moonlight$customDataDirty) {
            this.moonlight$customDataDirty = false;
            updateData = true;
        }
        if (this.moonlight$customMarkersDirty && this.moonlight$dirtyDecorationTicks++ % 5 == 0) {
            this.moonlight$customMarkersDirty = false;
            updateDeco = true;
        }

        if (updateData || updateDeco) {
            // creates a new packet or modify existing one
            if (packet == null) {
                packet = new ClientboundMapItemDataPacket(mapId,
                        field_132.scale, field_132.locked, null, null);
            }
            IMapDataPacketExtension ep = ((IMapDataPacketExtension) packet);

            if (updateData) {
                ep.moonlight$sendCustomMapData(ed.getCustomData().values());
            }
            if (updateDeco) {
                List<CustomMapDecoration> decorations = new ArrayList<>(ed.getCustomDecorations().values());

                //adds dynamic decoration and sends them to a client
                for (var t : MapDecorationRegistry.getValues()) {
                    var l = t.getDynamicDecorations(player, data);
                    if (!l.isEmpty()) decorations.addAll(l);
                }
                ep.moonlight$sendCustomDecorations(decorations);
            }
        }
        if (packet != null) {
            ((IMapDataPacketExtension) packet).moonlight$sendMapCenter(data.centerX, data.centerZ);
            // also sends here just incase
            ((IMapDataPacketExtension) packet).moonlight$sendCustomMapData(ed.getCustomData().values());

        }
        return packet;
    }

    @Override
    public void moonlight$setCustomDataDirty() {
        this.moonlight$customDataDirty = true;
    }

    @Override
    public void moonlight$setCustomMarkersDirty() {
        this.moonlight$customMarkersDirty = true;
    }
}
