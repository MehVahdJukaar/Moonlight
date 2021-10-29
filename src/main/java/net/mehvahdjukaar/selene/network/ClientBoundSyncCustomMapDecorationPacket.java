package net.mehvahdjukaar.selene.network;

import net.mehvahdjukaar.selene.Selene;
import net.mehvahdjukaar.selene.map.CustomDecoration;
import net.mehvahdjukaar.selene.map.ExpandedMapData;
import net.mehvahdjukaar.selene.map.CustomDecorationType;
import net.mehvahdjukaar.selene.map.MapDecorationHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.MapRenderer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fmllegacy.network.NetworkDirection;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.Supplier;


public class ClientBoundSyncCustomMapDecorationPacket {
    private final int mapId;
    private final byte scale;
    private final boolean locked;
    @Nullable
    private final MapItemSavedData.MapPatch colorPatch;

    private final CustomDecoration[] customDecoration;

    public ClientBoundSyncCustomMapDecorationPacket(int mapId, byte pScale, boolean pLocked, @Nullable MapItemSavedData.MapPatch pColorPatch, CustomDecoration[] customDecoration) {
        this.mapId = mapId;
        this.scale = pScale;
        this.locked = pLocked;
        this.colorPatch = pColorPatch;

        this.customDecoration = customDecoration;
    }

    public ClientBoundSyncCustomMapDecorationPacket(FriendlyByteBuf pBuffer) {
        this.mapId = pBuffer.readVarInt();
        this.scale = pBuffer.readByte();
        this.locked = pBuffer.readBoolean();

        int i = pBuffer.readUnsignedByte();
        if (i > 0) {
            int j = pBuffer.readUnsignedByte();
            int k = pBuffer.readUnsignedByte();
            int l = pBuffer.readUnsignedByte();
            byte[] abyte = pBuffer.readByteArray();
            this.colorPatch = new MapItemSavedData.MapPatch(k, l, i, j, abyte);
        } else {
            this.colorPatch = null;
        }

        this.customDecoration = new CustomDecoration[pBuffer.readVarInt()];

        for (int m = 0; m < this.customDecoration.length; ++m) {
            CustomDecorationType<?, ?> type = MapDecorationHandler.get(pBuffer.readResourceLocation());
            if (type != null) {
                this.customDecoration[m] = type.loadDecorationFromBuffer(pBuffer);
            }
        }
    }


    public static void buffer(ClientBoundSyncCustomMapDecorationPacket message, FriendlyByteBuf buffer) {
        buffer.writeVarInt(message.mapId);
        buffer.writeByte(message.scale);
        buffer.writeBoolean(message.locked);

        if (message.colorPatch != null) {
            buffer.writeByte(message.colorPatch.width);
            buffer.writeByte(message.colorPatch.height);
            buffer.writeByte(message.colorPatch.startX);
            buffer.writeByte(message.colorPatch.startY);
            buffer.writeByteArray(message.colorPatch.mapColors);
        } else {
            buffer.writeByte(0);
        }

        buffer.writeVarInt(message.customDecoration.length);

        for (CustomDecoration decoration : message.customDecoration) {
            buffer.writeResourceLocation(decoration.getType().getId());
            decoration.saveToBuffer(buffer);
        }
    }

    public static void handler(ClientBoundSyncCustomMapDecorationPacket message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (context.getDirection() == NetworkDirection.PLAY_TO_CLIENT) {

                MapRenderer mapRenderer = Minecraft.getInstance().gameRenderer.getMapRenderer();
                Level world = Minecraft.getInstance().level;

                int i = message.getMapId();
                String s = MapItem.makeKey(i);
                MapItemSavedData mapData = world.getMapData(s);

                if (mapData == null) {

                    mapData = MapItemSavedData.createForClient(message.scale, message.locked, world.dimension());
                    world.setMapData(s, mapData);
                }

                message.applyToMap(mapData);
                mapRenderer.update(i, mapData);
            }
        });
        context.setPacketHandled(true);
    }


    @OnlyIn(Dist.CLIENT)
    public int getMapId() {
        return this.mapId;
    }

    @OnlyIn(Dist.CLIENT)
    public void applyToMap(MapItemSavedData data) {
        if (data instanceof ExpandedMapData) {
            Map<String, CustomDecoration> decorations = ((ExpandedMapData) data).getCustomDecorations();
            decorations.clear();
            for (int i = 0; i < this.customDecoration.length; ++i) {
                CustomDecoration mapdecoration = this.customDecoration[i];
                if (mapdecoration != null) decorations.put("icon-" + i, mapdecoration);
                else {
                    Selene.LOGGER.warn("Failed to load custom map decoration, skipping");
                }
            }
        }
    }
}