package net.mehvahdjukaar.moonlight.network;

import net.mehvahdjukaar.moonlight.Moonlight;
import net.mehvahdjukaar.moonlight.map.*;
import net.mehvahdjukaar.moonlight.map.type.IMapDecorationType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.MapRenderer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.Map;
import java.util.function.Supplier;


public class ClientBoundSyncCustomMapDecorationPacket {
    private final int mapId;
    private final byte scale;
    private final boolean locked;

    private final CustomMapDecoration[] customDecoration;
    private final CustomDataHolder.Instance<?>[] customData;

    public ClientBoundSyncCustomMapDecorationPacket(
            int mapId, byte pScale, boolean pLocked,
            CustomMapDecoration[] customDecoration, CustomDataHolder.Instance<?>[] customData) {
        this.mapId = mapId;
        this.scale = pScale;
        this.locked = pLocked;

        this.customData = customData;
        this.customDecoration = customDecoration;
    }

    public ClientBoundSyncCustomMapDecorationPacket(FriendlyByteBuf pBuffer) {
        this.mapId = pBuffer.readVarInt();
        this.scale = pBuffer.readByte();
        this.locked = pBuffer.readBoolean();

        this.customDecoration = new CustomMapDecoration[pBuffer.readVarInt()];

        for (int m = 0; m < this.customDecoration.length; ++m) {
            IMapDecorationType<?, ?> type = MapDecorationRegistry.get(pBuffer.readResourceLocation());
            if (type != null) {
                this.customDecoration[m] = type.loadDecorationFromBuffer(pBuffer);
            }
        }
        //TODO: I really could have merged the 2 systems
        this.customData = new CustomDataHolder.Instance[pBuffer.readVarInt()];
        for (int m = 0; m < this.customData.length; ++m) {
            CustomDataHolder<?> type = MapDecorationRegistry.CUSTOM_MAP_DATA_TYPES.getOrDefault(new ResourceLocation(pBuffer.readUtf()), null);
            if (type != null) {
                this.customData[m] = type.createFromBuffer(pBuffer);
            }
        }
    }


    public static void buffer(ClientBoundSyncCustomMapDecorationPacket message, FriendlyByteBuf buffer) {
        buffer.writeVarInt(message.mapId);
        buffer.writeByte(message.scale);
        buffer.writeBoolean(message.locked);

        buffer.writeVarInt(message.customDecoration.length);

        for (CustomMapDecoration decoration : message.customDecoration) {
            buffer.writeResourceLocation(decoration.getType().getId());
            decoration.saveToBuffer(buffer);
        }

        buffer.writeVarInt(message.customData.length);

        for (CustomDataHolder.Instance<?> data : message.customData) {
            buffer.writeUtf(data.getType().id().toString());
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

                if (mapData != null) {
                    //mapData = MapItemSavedData.createForClient(message.scale, message.locked, Minecraft.getInstance().level.dimension());
                    //Minecraft.getInstance().level.setMapData(s, mapData);
                    message.applyToMap(mapData);
                    mapRenderer.update(i, mapData);
                }
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
            Map<String, CustomMapDecoration> decorations = mapData.getCustomDecorations();
            decorations.clear();
            for (int i = 0; i < this.customDecoration.length; ++i) {
                CustomMapDecoration customDecoration = this.customDecoration[i];
                if (customDecoration != null) decorations.put("icon-" + i, customDecoration);
                else {
                    Moonlight.LOGGER.warn("Failed to load custom map decoration, skipping");
                }
            }
            Map<ResourceLocation, CustomDataHolder.Instance<?>> customData = mapData.getCustomData();
            customData.clear();
            for (CustomDataHolder.Instance<?> instance : this.customData) {
                if (instance != null) customData.put(instance.getType().id(), instance);
                else {
                    Moonlight.LOGGER.warn("Failed to load custom map data, skipping");
                }
            }

        }
    }
}