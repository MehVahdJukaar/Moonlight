package net.mehvahdjukaar.selene.network;

import net.mehvahdjukaar.selene.Selene;
import net.mehvahdjukaar.selene.map.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.MapRenderer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

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
    private final CustomDataHolder.Instance<?>[] customData;

    public ClientBoundSyncCustomMapDecorationPacket(
            int mapId, byte pScale, boolean pLocked, @Nullable MapItemSavedData.MapPatch pColorPatch,
            CustomDecoration[] customDecoration, CustomDataHolder.Instance<?>[] customData) {
        this.mapId = mapId;
        this.scale = pScale;
        this.locked = pLocked;
        this.colorPatch = pColorPatch;

        this.customData = customData;
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
            byte[] byteArray = pBuffer.readByteArray();
            this.colorPatch = new MapItemSavedData.MapPatch(k, l, i, j, byteArray);
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
        //TODO: I really could have merged the 2 systems
        this.customData = new CustomDataHolder.Instance[pBuffer.readVarInt()];
        for (int m = 0; m < this.customData.length; ++m) {
            CustomDataHolder<?> type = MapDecorationHandler.CUSTOM_MAP_DATA_TYPES.getOrDefault(pBuffer.readUtf(), null);
            if (type != null) {
                this.customData[m] = type.createFromBuffer(pBuffer);
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

        buffer.writeVarInt(message.customData.length);

        for (CustomDataHolder.Instance<?> data : message.customData) {
            buffer.writeUtf(data.getType().id());
            data.saveToBuffer(buffer);
        }
    }

    public static void handler(ClientBoundSyncCustomMapDecorationPacket message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (context.getDirection() == NetworkDirection.PLAY_TO_CLIENT) {

                MapRenderer mapRenderer = Minecraft.getInstance().gameRenderer.getMapRenderer();

                int i = message.getMapId();
                String s = MapItem.makeKey(i);
                MapItemSavedData mapData = Minecraft.getInstance().level.getMapData(s);

                if (mapData == null) {

                    mapData = MapItemSavedData.createForClient(message.scale, message.locked, Minecraft.getInstance().level.dimension());
                    Minecraft.getInstance().level.setMapData(s, mapData);
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
        if (data instanceof ExpandedMapData mapData) {
            Map<String, CustomDecoration> decorations = mapData.getCustomDecorations();
            decorations.clear();
            for (int i = 0; i < this.customDecoration.length; ++i) {
                CustomDecoration customDecoration = this.customDecoration[i];
                if (customDecoration != null) decorations.put("icon-" + i, customDecoration);
                else {
                    Selene.LOGGER.warn("Failed to load custom map decoration, skipping");
                }
            }
            Map<String, CustomDataHolder.Instance<?>> customData = mapData.getCustomData();
            customData.clear();
            for (CustomDataHolder.Instance<?> instance : this.customData) {
                if (instance != null) customData.put(instance.getType().id(), instance);
                else {
                    Selene.LOGGER.warn("Failed to load custom map data, skipping: " + instance.getType().id());
                }
            }

        }
    }
}