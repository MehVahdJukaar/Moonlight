package net.mehvahdjukaar.moonlight.core.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.mehvahdjukaar.moonlight.api.map.CustomMapData;
import net.mehvahdjukaar.moonlight.api.map.CustomMapDecoration;
import net.mehvahdjukaar.moonlight.api.map.ExpandedMapData;
import net.mehvahdjukaar.moonlight.api.map.markers.MapBlockMarker;
import net.mehvahdjukaar.moonlight.core.map.MapDataInternal;
import net.mehvahdjukaar.moonlight.core.misc.IHoldingPlayerExtension;
import net.mehvahdjukaar.moonlight.core.misc.IMapDataPacketExtension;
import net.minecraft.nbt.CompoundTag;
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
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

@Mixin(MapItemSavedData.HoldingPlayer.class)
public abstract class HoldingPlayerMixin implements IHoldingPlayerExtension {

    @Inject(method = "<init>", at = @At("TAIL"))
    public void initializeDirty(MapItemSavedData mapItemSavedData, Player player, CallbackInfo ci) {
        //just to be sure. we HAVE to send this on the very first update packet
        moonlight$customMarkersDirty = true;
        for (var v : ((ExpandedMapData) mapItemSavedData).getCustomData().values()) {
            moonlight$customDataDirty.put(v.getType(), v.createDirtyCounter());
        }
    }

    @Unique
    private final ReentrantLock moonlight$concurrentLock = new ReentrantLock();

    @Unique
    private final Map<CustomMapData.Type<?>, CustomMapData.DirtyCounter> moonlight$customDataDirty = new IdentityHashMap<>();
    @Unique
    private boolean moonlight$customMarkersDirty = true;

    @Unique
    private int moonlight$dirtyDecorationTicks = 0;
    @Unique
    private int moonlight$volatileDecorationRefreshTicks = 0;

    @Final
    @Shadow
    MapItemSavedData field_132;

    @Shadow
    @Final
    public Player player;

    @Inject(method = "nextUpdatePacket", at = @At("HEAD"), cancellable = true)
    public void checkLocked(int mapId, CallbackInfoReturnable<@Nullable Packet<?>> cir) {
        //we won't wait here. if its locked too bad we cant block the main thread
        if (moonlight$concurrentLock.isLocked()) cir.setReturnValue(null);
    }

    @ModifyReturnValue(method = "nextUpdatePacket", at = @At("TAIL"))
    public Packet<?> addExtraPacketData(@Nullable Packet<?> packet, int mapId) {
        MapItemSavedData data = field_132;
        ExpandedMapData ed = ((ExpandedMapData) data);

        boolean updateData = false;
        boolean updateDeco = false;

        // same logic as vanilla just for custom stuff
        List<Map.Entry<CustomMapData.Type<?>, CustomMapData.DirtyCounter>> dirtyData = new ArrayList<>();
        for (var e : moonlight$customDataDirty.entrySet()) {
            CustomMapData.DirtyCounter value = e.getValue();
            if (value.isDirty()) {
                dirtyData.add(e);
                updateData = true;
            }
        }
        if (this.moonlight$customMarkersDirty && this.moonlight$dirtyDecorationTicks++ % 5 == 0) {
            this.moonlight$customMarkersDirty = false;
            updateDeco = true;
        }
        //update every 5 sec
        List<CustomMapDecoration> extra = new ArrayList<>();
        //we got to update every darn time if we want other heartstone players to be updated properly. Cant use client info because client doesnt have them
        //re any time check optimization will be done by those getDynamic functions
        //if ((moonlight$volatileDecorationRefreshTicks++ % (20 * 4)) == 0 || updateDeco) {
        //adds dynamic decoration and sends them to a client
        for (MapBlockMarker<?> m : MapDataInternal.getDynamicServer(player, mapId, data)) {
            var d = m.createDecorationFromMarker(data);
            if (d != null) extra.add(d);
        }
        // only send if we have stuff to update or every 4 sec
        // this ensures removal happens (even if late), while keeping additions / modifications instant
        if(!extra.isEmpty() || (moonlight$volatileDecorationRefreshTicks++ % (20 * 4)) == 0) updateDeco = true;
        //}

        if (updateData || updateDeco) {
            // creates a new packet or modify existing one
            if (packet == null) {
                packet = new ClientboundMapItemDataPacket(mapId,
                        field_132.scale, field_132.locked, null, null);
            }
            IMapDataPacketExtension ep = ((IMapDataPacketExtension) packet);

            if (updateData) {
                CompoundTag customDataTag = new CompoundTag();
                for (var e : dirtyData) {
                    saveDataToUpdateTag(ed, customDataTag, e);
                    e.getValue().clearDirty();
                }
                ep.moonlight$sendCustomMapDataTag(customDataTag);
            }
            if (updateDeco) {
                List<CustomMapDecoration> decorations = new ArrayList<>(ed.getCustomDecorations().values());
                decorations.addAll(extra);

                ep.moonlight$sendCustomDecorations(decorations);
            }
        }
        return packet;
    }

    @Unique
    private static <C extends CustomMapData.DirtyCounter, D extends CustomMapData<C>> void saveDataToUpdateTag(
            ExpandedMapData ed, CompoundTag customDataTag,
            Map.Entry<CustomMapData.Type<?>, CustomMapData.DirtyCounter> e) {
        D d = (D) ed.getCustomData().get(e.getKey().id());
        //TODO: put this in a separate compound. cant cause of backwards compat
        C value = (C) e.getValue();
        d.saveToUpdateTag(customDataTag, value);
    }

    @Override
    public <H extends CustomMapData.DirtyCounter> void moonlight$setCustomDataDirty(
            CustomMapData.Type<?> type, Consumer<H> dirtySetter) {
        try {
            moonlight$concurrentLock.lock();
            var t = this.moonlight$customDataDirty.get(type);
            dirtySetter.accept((H) t);
        }finally {
            moonlight$concurrentLock.unlock();
        }
    }

    @Override
    public void moonlight$setCustomMarkersDirty() {
        this.moonlight$customMarkersDirty = true;
    }


    @Inject(method = "markColorsDirty", at = @At("HEAD"))
    public void lockData(int x, int z, CallbackInfo ci) {
        moonlight$concurrentLock.lock();
    }

    @Inject(method = "markColorsDirty", at = @At("RETURN"))
    public void sanityCheck(int x, int z, CallbackInfo ci) {
        moonlight$concurrentLock.unlock();
    }
}
