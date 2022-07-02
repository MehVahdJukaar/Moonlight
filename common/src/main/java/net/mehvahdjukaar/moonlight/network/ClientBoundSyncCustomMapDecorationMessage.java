package net.mehvahdjukaar.moonlight.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.mehvahdjukaar.moonlight.Moonlight;
import net.mehvahdjukaar.moonlight.map.CustomDataHolder;
import net.mehvahdjukaar.moonlight.map.CustomMapDecoration;
import net.mehvahdjukaar.moonlight.map.ExpandedMapData;
import net.mehvahdjukaar.moonlight.map.MapDecorationRegistry;
import net.mehvahdjukaar.moonlight.map.type.IMapDecorationType;
import net.mehvahdjukaar.moonlight.platform.network.ChannelHandler;
import net.mehvahdjukaar.moonlight.platform.network.Message;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.MapRenderer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

import java.util.Map;


public class ClientBoundSyncCustomMapDecorationMessage implements Message {
    private final int mapId;
    private final byte scale;
    private final boolean locked;

    private final CustomMapDecoration[] customDecoration;
    private final CustomDataHolder.Instance<?>[] customData;

    public ClientBoundSyncCustomMapDecorationMessage(
            int mapId, byte pScale, boolean pLocked,
            CustomMapDecoration[] customDecoration, CustomDataHolder.Instance<?>[] customData) {
        this.mapId = mapId;
        this.scale = pScale;
        this.locked = pLocked;

        this.customData = customData;
        this.customDecoration = customDecoration;
    }

    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeVarInt(this.mapId);
        buffer.writeByte(this.scale);
        buffer.writeBoolean(this.locked);

        buffer.writeVarInt(this.customDecoration.length);

        for (CustomMapDecoration decoration : this.customDecoration) {
            buffer.writeResourceLocation(decoration.getType().getId());
            decoration.saveToBuffer(buffer);
        }

        buffer.writeVarInt(this.customData.length);

        for (CustomDataHolder.Instance<?> data : this.customData) {
            buffer.writeUtf(data.getType().id().toString());
            data.saveToBuffer(buffer);
        }
    }

    //receivers
    public ClientBoundSyncCustomMapDecorationMessage(FriendlyByteBuf pBuffer) {
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

    @Override
    public void handle(ChannelHandler.Context context) {
        if (context.getDirection() == ChannelHandler.NetworkDir.PLAY_TO_CLIENT) {

            MapRenderer mapRenderer = Minecraft.getInstance().gameRenderer.getMapRenderer();

            int i = this.getMapId();
            String s = MapItem.makeKey(i);
            MapItemSavedData mapData = Minecraft.getInstance().level.getMapData(s);

            if (mapData != null) {
                //mapData = MapItemSavedData.createForClient(message.scale, message.locked, Minecraft.getInstance().level.dimension());
                //Minecraft.getInstance().level.setMapData(s, mapData);
                this.applyToMap(mapData);
                mapRenderer.update(i, mapData);
            }
        }
    }

    @Environment(EnvType.CLIENT)
    public int getMapId() {
        return this.mapId;
    }

    @Environment(EnvType.CLIENT)
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