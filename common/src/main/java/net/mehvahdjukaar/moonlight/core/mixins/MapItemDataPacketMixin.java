package net.mehvahdjukaar.moonlight.core.mixins;

import io.netty.buffer.Unpooled;
import net.mehvahdjukaar.moonlight.api.map.CustomMapData;
import net.mehvahdjukaar.moonlight.api.map.CustomMapDecoration;
import net.mehvahdjukaar.moonlight.api.map.MapDecorationRegistry;
import net.mehvahdjukaar.moonlight.api.map.type.MapDecorationType;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.moonlight.core.client.MapStuffClient;
import net.mehvahdjukaar.moonlight.core.misc.IMapDataPacketExtension;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundMapItemDataPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;

//I hope this won't break with mods. We need this as all data needs to be received at the same time
@Mixin(ClientboundMapItemDataPacket.class)
public class MapItemDataPacketMixin implements IMapDataPacketExtension {

    @Shadow
    @Final
    private int mapId;
    @Shadow
    @Final
    private byte scale;
    @Shadow
    @Final
    private boolean locked;
    @Unique
    private CustomMapDecoration[] moonlight$customDecorations = null;
    @Unique
    private CustomMapData[] moonlight$customData = null;
    @Unique
    private int moonlight$mapCenterX = 0;
    @Unique
    private int moonlight$mapCenterZ = 0;

    @Inject(method = "<init>(Lnet/minecraft/network/FriendlyByteBuf;)V",
            at = @At("RETURN"))
    private void readExtraData(FriendlyByteBuf buf, CallbackInfo ci) {
        moonlight$mapCenterX = buf.readVarInt();
        moonlight$mapCenterZ = buf.readVarInt();
        if (buf.readBoolean()) {
            for (int m = 0; m < moonlight$customDecorations.length; ++m) {
                MapDecorationType<?, ?> type = MapDecorationRegistry.get(buf.readResourceLocation());
                if (type != null) {
                    moonlight$customDecorations[m] = type.loadDecorationFromBuffer(buf);
                }
            }
        }
        if (buf.readBoolean()) {
            //TODO: I really could have merged the 2 systems
            this.moonlight$customData = new CustomMapData[buf.readVarInt()];
            for (int m = 0; m < moonlight$customData.length; ++m) {
                CustomMapData.Type<?> type = MapDecorationRegistry.CUSTOM_MAP_DATA_TYPES.getOrDefault(buf.readResourceLocation(), null);
                if (type != null) {
                    moonlight$customData[m] = type.createFromBuffer(buf);
                }
            }
        }
    }

    @Inject(method = "write", at = @At("RETURN"))
    private void writeExtraData(FriendlyByteBuf buf, CallbackInfo ci) {
        buf.writeVarInt(moonlight$mapCenterX);
        buf.writeVarInt(moonlight$mapCenterZ);
        buf.writeBoolean(moonlight$customDecorations != null);
        if (moonlight$customDecorations != null) {
            buf.writeVarInt(moonlight$customDecorations.length);
            for (CustomMapDecoration decoration : moonlight$customDecorations) {
                buf.writeResourceLocation(Utils.getID(decoration.getType()));
                decoration.saveToBuffer(buf);
            }
        }

        buf.writeBoolean(moonlight$customData != null);
        if (moonlight$customData != null) {
            buf.writeVarInt(this.moonlight$customData.length);

            for (CustomMapData data : moonlight$customData) {
                buf.writeResourceLocation(data.getType().id());
                data.saveToBuffer(buf);
            }
        }
    }

    @Inject(method = "handle(Lnet/minecraft/network/protocol/game/ClientGamePacketListener;)V",
            at = @At("TAIL"))
    private void handleExtraData(ClientGamePacketListener handler, CallbackInfo ci) {
        MapStuffClient.handlePacketExtension(this.mapId, this.scale, this.locked,
                this.moonlight$mapCenterX, this.moonlight$mapCenterZ,
                this.moonlight$customDecorations,
                this.moonlight$customData);
    }

    @Override
    public void moonlight$sendCustomDecorations(Collection<CustomMapDecoration> decorations) {

        //packet will be passed to client no decoding. if we are on an integrated server we need to create new objects
        if (PlatHelper.getPhysicalSide().isClient()) {
            var buffer = new FriendlyByteBuf(Unpooled.buffer());
            decorations = decorations.stream().map(e -> {
                e.saveToBuffer(buffer);
                CustomMapDecoration d = e.getType().loadDecorationFromBuffer(buffer);
                return d;
            }).toList();
        }
        moonlight$customDecorations = decorations.toArray(CustomMapDecoration[]::new);
    }

    @Override
    public void moonlight$sendCustomMapData(Collection<CustomMapData> data) {

        //clone objects
        if (PlatHelper.getPhysicalSide().isClient()) {
            data = data.stream().map(e -> {
                CompoundTag tag = new CompoundTag();
                e.save(tag);
                CustomMapData n = e.getType().factory().apply(tag);
                return n;
            }).toList();
        }
        moonlight$customData = data.toArray(CustomMapData[]::new);
    }

    @Override
    public void moonlight$sendMapCenter(int centerX, int centerZ) {
        moonlight$mapCenterX = centerX;
        moonlight$mapCenterZ = centerZ;
    }
}
